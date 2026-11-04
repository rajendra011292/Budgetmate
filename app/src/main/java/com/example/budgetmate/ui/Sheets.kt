package com.example.budgetmate.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.budgetmate.BudgetViewModel
import com.example.budgetmate.data.*
import java.time.YearMonth
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale

sealed interface Sheet {
    data object AddMenu : Sheet
    data class Transaction(val type: String, val envelopeId: Long? = null) : Sheet
    data object NewAccount : Sheet
    data class NewEnvelope(val parentId: Long? = null) : Sheet
    data class EditEntry(val entry: LedgerEntry) : Sheet
    data object Transfer : Sheet
    data object NewGoal : Sheet
    data class Contribute(val goal: SavingsGoal) : Sheet
    data class Assign(val envelopeId: Long) : Sheet
    data class Move(val fromId: Long?, val toId: Long?) : Sheet
    data object NewRecurring : Sheet
}

@Composable
fun SheetHost(vm: BudgetViewModel, sheet: Sheet?, month: YearMonth, open: (Sheet?) -> Unit) {
    if (sheet == null) return
    val accounts by vm.accounts.collectAsState()
    val envelopes by vm.envelopes.collectAsState()
    val entries by vm.entries.collectAsState()
    val close = { open(null) }

    when (sheet) {
        Sheet.AddMenu -> AddMenuSheet(accounts.isNotEmpty(), open, close)
        is Sheet.Transaction -> TransactionSheet(vm, sheet, accounts, envelopes, entries, close)
        Sheet.NewAccount -> AccountSheet(vm, close)
        is Sheet.NewEnvelope -> EnvelopeSheet(vm, sheet, envelopes, close)
        is Sheet.EditEntry -> EditEntrySheet(vm, sheet, accounts, envelopes, entries, close)
        Sheet.Transfer -> TransferSheet(vm, accounts, close)
        Sheet.NewGoal -> GoalSheet(vm, close)
        is Sheet.Contribute -> ContributeSheet(vm, sheet.goal, accounts, envelopes, entries, close)
        is Sheet.Assign -> AssignSheet(vm, sheet.envelopeId, month, accounts, envelopes, entries, open, close)
        is Sheet.Move -> MoveSheet(vm, sheet, month, accounts, envelopes, entries, close)
        Sheet.NewRecurring -> RecurringSheet(vm, accounts, envelopes, close)
    }
}

// ---------- Add menu ----------

