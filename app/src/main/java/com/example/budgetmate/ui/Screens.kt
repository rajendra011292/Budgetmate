package com.example.budgetmate.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budgetmate.data.*
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val listPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 110.dp)

@Composable
private fun ScreenHeader(title: String, subtitle: String) {
    Column(Modifier.padding(bottom = 4.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// =====================================================================
// Accounts
// =====================================================================

@Composable
fun AccountsScreen(snap: BudgetSnapshot, accounts: List<Account>, open: (Sheet?) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(modifier.fillMaxSize(), contentPadding = listPadding, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { ScreenHeader("Accounts", "Where your money lives") }
        item {
            GradientCard(BM.heroBrush) {
                Text("TOTAL BALANCE", color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.labelMedium, letterSpacing = 1.2.sp)
                Text(money(snap.totalBalance), color = Color.White, style = MaterialTheme.typography.displaySmall)
                Text("${accounts.size} account${if (accounts.size == 1) "" else "s"}", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodyMedium)
            }
        }
        if (accounts.size >= 2) {
            item { FilledTonalButton(onClick = { open(Sheet.Transfer) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) { Text("🔁  Transfer between accounts") } }
        }
        if (accounts.isEmpty()) {
            item {
                BmCard {
                    EmptyState("🏦", "No accounts yet", "Add the places your money sits — bank accounts, wallets, cash.")
                    PrimaryButton("Add account", { open(Sheet.NewAccount) })
                }
            }
        }
        items(accounts, key = { it.id }) { a ->
            BmCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    EmojiAvatar("🏦", BM.palette[colorIndexFor(a.id)])
                    Column(Modifier.weight(1f)) {
                        Text(a.name, style = MaterialTheme.typography.titleMedium)
                        Text("Opened with ${money(a.openingBalance)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(money(snap.accountBalances[a.id] ?: a.openingBalance), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

// =====================================================================
// Activity
// =====================================================================

private enum class Filter(val label: String) { ALL("All"), SPENDING("Spending"), INCOME("Income"), ASSIGNED("Assigned"), TRANSFERS("Transfers") }

@Composable
fun ActivityScreen(
    entries: List<LedgerEntry>, accounts: List<Account>, envelopes: List<Envelope>,
    onEdit: (LedgerEntry) -> Unit, modifier: Modifier = Modifier
) {
    var filter by remember { mutableStateOf(Filter.ALL) }
    val zone = ZoneId.systemDefault()
    val filtered = entries.filter {
        when (filter) {
            Filter.ALL -> true
            Filter.SPENDING -> it.type == EntryType.EXPENSE
            Filter.INCOME -> it.type == EntryType.INCOME
            Filter.ASSIGNED -> it.type == EntryType.ALLOCATION
            Filter.TRANSFERS -> it.type == EntryType.TRANSFER_IN || it.type == EntryType.TRANSFER_OUT
        }
    }
    val grouped = filtered.groupBy { Instant.ofEpochMilli(it.date).atZone(zone).toLocalDate() }

    LazyColumn(modifier.fillMaxSize(), contentPadding = listPadding, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { ScreenHeader("Activity", "Everything that moved your money") }
        item {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Filter.values().forEach { f ->
                    FilterChip(selected = filter == f, onClick = { filter = f }, label = { Text(f.label) }, shape = RoundedCornerShape(14.dp))
                }
            }
        }
        if (filtered.isEmpty()) {
            item { EmptyState("🧾", "Nothing here yet", "Transactions you add will show up in this list.") }
        }
        grouped.forEach { (day, list) ->
            item(key = "d$day") {
                Text(dayLabel(day), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
            }
            items(list, key = { it.id }) { e -> EntryRow(e, accounts, envelopes) { onEdit(e) } }
        }
    }
}

fun dayLabel(d: LocalDate): String {
    val today = LocalDate.now()
    return when (d) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> d.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.getDefault()))
    }
}

@Composable
private fun EntryRow(e: LedgerEntry, accounts: List<Account>, envelopes: List<Envelope>, onClick: () -> Unit) {
    val account = accounts.firstOrNull { it.id == e.accountId }?.name ?: ""
    val env = envelopes.firstOrNull { it.id == e.envelopeId }
    val other = accounts.firstOrNull { it.id == e.transferAccountId }?.name ?: ""

    val emoji: String; val title: String; val sub: String; val amountText: String; val amountColor: Color
    when (e.type) {
        EntryType.INCOME -> { emoji = "💰"; title = e.note.ifBlank { "Income" }; sub = "$account · to Ready to Assign"; amountText = "+${money(e.amount)}"; amountColor = BM.Good }
        EntryType.EXPENSE -> { emoji = env?.let { emojiFor(it.name) } ?: "💸"; title = e.note.ifBlank { "Expense" }; sub = "$account · ${env?.name ?: "No envelope"}"; amountText = "−${money(e.amount)}"; amountColor = MaterialTheme.colorScheme.onSurface }
        EntryType.ALLOCATION -> {
            emoji = if (e.amount >= 0) "📥" else "📤"
            title = e.note.ifBlank { "Assigned" }
            sub = env?.name ?: "Goals / Ready to Assign"
            amountText = (if (e.amount >= 0) "+" else "−") + money(kotlin.math.abs(e.amount))
            amountColor = MaterialTheme.colorScheme.primary
        }
        EntryType.TRANSFER_OUT -> { emoji = "🔁"; title = "Transfer to $other"; sub = e.note.ifBlank { account }; amountText = "−${money(e.amount)}"; amountColor = MaterialTheme.colorScheme.onSurfaceVariant }
        else -> { emoji = "🔁"; title = "Transfer from $other"; sub = e.note.ifBlank { account }; amountText = "+${money(e.amount)}"; amountColor = MaterialTheme.colorScheme.onSurfaceVariant }
    }
    BmCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            EmojiAvatar(emoji, amountColor, 40)
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(sub, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            Text(amountText, style = MaterialTheme.typography.titleMedium, color = amountColor)
        }
    }
}

// =====================================================================
// Goals
// =====================================================================

@Composable
fun GoalsScreen(
    goals: List<SavingsGoal>, ready: Long, open: (Sheet?) -> Unit,
    onDelete: (SavingsGoal) -> Unit, modifier: Modifier = Modifier
) {
    var toDelete by remember { mutableStateOf<SavingsGoal?>(null) }
    LazyColumn(modifier.fillMaxSize(), contentPadding = listPadding, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { ScreenHeader("Goals", "Fund them from Ready to Assign (${money(ready)} available)") }
        if (goals.isEmpty()) {
            item {
                BmCard {
                    EmptyState("🎯", "No savings goals yet", "Create a goal like an emergency fund or a trip, then fund it from money you haven't assigned.")
                    PrimaryButton("Create goal", { open(Sheet.NewGoal) })
                }
            }
        }
        items(goals, key = { it.id }) { g ->
            val progress = if (g.targetAmount > 0) (g.savedAmount.toFloat() / g.targetAmount).coerceIn(0f, 1f) else 0f
            val done = g.savedAmount >= g.targetAmount
            BmCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { progress }, modifier = Modifier.size(68.dp), strokeWidth = 7.dp,
                            color = if (done) BM.Good else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        )
                        Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelLarge)
                    }
                    Column(Modifier.weight(1f)) {
                        Text(g.name + if (done) " ✅" else "", style = MaterialTheme.typography.titleMedium)
                        Text("${money(g.savedAmount)} of ${money(g.targetAmount)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (g.note.isNotBlank()) Text(g.note, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilledTonalButton(onClick = { open(Sheet.Contribute(g)) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) { Text("Add / take back") }
                    IconButton(onClick = { toDelete = g }) { Icon(Icons.Default.Delete, "Delete goal", tint = MaterialTheme.colorScheme.error) }
                }
            }
        }
    }
    toDelete?.let { g ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Delete ${g.name}?") },
            text = { Text(if (g.savedAmount > 0) "${money(g.savedAmount)} will return to Ready to Assign." else "This goal has no money in it.") },
            confirmButton = { TextButton(onClick = { onDelete(g); toDelete = null }) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancel") } }
        )
    }
}

// =====================================================================
// Reports
// =====================================================================

@Composable
fun ReportsScreen(
    snap: BudgetSnapshot, entries: List<LedgerEntry>, recurring: List<RecurringTransaction>, envelopes: List<Envelope>,
    month: YearMonth, onMonth: (YearMonth) -> Unit, open: (Sheet?) -> Unit,
    onToggleRecurring: (RecurringTransaction, Boolean) -> Unit, onDeleteRecurring: (RecurringTransaction) -> Unit,
    modifier: Modifier = Modifier
) {
    val spending = snap.lines.filter { it.activity > 0 }.sortedByDescending { it.activity }
    val months = (5 downTo 0).map { month.minusMonths(it.toLong()) }
    val incomeBy = months.map { m -> entries.filter { it.type == EntryType.INCOME && yearMonthOf(it.date) == m }.sumOf { it.amount } }
    val spentBy = months.map { m -> entries.filter { it.type == EntryType.EXPENSE && yearMonthOf(it.date) == m }.sumOf { it.amount } }
    val chartMax = maxOf(incomeBy.maxOrNull() ?: 0L, spentBy.maxOrNull() ?: 0L, 1L).toFloat()

    LazyColumn(modifier.fillMaxSize(), contentPadding = listPadding, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Reports", style = MaterialTheme.typography.headlineMedium)
                    Text("How your plan is holding up", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                MonthSwitcher(month, onMonth)
            }
        }

        // Zero-sum health
        item {
            val ready = snap.readyToAssign
            val (emoji, title, text, color) = when {
                ready < 0 -> Quad("🚨", "Over-assigned", "You've given out ${money(-ready)} you don't have.", BM.Bad)
                snap.overspent.isNotEmpty() -> Quad("⚠️", "Overspent", "${snap.overspent.size} envelope(s) are below zero.", BM.Warn)
                ready == 0L -> Quad("✅", "Zero-sum achieved", "Every rupee has a job.", BM.Good)
                else -> Quad("💡", "${money(ready)} still unassigned", "Give it a job to reach zero.", MaterialTheme.colorScheme.primary)
            }
            BmCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    EmojiAvatar(emoji, color)
                    Column {
                        Text(title, style = MaterialTheme.typography.titleMedium, color = color)
                        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("Money in accounts ${money(snap.totalBalance)} = Ready ${money(ready)} + Envelopes ${money(snap.envelopeTotal)} + Goals ${money(snap.goalReserve)}",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Income vs spending this month
        item {
            BmCard {
                Text("This month", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(10.dp))
                val top = maxOf(snap.monthIncome, snap.monthSpent, 1L).toFloat()
                BarLine("Income", snap.monthIncome, snap.monthIncome / top, BM.Good)
                Spacer(Modifier.height(8.dp))
                BarLine("Spending", snap.monthSpent, snap.monthSpent / top, BM.Bad)
                Spacer(Modifier.height(10.dp))
                val net = snap.monthIncome - snap.monthSpent
                Text("Net ${if (net >= 0) "+" else "−"}${money(kotlin.math.abs(net))}", style = MaterialTheme.typography.titleMedium, color = if (net >= 0) BM.Good else BM.Bad)
            }
        }

        // 6-month chart
        item {
            BmCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Last 6 months", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Legend2("Income", BM.Good); Spacer(Modifier.width(10.dp)); Legend2("Spent", BM.Bad)
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth().height(150.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    months.forEachIndexed { i, m ->
                        Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.Bottom) {
                                Bar(incomeBy[i] / chartMax, BM.Good, Modifier.weight(1f))
                                Bar(spentBy[i] / chartMax, BM.Bad, Modifier.weight(1f))
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(m.format(DateTimeFormatter.ofPattern("MMM", Locale.getDefault())), style = MaterialTheme.typography.labelSmall,
                                color = if (m == month) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }

        // Spending by envelope
        item { SectionTitle("Spending by envelope") }
        if (spending.isEmpty()) {
            item { BmCard { EmptyState("📊", "No spending this month", "Expenses you record will be broken down here.") } }
        } else {
            item {
                BmCard {
                    spending.forEachIndexed { i, l ->
                        if (i > 0) Spacer(Modifier.height(12.dp))
                        val share = l.activity.toFloat() / snap.monthSpent.coerceAtLeast(1L)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${emojiFor(l.envelope.name)} ${l.envelope.name}", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            Text("${money(l.activity)} · ${(share * 100).toInt()}%", style = MaterialTheme.typography.labelLarge)
                        }
                        Spacer(Modifier.height(5.dp))
                        Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                            Box(Modifier.fillMaxWidth(share.coerceIn(0.02f, 1f)).fillMaxHeight().clip(RoundedCornerShape(50)).background(BM.palette[colorIndexFor(l.envelope.id)]))
                        }
                    }
                }
            }
        }

        // Recurring
        item {
            SectionTitle("Recurring") { TextButton(onClick = { open(Sheet.NewRecurring) }) { Text("+ Add") } }
        }
        if (recurring.isEmpty()) {
            item { BmCard { EmptyState("🔄", "Nothing recurring", "Add rent, salary or subscriptions and they'll post on their own.") } }
        }
        items(recurring, key = { it.id }) { r ->
            val env = envelopes.firstOrNull { it.id == r.envelopeId }
            BmCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    EmojiAvatar(if (r.type == EntryType.INCOME) "💰" else (env?.let { emojiFor(it.name) } ?: "💸"), if (r.type == EntryType.INCOME) BM.Good else BM.Bad, 40)
                    Column(Modifier.weight(1f)) {
                        Text(r.name, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                        Text("${r.interval.lowercase().replaceFirstChar { it.uppercase() }} · next ${Instant.ofEpochMilli(r.nextDue).atZone(ZoneId.systemDefault()).toLocalDate().format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()))}",
                            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(money(r.amount), style = MaterialTheme.typography.titleMedium)
                    Switch(checked = r.active, onCheckedChange = { onToggleRecurring(r, it) })
                    IconButton(onClick = { onDeleteRecurring(r) }) { Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error) }
                }
            }
        }
    }
}

private data class Quad(val a: String, val b: String, val c: String, val d: Color)

@Composable
private fun BarLine(label: String, value: Long, fraction: Float, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(78.dp))
        Box(Modifier.weight(1f).height(12.dp).clip(RoundedCornerShape(50)).background(color.copy(alpha = 0.15f))) {
            Box(Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).fillMaxHeight().clip(RoundedCornerShape(50)).background(color))
        }
        Text(money(value), style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(start = 10.dp))
    }
}

@Composable
private fun Bar(fraction: Float, color: Color, modifier: Modifier) {
    Box(modifier.fillMaxHeight(fraction.coerceIn(0.02f, 1f)).clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)).background(color))
}

@Composable
private fun Legend2(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(color))
        Text(text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
