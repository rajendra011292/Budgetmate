package com.example.budgetmate.data

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.util.Locale

/** Ledger entry types. */
object EntryType {
    const val INCOME = "INCOME"
    const val ALLOCATION = "ALLOCATION"
    const val EXPENSE = "EXPENSE"
    const val TRANSFER_IN = "TRANSFER_IN"
    const val TRANSFER_OUT = "TRANSFER_OUT"
}

private val zone: ZoneId get() = ZoneId.systemDefault()

fun monthStart(ym: YearMonth): Long = ym.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
fun monthEnd(ym: YearMonth): Long = monthStart(ym.plusMonths(1))
fun yearMonthOf(millis: Long): YearMonth = YearMonth.from(Instant.ofEpochMilli(millis).atZone(zone))

/** The timestamp a new assignment should get when the user is looking at [month]. */
fun assignmentDate(month: YearMonth): Long {
    val now = System.currentTimeMillis()
    return if (yearMonthOf(now) == month) now
    else month.atDay(1).atTime(12, 0).atZone(zone).toInstant().toEpochMilli()
}

fun nextDueAfter(from: Long, interval: String): Long {
    val d = Instant.ofEpochMilli(from).atZone(zone).toLocalDate()
    val n = when (interval) {
        "WEEKLY" -> d.plusWeeks(1)
        "YEARLY" -> d.plusYears(1)
        else -> d.plusMonths(1)
    }
    return n.atStartOfDay(zone).toInstant().toEpochMilli()
}

// ---------- Money formatting / parsing ----------

private val rupee: NumberFormat get() = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

fun money(cents: Long): String {
    val f = rupee
    f.minimumFractionDigits = if (cents % 100L == 0L) 0 else 2
    f.maximumFractionDigits = 2
    return f.format(cents / 100.0)
}

/** "1,23,456" style compact money for big hero numbers. */
fun moneyShort(cents: Long): String {
    val v = kotlin.math.abs(cents) / 100.0
    val sign = if (cents < 0) "-" else ""
    return when {
        v >= 10_000_000 -> "$sign₹${trim(v / 10_000_000)}Cr"
        v >= 100_000 -> "$sign₹${trim(v / 100_000)}L"
        v >= 1_000 -> "$sign₹${trim(v / 1_000)}K"
        else -> "$sign₹${trim(v)}"
    }
}

private fun trim(d: Double): String {
    val s = String.format(Locale.US, "%.1f", d)
    return if (s.endsWith(".0")) s.dropLast(2) else s
}

/** Parses user input such as "1250" or "1250.5" into cents. Returns 0 for invalid input. */
fun parseAmount(text: String): Long =
    text.trim().toBigDecimalOrNull()?.movePointRight(2)?.setScale(0, RoundingMode.HALF_UP)?.toLong() ?: 0L

/** Keeps digits and one decimal point with at most two decimals. */
fun cleanAmount(input: String): String {
    val f = input.filter { it.isDigit() || it == '.' }
    val i = f.indexOf('.')
    if (i < 0) return f.take(10)
    val head = f.substring(0, i).take(10)
    val tail = f.substring(i + 1).filter { it.isDigit() }.take(2)
    return "$head.$tail"
}

fun centsToInput(cents: Long): String {
    if (cents == 0L) return ""
    val bd = BigDecimal(cents).movePointLeft(2).stripTrailingZeros()
    return bd.toPlainString()
}

// ---------- Zero-sum snapshot ----------

data class EnvelopeLine(
    val envelope: Envelope,
    val assigned: Long,   // assigned during the selected month
    val activity: Long,   // spent during the selected month (positive number)
    val available: Long   // cumulative balance at the end of the selected month
)

/**
 * Zero-sum identity (all at the end of the selected month):
 *   total money in accounts = Ready to Assign + sum(envelope available) + goal savings
 * The budget is "balanced" when Ready to Assign is exactly zero.
 */
data class BudgetSnapshot(
    val month: YearMonth,
    val readyToAssign: Long,
    val totalBalance: Long,
    val monthIncome: Long,
    val monthAssigned: Long,
    val monthSpent: Long,
    val envelopeTotal: Long,
    val goalReserve: Long,
    val lines: List<EnvelopeLine>,
    val accountBalances: Map<Long, Long>
) {
    val overspent: List<EnvelopeLine> get() = lines.filter { it.available < 0 }
    val isBalanced: Boolean get() = readyToAssign == 0L && overspent.isEmpty()
}