@Composable
private fun AddMenuSheet(hasAccounts: Boolean, open: (Sheet?) -> Unit, close: () -> Unit) {
    BmSheet("What would you like to add?", close) {
        if (!hasAccounts) {
            Text("Start by adding an account that holds your money.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            PrimaryButton("Add your first account", { open(Sheet.NewAccount) })
            return@BmSheet
        }
        val tiles = listOf(
            Triple("💸", "Expense", Sheet.Transaction(EntryType.EXPENSE)),
            Triple("💰", "Income", Sheet.Transaction(EntryType.INCOME)),
            Triple("✉️", "Envelope", Sheet.NewEnvelope()),
            Triple("🔀", "Move money", Sheet.Move(null, null)),
            Triple("🔁", "Transfer", Sheet.Transfer),
            Triple("🏦", "Account", Sheet.NewAccount),
            Triple("🎯", "Savings goal", Sheet.NewGoal),
            Triple("🔄", "Recurring", Sheet.NewRecurring)
        )
        tiles.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { (emoji, label, target) ->
                    Surface(
                        onClick = { open(target) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(emoji, style = MaterialTheme.typography.headlineMedium)
                            Text(label, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}

// ---------- Transaction ----------

@Composable
private fun TransactionSheet(
    vm: BudgetViewModel, sheet: Sheet.Transaction,
    accounts: List<Account>, envelopes: List<Envelope>, entries: List<LedgerEntry>, close: () -> Unit
) {
    val form = rememberForm(close)
    var type by remember { mutableStateOf(sheet.type) }
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var accountId by remember { mutableStateOf(accounts.firstOrNull()?.id) }
    var envelopeId by remember { mutableStateOf(sheet.envelopeId) }
    val snap = remember(accounts, envelopes, entries) { computeSnapshot(accounts, envelopes, entries, YearMonth.now()) }
    val cents = parseAmount(amount)

    BmSheet(if (type == EntryType.INCOME) "Add income" else "Add expense", close,
        subtitle = if (type == EntryType.INCOME) "Income lands in Ready to Assign, so you can give it a job." else "Spending comes out of an envelope.") {
        ChipPicker("Type", listOf(EntryType.EXPENSE to "💸 Expense", EntryType.INCOME to "💰 Income"), type) { type = it }
        AmountField(amount, { amount = it })
        BmTextField(name, { name = it }, if (type == EntryType.INCOME) "Source (e.g. Salary)" else "What was it for?")
        ChipPicker("Account", accounts.map { it.id as Long? to it.name }, accountId) { accountId = it }
        if (type == EntryType.EXPENSE) {
            ChipPicker("Envelope", envelopes.map { it.id as Long? to "${emojiFor(it.name)} ${it.name}" }, envelopeId) { envelopeId = it }
            val line = snap.lines.firstOrNull { it.envelope.id == envelopeId }
            if (line != null && cents > line.available) {
                Text("⚠️ ${line.envelope.name} has ${money(line.available.coerceAtLeast(0))}. This will overspend by ${money(cents - line.available.coerceAtLeast(0))} — cover it afterwards by moving money.",
                    style = MaterialTheme.typography.bodyMedium, color = BM.Warn)
            }
            if (envelopes.isEmpty()) Text("Create an envelope first so spending has somewhere to come from.", style = MaterialTheme.typography.bodyMedium, color = BM.Warn)
        }
        ErrorText(form.error)
        PrimaryButton(if (form.busy) "Saving…" else "Save", enabled = !form.busy, onClick = {
            form.submit {
                if (type == EntryType.INCOME) vm.addIncome(name, cents, accountId, name)
                else vm.addExpense(name, cents, accountId, envelopeId, name)
            }
        })
    }
}

// ---------- Edit an existing entry (tapped in Activity) ----------

@Composable
private fun EditEntrySheet(
    vm: BudgetViewModel, sheet: Sheet.EditEntry,
    accounts: List<Account>, envelopes: List<Envelope>, entries: List<LedgerEntry>, close: () -> Unit
) {
    val e = entries.firstOrNull { it.id == sheet.entry.id }
    if (e == null) { LaunchedEffect(Unit) { close() }; return }
    val form = rememberForm(close)
    var amount by remember(e.id) { mutableStateOf(centsToInput(e.amount)) }
    var note by remember(e.id) { mutableStateOf(e.note) }
    var accountId by remember(e.id) { mutableStateOf<Long?>(e.accountId) }
    var envelopeId by remember(e.id) { mutableStateOf(e.envelopeId) }
    var confirmDelete by remember { mutableStateOf(false) }

    val isTransfer = e.type == EntryType.TRANSFER_IN || e.type == EntryType.TRANSFER_OUT
    val showEnvelope = e.type == EntryType.EXPENSE || e.type == EntryType.ALLOCATION
    val title = when (e.type) {
        EntryType.INCOME -> "💰 Edit income"
        EntryType.EXPENSE -> "💸 Edit expense"
        EntryType.ALLOCATION -> "📥 Edit assignment"
        else -> "🔁 Transfer"
    }
    val accountLabel = when (e.type) {
        EntryType.INCOME -> "Received in"
        EntryType.EXPENSE -> "Paid from"
        else -> "Account"
    }

    BmSheet(title, close, subtitle = "Recorded ${dayLabel(Instant.ofEpochMilli(e.date).atZone(java.time.ZoneId.systemDefault()).toLocalDate())} · ${money(e.amount)}") {
        if (isTransfer) {
            Text("Transfers can only be renamed or deleted — the amount and accounts move as a pair.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        AmountField(amount, { amount = it }, "Amount (was ${money(e.amount)})")
        BmTextField(note, { note = it }, if (e.type == EntryType.INCOME) "Source or note" else "Note")
        if (!isTransfer) {
            BmDropdown(accountLabel, accounts.map { it.id as Long? to it.name }, accountId) { accountId = it }
        }
        if (showEnvelope) {
            val label = if (e.type == EntryType.ALLOCATION) "Assign to" else "Envelope"
            val options = if (e.type == EntryType.ALLOCATION)
                listOf<Pair<Long?, String>>(null to "🎯 Goals / Ready to Assign") + envelopes.map { it.id as Long? to "${emojiFor(it.name)} ${it.name}" }
            else
                envelopes.map { it.id as Long? to "${emojiFor(it.name)} ${it.name}" }
            BmDropdown(label, options, envelopeId) { envelopeId = it }
        }
        ErrorText(form.error)
        if (!isTransfer) {
            PrimaryButton(if (form.busy) "Saving…" else "Save changes", enabled = !form.busy, onClick = {
                form.submit { vm.updateEntry(
                    e.copy(amount = parseAmount(amount), note = note, accountId = accountId ?: e.accountId,
                        envelopeId = if (showEnvelope) envelopeId else e.envelopeId)
                ) }
            })
        } else {
            PrimaryButton(if (form.busy) "Saving…" else "Save", enabled = !form.busy, onClick = {
                form.submit { vm.updateEntry(e.copy(note = note)) }
            })
        }
        SecondaryButton(
            if (confirmDelete) "Tap again — this removes the entry" else "Delete this entry",
            onClick = { if (confirmDelete) form.submit { vm.deleteEntry(e) } else confirmDelete = true },
            danger = true
        )
    }
}

// ---------- Account / Envelope / Transfer / Goal ----------

@Composable
private fun AccountSheet(vm: BudgetViewModel, close: () -> Unit) {
    val form = rememberForm(close)
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    BmSheet("Add account", close, subtitle = "Opening money becomes Ready to Assign.") {
        BmTextField(name, { name = it }, "Account name (e.g. HDFC Savings, Cash)")
        AmountField(amount, { amount = it }, "Current balance")
        ErrorText(form.error)
        PrimaryButton(if (form.busy) "Saving…" else "Add account", enabled = !form.busy, onClick = {
            form.submit { vm.addAccount(name, parseAmount(amount)) }
        })
    }
}

@Composable
private fun EnvelopeSheet(vm: BudgetViewModel, sheet: Sheet.NewEnvelope, envelopes: List<Envelope>, close: () -> Unit) {
    val form = rememberForm(close)
    var name by remember { mutableStateOf("") }
    var parentId by remember { mutableStateOf(sheet.parentId) }
    BmSheet("New envelope", close, subtitle = "A job for your money, like Rent, Groceries or Fun.") {
        BmTextField(name, { name = it }, "Envelope name")
        if (name.isNotBlank()) Text("${emojiFor(name)}  ${name.trim()}", style = MaterialTheme.typography.titleMedium)
        BmDropdown("Inside (optional)", listOf<Pair<Long?, String>>(null to "💶 Top level (no parent)") + envelopes.filter { it.parentId == null }.map { it.id as Long? to "${emojiFor(it.name)} ${it.name}" }, parentId) { parentId = it }
        ErrorText(form.error)
        PrimaryButton(if (form.busy) "Saving…" else "Create envelope", enabled = !form.busy, onClick = {
            form.submit { vm.addEnvelope(name, parentId) }
        })
    }
}

@Composable
private fun TransferSheet(vm: BudgetViewModel, accounts: List<Account>, close: () -> Unit) {
    val form = rememberForm(close)
    var from by remember { mutableStateOf(accounts.getOrNull(0)?.id) }
    var to by remember { mutableStateOf(accounts.getOrNull(1)?.id) }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    BmSheet("Transfer between accounts", close, subtitle = "Moving money between accounts doesn't change your budget.") {
        AmountField(amount, { amount = it })
        ChipPicker("From", accounts.map { it.id as Long? to it.name }, from) { from = it }
        ChipPicker("To", accounts.filter { it.id != from }.map { it.id as Long? to it.name }, to) { to = it }
        BmTextField(note, { note = it }, "Note (optional)")
        ErrorText(form.error)
        PrimaryButton(if (form.busy) "Saving…" else "Transfer", enabled = !form.busy, onClick = {
            form.submit { vm.transfer(from, to, parseAmount(amount), note) }
        })
    }
}

@Composable
private fun GoalSheet(vm: BudgetViewModel, close: () -> Unit) {
    val form = rememberForm(close)
    var name by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    BmSheet("New savings goal", close, subtitle = "Fund it from Ready to Assign whenever you can.") {
        BmTextField(name, { name = it }, "Goal name (e.g. Emergency fund)")
        AmountField(target, { target = it }, "Target amount")
        BmTextField(note, { note = it }, "Note (optional)")
        ErrorText(form.error)
        PrimaryButton(if (form.busy) "Saving…" else "Create goal", enabled = !form.busy, onClick = {
            form.submit { vm.addGoal(name, parseAmount(target), note) }
        })
    }
}

@Composable
private fun ContributeSheet(
    vm: BudgetViewModel, goal: SavingsGoal,
    accounts: List<Account>, envelopes: List<Envelope>, entries: List<LedgerEntry>, close: () -> Unit
) {
    val form = rememberForm(close)
    var add by remember { mutableStateOf(true) }
    var amount by remember { mutableStateOf("") }
    val ready = remember(accounts, envelopes, entries) { computeSnapshot(accounts, envelopes, entries, YearMonth.now()).readyToAssign }
    BmSheet(goal.name, close, subtitle = "Saved ${money(goal.savedAmount)} of ${money(goal.targetAmount)} · Ready to Assign ${money(ready)}") {
        ChipPicker("Action", listOf(true to "Add money", false to "Take back"), add) { add = it }
        AmountField(amount, { amount = it })
        if (add && ready > 0) {
            SecondaryButton("Use all Ready to Assign (${money(ready)})", { amount = centsToInput(ready) })
        }
        ErrorText(form.error)
        PrimaryButton(if (form.busy) "Saving…" else if (add) "Add to goal" else "Release to Ready to Assign", enabled = !form.busy, onClick = {
            val c = parseAmount(amount)
            form.submit { vm.contributeToGoal(goal, if (add) c else -c) }
        })
    }
}

// ---------- Assign (zero-sum core) ----------

@Composable
private fun AssignSheet(
    vm: BudgetViewModel, envelopeId: Long, month: YearMonth,
    accounts: List<Account>, envelopes: List<Envelope>, entries: List<LedgerEntry>,
    open: (Sheet?) -> Unit, close: () -> Unit
) {
    val snap = remember(accounts, envelopes, entries, month) { computeSnapshot(accounts, envelopes, entries, month) }
    val line = snap.lines.firstOrNull { it.envelope.id == envelopeId }
    if (line == null) { LaunchedEffect(Unit) { close() }; return }
    val form = rememberForm(close)
    var name by remember { mutableStateOf(line.envelope.name) }
    var amount by remember { mutableStateOf(centsToInput(line.assigned)) }
    var confirmDelete by remember { mutableStateOf(false) }
    val monthLabel = month.format(DateTimeFormatter.ofPattern("MMMM", Locale.getDefault()))
    val newAssigned = parseAmount(amount)
    val after = snap.readyToAssign - (newAssigned - line.assigned)

    BmSheet("${emojiFor(line.envelope.name)} ${line.envelope.name}", close,
        subtitle = "Available ${money(line.available)} · Spent in $monthLabel ${money(line.activity)}") {
        AmountField(amount, { amount = it }, "Assigned in $monthLabel")
        Text(
            when {
                after < 0 -> "That's ${money(-after)} more than you have. Lower the amount."
                after == 0L -> "Perfect — every rupee has a job."
                else -> "Ready to Assign after this: ${money(after)}"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = if (after < 0) BM.Bad else if (after == 0L) BM.Good else MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (snap.readyToAssign > 0) {
            SecondaryButton("Assign all remaining (${money(snap.readyToAssign)})", { amount = centsToInput(line.assigned + snap.readyToAssign) })
        }
        BmTextField(name, { name = it }, "Envelope name")
        ErrorText(form.error)
        PrimaryButton(if (form.busy) "Saving…" else "Save", enabled = !form.busy, onClick = {
            form.submit {
                val renamed = if (name.trim() != line.envelope.name) vm.renameEnvelope(line.envelope, name) else null
                renamed ?: vm.setAssigned(envelopeId, month, newAssigned)
            }
        })
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SecondaryButton("Move money", { open(Sheet.Move(null, envelopeId)) }, Modifier.weight(1f))
            SecondaryButton("Add expense", { open(Sheet.Transaction(EntryType.EXPENSE, envelopeId)) }, Modifier.weight(1f))
        }
        SecondaryButton("Add child envelope", { open(Sheet.NewEnvelope(envelopeId)) }, Modifier.fillMaxWidth())
        SecondaryButton(
            if (confirmDelete) "Tap again — remaining money returns to Ready to Assign" else "Delete envelope",
            onClick = { if (confirmDelete) form.submit { vm.deleteEnvelope(line.envelope); null } else confirmDelete = true },
            danger = true
        )
    }
}

// ---------- Move money ----------

@Composable
private fun MoveSheet(
    vm: BudgetViewModel, sheet: Sheet.Move, month: YearMonth,
    accounts: List<Account>, envelopes: List<Envelope>, entries: List<LedgerEntry>, close: () -> Unit
) {
    val snap = remember(accounts, envelopes, entries, month) { computeSnapshot(accounts, envelopes, entries, month) }
    val form = rememberForm(close)
    var from by remember { mutableStateOf(sheet.fromId) }
    var to by remember { mutableStateOf(sheet.toId) }
    var amount by remember {
        val target = snap.lines.firstOrNull { it.envelope.id == sheet.toId }
        mutableStateOf(if (target != null && target.available < 0) centsToInput(-target.available) else "")
    }
    val fromOptions = listOf<Pair<Long?, String>>(null to "💵 Ready to Assign · ${money(snap.readyToAssign)}") +
        snap.lines.filter { it.envelope.id != to }.map { it.envelope.id as Long? to "${emojiFor(it.envelope.name)} ${it.envelope.name} · ${money(it.available)}" }
    val toOptions = listOf<Pair<Long?, String>>(null to "💵 Ready to Assign") +
        snap.lines.filter { it.envelope.id != from }.map { it.envelope.id as Long? to "${emojiFor(it.envelope.name)} ${it.envelope.name} · ${money(it.available)}" }

    BmSheet("Move money", close, subtitle = "Re-balance without changing the total. Great for covering overspending.") {
        AmountField(amount, { amount = it })
        ChipPicker("From", fromOptions, from) { from = it }
        ChipPicker("To", toOptions, to) { to = it }
        ErrorText(form.error)
        PrimaryButton(if (form.busy) "Moving…" else "Move", enabled = !form.busy, onClick = {
            form.submit { vm.moveMoney(from, to, parseAmount(amount), month) }
        })
    }
}

// ---------- Recurring ----------

@Composable
private fun RecurringSheet(vm: BudgetViewModel, accounts: List<Account>, envelopes: List<Envelope>, close: () -> Unit) {
    val form = rememberForm(close)
    var type by remember { mutableStateOf(EntryType.EXPENSE) }
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var accountId by remember { mutableStateOf(accounts.firstOrNull()?.id) }
    var envelopeId by remember { mutableStateOf<Long?>(null) }
    var interval by remember { mutableStateOf("MONTHLY") }
    BmSheet("Recurring transaction", close, subtitle = "Posts automatically starting today, then on schedule.") {
        ChipPicker("Type", listOf(EntryType.EXPENSE to "💸 Expense", EntryType.INCOME to "💰 Income"), type) { type = it }
        BmTextField(name, { name = it }, "Description (e.g. Rent, Salary)")
        AmountField(amount, { amount = it })
        ChipPicker("Account", accounts.map { it.id as Long? to it.name }, accountId) { accountId = it }
        if (type == EntryType.EXPENSE) {
            ChipPicker("Envelope", envelopes.map { it.id as Long? to "${emojiFor(it.name)} ${it.name}" }, envelopeId) { envelopeId = it }
        }
        ChipPicker("Repeats", listOf("WEEKLY" to "Weekly", "MONTHLY" to "Monthly", "YEARLY" to "Yearly"), interval) { interval = it }
        ErrorText(form.error)
        PrimaryButton(if (form.busy) "Saving…" else "Save", enabled = !form.busy, onClick = {
            form.submit { vm.addRecurring(name, parseAmount(amount), type, accountId, envelopeId, interval) }
        })
    }
}
