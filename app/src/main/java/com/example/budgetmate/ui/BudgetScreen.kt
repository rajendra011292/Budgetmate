package com.example.budgetmate.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budgetmate.data.*
import java.time.YearMonth

@Composable
fun BudgetScreen(
    snap: BudgetSnapshot,
    month: YearMonth,
    onMonth: (YearMonth) -> Unit,
    open: (Sheet?) -> Unit,
    onCopyLast: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lines = snap.lines
    val topLevel = lines.filter { it.envelope.parentId == null }
    val byParent = lines.filter { it.envelope.parentId != null }.groupBy { it.envelope.parentId }

    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Budget", style = MaterialTheme.typography.headlineMedium)
                    Text("Give every rupee a job", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                MonthSwitcher(month, onMonth)
            }
        }
        item { HeroCard(snap, lines.isNotEmpty()) }

        if (snap.overspent.isNotEmpty()) {
            item {
                val worst = snap.overspent.minByOrNull { it.available }!!
                Surface(shape = RoundedCornerShape(20.dp), color = BM.Bad.copy(alpha = 0.10f)) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("⚠️", fontSize = 24.sp)
                        Column(Modifier.weight(1f)) {
                            Text("${worst.envelope.name} is overspent by ${money(-worst.available)}", style = MaterialTheme.typography.titleMedium)
                            Text("Move money from another envelope to cover it.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        FilledTonalButton(onClick = { open(Sheet.Move(null, worst.envelope.parentId ?: worst.envelope.id)) }) { Text("Cover") }
                    }
                }
            }
        }

        if (lines.isNotEmpty()) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilledTonalButton(onClick = onCopyLast, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp)) { Text("Same as last month") }
                    FilledTonalButton(onClick = { open(Sheet.Move(null, null)) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp)) { Text("Move money") }
                }
            }
            item { SectionTitle("Envelopes") }
            topLevel.forEach { top ->
                val kids = byParent[top.envelope.id].orEmpty()
                item(key = "e${top.envelope.id}") { EnvelopeRow(top, 0, kids) { open(Sheet.Assign(top.envelope.id)) } }
                if (kids.isNotEmpty()) {
                    items(kids, key = { "e${it.envelope.id}" }) { k ->
                        EnvelopeRow(k, 22) { open(Sheet.Assign(k.envelope.id)) }
                    }
                }
            }
            // Orphans whose parent no longer exists (defensive)
            val knownTop = topLevel.map { it.envelope.id }.toSet()
            val orphans = byParent.filterKeys { it !in knownTop }.values.flatten()
            if (orphans.isNotEmpty()) {
                items(orphans, key = { "o${it.envelope.id}" }) { k -> EnvelopeRow(k, 0) { open(Sheet.Assign(k.envelope.id)) } }
            }
            item {
                TextButton(onClick = { open(Sheet.NewEnvelope()) }, modifier = Modifier.fillMaxWidth()) { Text("+ Add envelope") }
            }
        } else {
            item {
                BmCard {
                    EmptyState("✉️", "Create your first envelope", "Envelopes tell your money what to do — Rent, Groceries, Fun. Add one, then assign money to it.")
                    PrimaryButton("Add envelope", { open(Sheet.NewEnvelope()) })
                }
            }
        }
    }
}

@Composable
private fun HeroCard(snap: BudgetSnapshot, hasEnvelopes: Boolean) {
    val ready = snap.readyToAssign
    val over = snap.overspent
    val hasMoney = snap.totalBalance > 0 || snap.envelopeTotal > 0 || snap.goalReserve > 0
    val brush: Brush
    val headline: String
    val sub: String
    when {
        ready < 0 -> { brush = BM.badBrush; headline = "Over-assigned"; sub = "You've assigned ${money(-ready)} more than you have. Lower some envelopes." }
        over.isNotEmpty() -> { brush = BM.warnBrush; headline = "Ready to Assign"; sub = "${over.size} overspent envelope${if (over.size > 1) "s" else ""} to cover." }
        ready == 0L && hasMoney -> { brush = BM.goodBrush; headline = "Ready to Assign"; sub = "Every rupee has a job 🎉" }
        ready > 0 -> { brush = BM.heroBrush; headline = "Ready to Assign"; sub = if (hasEnvelopes) "Assign it to envelopes until this hits ₹0." else "Create envelopes, then assign this money." }
        else -> { brush = BM.heroBrush; headline = "Ready to Assign"; sub = "Add an account and income to get started." }
    }
    GradientCard(brush) {
        Text(headline.uppercase(), color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.labelMedium, letterSpacing = 1.2.sp)
        Text(money(ready), color = Color.White, style = MaterialTheme.typography.displaySmall)
        Text(sub, color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth()) {
            HeroStat("Income", money(snap.monthIncome), Modifier.weight(1f))
            HeroStat("Assigned", money(snap.monthAssigned), Modifier.weight(1f))
            HeroStat("Spent", money(snap.monthSpent), Modifier.weight(1f))
        }
        val total = (maxOf(ready, 0) + maxOf(snap.envelopeTotal, 0) + maxOf(snap.goalReserve, 0)).toFloat()
        if (total > 0f) {
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.15f))) {
                val parts = listOf(
                    maxOf(snap.envelopeTotal, 0L) to Color.White,
                    maxOf(snap.goalReserve, 0L) to Color(0xFFFDE68A),
                    maxOf(ready, 0L) to Color.White.copy(alpha = 0.35f)
                )
                parts.forEach { (v, c) -> if (v > 0) Box(Modifier.weight(v / total).fillMaxHeight().background(c)) }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Legend("Envelopes ${moneyShort(snap.envelopeTotal)}", Color.White)
                Legend("Goals ${moneyShort(snap.goalReserve)}", Color(0xFFFDE68A))
                Legend("Ready ${moneyShort(ready)}", Color.White.copy(alpha = 0.45f))
            }
        }
    }
}

@Composable
private fun HeroStat(label: String, value: String, modifier: Modifier) {
    Column(modifier) {
        Text(label, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
        Text(value, color = Color.White, style = MaterialTheme.typography.titleMedium, maxLines = 1)
    }
}

@Composable
private fun Legend(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(color))
        Text(text, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun EnvelopeRow(line: EnvelopeLine, indent: Int, children: List<EnvelopeLine> = emptyList(), onClick: () -> Unit) {
    val e = line.envelope
    val color = BM.palette[colorIndexFor(e.id)]
    // Parents with children show the family total: parent + children money and spending.
    val avail = line.available + children.sumOf { it.available }
    val activity = line.activity + children.sumOf { it.activity }
    val availColor = when {
        avail < 0 -> BM.Bad
        avail == 0L -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> BM.Good
    }
    val base = avail + activity
    val ratio = when {
        avail < 0 -> 1f
        base > 0 -> (activity.toFloat() / base).coerceIn(0f, 1f)
        else -> 0f
    }
    Box(Modifier.padding(start = indent.dp)) {
        BmCard(onClick = onClick) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EmojiAvatar(emojiFor(e.name), color)
                Column(Modifier.weight(1f)) {
                    Text(e.name + if (children.isNotEmpty()) "  💰${children.size}" else "", style = MaterialTheme.typography.titleMedium, maxLines = 1)
                    Text(
                        "Assigned ${money(line.assigned)} · Spent ${money(activity)}" + if (children.isNotEmpty()) " (incl. children)" else "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1
                    )
                }
                Pill(money(avail), availColor)
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { ratio },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),
                color = if (avail < 0) BM.Bad else color,
                trackColor = color.copy(alpha = 0.15f)
            )
        }
    }
}