fun computeSnapshot(
    accounts: List<Account>,
    envelopes: List<Envelope>,
    entries: List<LedgerEntry>,
    month: YearMonth
): BudgetSnapshot {
    val start = monthStart(month)
    val end = monthEnd(month)
    val upTo = entries.filter { it.date < end }

    val opening = accounts.sumOf { it.openingBalance }
    val incomeAll = upTo.filter { it.type == EntryType.INCOME }.sumOf { it.amount }
    val allocAll = upTo.filter { it.type == EntryType.ALLOCATION }.sumOf { it.amount }
    val ready = opening + incomeAll - allocAll

    val allocByEnv = upTo.filter { it.type == EntryType.ALLOCATION && it.envelopeId != null }
        .groupBy { it.envelopeId!! }.mapValues { (_, v) -> v.sumOf { it.amount } }
    val spentByEnv = upTo.filter { it.type == EntryType.EXPENSE && it.envelopeId != null }
        .groupBy { it.envelopeId!! }.mapValues { (_, v) -> v.sumOf { it.amount } }
    val monthAllocByEnv = upTo.filter { it.type == EntryType.ALLOCATION && it.envelopeId != null && it.date >= start }
        .groupBy { it.envelopeId!! }.mapValues { (_, v) -> v.sumOf { it.amount } }
    val monthSpentByEnv = upTo.filter { it.type == EntryType.EXPENSE && it.envelopeId != null && it.date >= start }
        .groupBy { it.envelopeId!! }.mapValues { (_, v) -> v.sumOf { it.amount } }

    val lines = envelopes.map { e ->
        EnvelopeLine(
            envelope = e,
            assigned = monthAllocByEnv[e.id] ?: 0L,
            activity = monthSpentByEnv[e.id] ?: 0L,
            available = (allocByEnv[e.id] ?: 0L) - (spentByEnv[e.id] ?: 0L)
        )
    }

    val nullAlloc = upTo.filter { it.type == EntryType.ALLOCATION && it.envelopeId == null }.sumOf { it.amount }
    val nullSpent = upTo.filter { it.type == EntryType.EXPENSE && it.envelopeId == null }.sumOf { it.amount }

    val balances = accounts.associate { a ->
        a.id to (a.openingBalance +
            entries.filter { it.accountId == a.id && (it.type == EntryType.INCOME || it.type == EntryType.TRANSFER_IN) }.sumOf { it.amount } -
            entries.filter { it.accountId == a.id && (it.type == EntryType.EXPENSE || it.type == EntryType.TRANSFER_OUT) }.sumOf { it.amount })
    }

    val monthEntries = upTo.filter { it.date >= start }
    return BudgetSnapshot(
        month = month,
        readyToAssign = ready,
        totalBalance = balances.values.sum(),
        monthIncome = monthEntries.filter { it.type == EntryType.INCOME }.sumOf { it.amount },
        monthAssigned = lines.sumOf { it.assigned },
        monthSpent = lines.sumOf { it.activity },
        envelopeTotal = lines.sumOf { it.available },
        goalReserve = nullAlloc - nullSpent,
        lines = lines,
        accountBalances = balances
    )
}

// ---------- Cosmetic helpers ----------

private val emojiRules = listOf(
    listOf("rent", "house", "home", "mortgage") to "🏠",
    listOf("grocer", "food", "kirana", "vegetable") to "🛒",
    listOf("dining", "restaurant", "eat", "zomato", "swiggy", "cafe", "coffee") to "🍽️",
    listOf("electric", "bill", "utilit", "water", "gas", "wifi", "internet", "recharge", "phone") to "💡",
    listOf("fuel", "petrol", "diesel", "transport", "travel", "cab", "bus", "metro", "car", "bike") to "🚗",
    listOf("health", "medic", "doctor", "pharma", "gym", "fitness") to "💊",
    listOf("school", "tuition", "educ", "course", "book") to "🎓",
    listOf("fun", "movie", "entertain", "game", "hobby", "netflix", "subscription") to "🎬",
    listOf("shop", "cloth", "fashion", "amazon") to "🛍️",
    listOf("emi", "loan", "debt", "credit") to "💳",
    listOf("saving", "emergency", "invest", "sip", "fund") to "🏦",
    listOf("gift", "donat", "charity") to "🎁",
    listOf("holiday", "trip", "vacation") to "✈️",
    listOf("pet", "dog", "cat") to "🐾",
    listOf("family", "kid", "child", "baby") to "👨‍👩‍👧"
)

fun emojiFor(name: String): String {
    val n = name.lowercase()
    return emojiRules.firstOrNull { (keys, _) -> keys.any { n.contains(it) } }?.second ?: "✉️"
}

/** Stable palette index for an envelope id (used by the UI to pick a colour). */
fun colorIndexFor(id: Long): Int = ((id % 8) + 8).toInt() % 8
