package com.example.budgetmate

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.room.withTransaction
import com.example.budgetmate.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth

/**
 * All budgeting rules live here so the UI stays dumb.
 *
 * Zero-sum rules enforced:
 *  - Income lands in "Ready to Assign".
 *  - You can only assign money that is Ready to Assign (no over-assigning).
 *  - Spending reduces an envelope; overspending is flagged and must be covered by moving money.
 *  - Savings-goal contributions are also taken from Ready to Assign.
 *  - Goal is Ready to Assign == 0 (every rupee has a job).
 */
class BudgetViewModel(app: Application) : AndroidViewModel(app) {

    private val db = Room.databaseBuilder(app, BudgetDatabase::class.java, "budgetmate.db")
        .addMigrations(BudgetDatabase.MIGRATION_1_2).build()
    private val dao = db.dao()

    val accounts: StateFlow<List<Account>> = dao.accounts().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val envelopes: StateFlow<List<Envelope>> = dao.envelopes().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val entries: StateFlow<List<LedgerEntry>> = dao.entries().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val goals: StateFlow<List<SavingsGoal>> = dao.goals().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val recurring: StateFlow<List<RecurringTransaction>> = dao.recurring().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch { runCatching { processRecurring() } }
    }

    private fun snapshot(month: YearMonth) = computeSnapshot(accounts.value, envelopes.value, entries.value, month)

    /** Ready to Assign across everything recorded so far (including future-dated entries). */
    private fun readyNow(): Long {
        val last = entries.value.maxOfOrNull { it.date }?.let { yearMonthOf(it) } ?: YearMonth.now()
        val m = if (last.isAfter(YearMonth.now())) last else YearMonth.now()
        return snapshot(m).readyToAssign
    }

    private fun anchorAccountId(): Long? = accounts.value.firstOrNull()?.id

    // ---------- Accounts ----------

    suspend fun addAccount(name: String, openingCents: Long): String? {
        if (name.isBlank()) return "Enter an account name."
        if (openingCents < 0) return "Opening balance cannot be negative."
        dao.addAccount(Account(name = name.trim(), openingBalance = openingCents))
        return null
    }

    suspend fun transfer(from: Long?, to: Long?, cents: Long, note: String): String? {
        if (from == null || to == null || from == to) return "Choose two different accounts."
        if (cents <= 0) return "Enter a positive amount."
        val now = System.currentTimeMillis()
        db.withTransaction {
            dao.addEntry(LedgerEntry(accountId = from, type = EntryType.TRANSFER_OUT, amount = cents, date = now, note = note, transferAccountId = to))
            dao.addEntry(LedgerEntry(accountId = to, type = EntryType.TRANSFER_IN, amount = cents, date = now, note = note, transferAccountId = from))
        }
        return null
    }

    // ---------- Envelopes ----------

    suspend fun addEnvelope(name: String, parentId: Long?): String? {
        if (name.isBlank()) return "Enter an envelope name."
        val acc = anchorAccountId() ?: return "Add an account first."
        dao.addEnvelope(Envelope(accountId = acc, parentId = parentId, name = name.trim()))
        return null
    }

    suspend fun renameEnvelope(env: Envelope, name: String): String? {
        if (name.isBlank()) return "Enter an envelope name."
        dao.updateEnvelope(env.copy(name = name.trim()))
        return null
    }

    /** Deleting an envelope releases whatever is left in it back to Ready to Assign. */
    suspend fun deleteEnvelope(env: Envelope) {
        val line = snapshot(YearMonth.now().plusMonths(600)).lines.firstOrNull { it.envelope.id == env.id }
        db.withTransaction {
            val leftover = line?.available ?: 0L
            if (leftover != 0L) {
                dao.addEntry(LedgerEntry(accountId = env.accountId, envelopeId = env.id, type = EntryType.ALLOCATION, amount = -leftover, note = "Released from ${env.name}"))
            }
            dao.children(env.id).forEach { dao.updateEnvelope(it.copy(parentId = null)) }
            dao.deleteEnvelope(env)
        }
    }

    // ---------- Assigning (the heart of zero-sum) ----------

    /** Set the amount assigned to an envelope for [month].
     *  Top-level envelopes are funded from Ready to Assign; child envelopes are funded from their parent envelope. */
    suspend fun setAssigned(envelopeId: Long, month: YearMonth, newAssigned: Long): String? {
        if (newAssigned < 0) return "Assigned amount cannot be negative."
        val acc = anchorAccountId() ?: return "Add an account first."
        val snap = snapshot(month)
        val line = snap.lines.firstOrNull { it.envelope.id == envelopeId } ?: return "Envelope not found."
        val delta = newAssigned - line.assigned
        if (delta == 0L) return null
        val date = assignmentDate(month)
        val parentId = line.envelope.parentId
        if (parentId != null) {
            val parent = snap.lines.firstOrNull { it.envelope.id == parentId } ?: return "Parent envelope not found."
            if (delta > 0 && parent.available < delta)
                return "${parent.envelope.name} only has ${money(parent.available.coerceAtLeast(0))} available. Lower the amount or move money into it first."
            if (delta < 0 && line.available + delta < 0)
                return "${line.envelope.name} only has ${money(line.available.coerceAtLeast(0))} to release back."
            db.withTransaction {
                dao.addEntry(LedgerEntry(accountId = acc, envelopeId = parentId, type = EntryType.ALLOCATION, amount = -delta, date = date, note = "To ${line.envelope.name}"))
                dao.addEntry(LedgerEntry(accountId = acc, envelopeId = envelopeId, type = EntryType.ALLOCATION, amount = delta, date = date, note = "From ${parent.envelope.name}"))
            }
        } else {
            if (delta > snap.readyToAssign) return "Only ${money(snap.readyToAssign)} is left to assign."
            if (delta < 0 && line.available + delta < 0) return "That would leave the envelope negative. Move money to cover spending instead."
            dao.addEntry(LedgerEntry(accountId = acc, envelopeId = envelopeId, type = EntryType.ALLOCATION, amount = delta, date = assignmentDate(month), note = "Assigned"))
        }
        return null
    }

    /** Move money between envelopes. A null side means "Ready to Assign". */
    suspend fun moveMoney(fromId: Long?, toId: Long?, cents: Long, month: YearMonth): String? {
        if (cents <= 0) return "Enter a positive amount."
        if (fromId == null && toId == null) return "Choose where the money should move."
        if (fromId == toId) return "Choose two different places."
        if (toId != null) {
            val toEnv = envelopes.value.firstOrNull { it.id == toId }
            if (toEnv != null && toEnv.parentId != null)
                return "Children are funded from their parent envelope. Choose the top-level parent or assign from it instead."
        }
        val acc = anchorAccountId() ?: return "Add an account first."
        val snap = snapshot(month)
        val date = assignmentDate(month)
        if (fromId == null) {
            if (cents > snap.readyToAssign) return "Only ${money(snap.readyToAssign)} is Ready to Assign."
        } else {
            val from = snap.lines.firstOrNull { it.envelope.id == fromId } ?: return "Envelope not found."
            if (cents > from.available) return "${from.envelope.name} only has ${money(from.available.coerceAtLeast(0))} available."
        }
        val fromName = fromId?.let { id -> envelopes.value.firstOrNull { it.id == id }?.name } ?: "Ready to Assign"
        val toName = toId?.let { id -> envelopes.value.firstOrNull { it.id == id }?.name } ?: "Ready to Assign"
        db.withTransaction {
            if (fromId != null) dao.addEntry(LedgerEntry(accountId = acc, envelopeId = fromId, type = EntryType.ALLOCATION, amount = -cents, date = date, note = "Moved to $toName"))
            if (toId != null) dao.addEntry(LedgerEntry(accountId = acc, envelopeId = toId, type = EntryType.ALLOCATION, amount = cents, date = date, note = "Moved from $fromName"))
        }
        return null
    }

    /** Repeat last month's assignments as far as money allows. Top-level envelopes draw from
     *  Ready to Assign; child envelopes draw from their parent. Returns how many envelopes were funded. */
    suspend fun assignLikeLastMonth(month: YearMonth): Int {
        val acc = anchorAccountId() ?: return 0
        val prev = snapshot(month.minusMonths(1)).lines.associate { it.envelope.id to it.assigned }
        val snap = snapshot(month)
        val date = assignmentDate(month)
        var ready = snap.readyToAssign
        var count = 0
        // Track each envelope's own available so parents used repeatedly in the loop stay correct.
        val ownAvailable = snap.lines.associate { it.envelope.id to it.available }.toMutableMap()
        val byId = snap.lines.associate { it.envelope.id to it }
        db.withTransaction {
            for (line in snap.lines) {
                val want = (prev[line.envelope.id] ?: 0L) - line.assigned
                if (want <= 0) continue
                val parentId = line.envelope.parentId
                if (parentId != null) {
                    val parent = byId[parentId] ?: continue
                    val pot = ownAvailable[parentId] ?: 0L
                    if (pot <= 0) continue
                    val give = minOf(want, pot)
                    dao.addEntry(LedgerEntry(accountId = acc, envelopeId = parentId, type = EntryType.ALLOCATION, amount = -give, date = date, note = "To ${line.envelope.name}"))
                    dao.addEntry(LedgerEntry(accountId = acc, envelopeId = line.envelope.id, type = EntryType.ALLOCATION, amount = give, date = date, note = "From ${parent.envelope.name}"))
                    ownAvailable[parentId] = pot - give
                    ownAvailable[line.envelope.id] = (ownAvailable[line.envelope.id] ?: 0L) + give
                    count++
                } else {
                    if (ready <= 0) continue
                    val give = minOf(want, ready)
                    dao.addEntry(LedgerEntry(accountId = acc, envelopeId = line.envelope.id, type = EntryType.ALLOCATION, amount = give, date = date, note = "Same as last month"))
                    ready -= give
                    ownAvailable[line.envelope.id] = (ownAvailable[line.envelope.id] ?: 0L) + give
                    count++
                }
            }
        }
        return count
    }

    // ---------- Transactions ----------

    suspend fun addIncome(name: String, cents: Long, accountId: Long?, note: String): String? {
        if (cents <= 0) return "Enter a positive amount."
        if (accountId == null) return "Choose an account."
        dao.addEntry(LedgerEntry(accountId = accountId, type = EntryType.INCOME, amount = cents, note = note.ifBlank { name }))
        return null
    }

    suspend fun addExpense(name: String, cents: Long, accountId: Long?, envelopeId: Long?, note: String): String? {
        if (cents <= 0) return "Enter a positive amount."
        if (accountId == null) return "Choose an account."
        if (envelopeId == null) return "Choose the envelope this money comes from."
        dao.addEntry(LedgerEntry(accountId = accountId, envelopeId = envelopeId, type = EntryType.EXPENSE, amount = cents, note = note.ifBlank { name }))
        return null
    }

    /** Edit an existing entry. Keeps the original date; enforces the same zero-sum rules as when adding. */
    suspend fun updateEntry(entry: LedgerEntry): String? {
        val old = entries.value.firstOrNull { it.id == entry.id } ?: return "Entry not found."
        if (entry.amount <= 0) return "Enter a positive amount."
        if (accounts.value.firstOrNull { it.id == entry.accountId } == null) return "Choose an account."
        val clean = when (entry.type) {
            EntryType.INCOME -> if (entry.envelopeId != null) return "Income has no envelope." else entry.copy(note = entry.note.trim(), envelopeId = null, transferAccountId = null)
            EntryType.EXPENSE -> if (entry.envelopeId == null) return "Choose the envelope this money comes from." else entry.copy(note = entry.note.trim().ifBlank { "Expense" }, transferAccountId = null)
            EntryType.ALLOCATION -> entry.copy(note = entry.note.trim(), transferAccountId = null)
            else -> entry.copy(note = entry.note.trim())
        }
        // Verify the whole budget stays balanced with the edited entry.
        val updated = entries.value.map { if (it.id == old.id) clean else it }
        val last = updated.maxOfOrNull { it.date }?.let { yearMonthOf(it) } ?: YearMonth.now()
        val m = if (last.isAfter(YearMonth.now())) last else YearMonth.now()
        if (computeSnapshot(accounts.value, envelopes.value, updated, m).readyToAssign < 0) {
            return "That would leave the budget over-assigned. Reduce some envelope assignments first."
        }
        dao.updateEntry(clean.copy(id = old.id, date = old.date))
        return null
    }

    suspend fun deleteEntry(entry: LedgerEntry): String? {
        if (entry.type == EntryType.INCOME && readyNow() - entry.amount < 0) {
            return "Deleting this income would leave you over-assigned. Reduce some envelope assignments first."
        }
        db.withTransaction {
            if (entry.type == EntryType.TRANSFER_IN || entry.type == EntryType.TRANSFER_OUT) {
                val opposite = if (entry.type == EntryType.TRANSFER_IN) EntryType.TRANSFER_OUT else EntryType.TRANSFER_IN
                entries.value.firstOrNull {
                    it.id != entry.id && it.type == opposite && it.amount == entry.amount &&
                        it.accountId == entry.transferAccountId && it.transferAccountId == entry.accountId &&
                        kotlin.math.abs(it.date - entry.date) < 5_000
                }?.let { dao.deleteEntry(it) }
            }
            dao.deleteEntry(entry)
        }
        return null
    }

    // ---------- Savings goals (funded from Ready to Assign) ----------

    suspend fun addGoal(name: String, targetCents: Long, note: String): String? {
        if (name.isBlank() || targetCents <= 0) return "Enter a goal name and a target amount."
        dao.addGoal(SavingsGoal(name = name.trim(), targetAmount = targetCents, note = note))
        return null
    }

    /** Positive [cents] moves money from Ready to Assign into the goal; negative releases it back. */
    suspend fun contributeToGoal(goal: SavingsGoal, cents: Long): String? {
        if (cents == 0L) return "Enter an amount."
        val acc = anchorAccountId() ?: return "Add an account first."
        if (cents > 0 && cents > readyNow()) return "Only ${money(readyNow())} is Ready to Assign."
        if (cents < 0 && -cents > goal.savedAmount) return "This goal only holds ${money(goal.savedAmount)}."
        db.withTransaction {
            dao.addEntry(LedgerEntry(accountId = acc, envelopeId = null, type = EntryType.ALLOCATION, amount = cents, note = "Goal: ${goal.name}"))
            dao.updateGoal(goal.copy(savedAmount = goal.savedAmount + cents))
        }
        return null
    }

    suspend fun deleteGoal(goal: SavingsGoal) {
        val acc = anchorAccountId()
        db.withTransaction {
            if (goal.savedAmount > 0 && acc != null) {
                dao.addEntry(LedgerEntry(accountId = acc, envelopeId = null, type = EntryType.ALLOCATION, amount = -goal.savedAmount, note = "Released goal: ${goal.name}"))
            }
            dao.deleteGoal(goal)
        }
    }

    // ---------- Recurring ----------

    suspend fun addRecurring(name: String, cents: Long, type: String, accountId: Long?, envelopeId: Long?, interval: String): String? {
        if (name.isBlank() || cents <= 0) return "Enter a description and an amount."
        if (accountId == null) return "Choose an account."
        if (type == EntryType.EXPENSE && envelopeId == null) return "Choose an envelope for this expense."
        val today = java.time.LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        dao.addRecurring(RecurringTransaction(name = name.trim(), amount = cents, type = type, accountId = accountId,
            envelopeId = if (type == EntryType.EXPENSE) envelopeId else null, interval = interval, nextDue = today))
        processRecurring()
        return null
    }

    suspend fun setRecurringActive(item: RecurringTransaction, active: Boolean) {
        dao.updateRecurring(item.copy(active = active))
        if (active) processRecurring()
    }

    suspend fun deleteRecurring(item: RecurringTransaction) = dao.deleteRecurring(item)

    /** Posts every recurring item that has come due (catching up on missed periods). */
    suspend fun processRecurring() {
        val now = System.currentTimeMillis()
        val due = dao.dueRecurring(now)
        if (due.isEmpty()) return
        db.withTransaction {
            for (r in due) {
                var next = r.nextDue
                var guard = 0
                while (next <= now && guard < 60) {
                    val type = if (r.type == EntryType.INCOME) EntryType.INCOME else EntryType.EXPENSE
                    val envStillExists = r.envelopeId?.let { dao.envelope(it) } != null
                    dao.addEntry(LedgerEntry(accountId = r.accountId, envelopeId = if (type == EntryType.EXPENSE && envStillExists) r.envelopeId else null,
                        type = type, amount = r.amount, date = next, note = "${r.name} (recurring)"))
                    next = nextDueAfter(next, r.interval)
                    guard++
                }
                dao.updateRecurring(r.copy(nextDue = next))
            }
        }
    }
}
