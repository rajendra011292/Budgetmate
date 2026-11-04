package com.example.budgetmate.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budgetmate.data.cleanAmount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

// ---------- Form helper ----------

class FormState(private val scope: CoroutineScope, private val onDone: () -> Unit) {
    var error by mutableStateOf("")
    var busy by mutableStateOf(false)

    /** Runs [block]; a null result means success and closes the sheet, otherwise it is shown as an error. */
    fun submit(block: suspend () -> String?) {
        if (busy) return
        busy = true
        error = ""
        scope.launch {
            val result = try { block() } catch (e: Exception) { e.localizedMessage ?: "Something went wrong. Please try again." }
            busy = false
            if (result == null) onDone() else error = result
        }
    }

    fun fail(message: String) { error = message }
}

@Composable
fun rememberForm(onDone: () -> Unit): FormState {
    val scope = rememberCoroutineScope()
    return remember { FormState(scope, onDone) }
}

// ---------- Bottom sheet ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BmSheet(
    title: String,
    onDismiss: () -> Unit,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = state,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column {
                Text(title, style = MaterialTheme.typography.titleLarge)
                if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            content()
        }
    }
}

@Composable
fun ErrorText(message: String) {
    if (message.isBlank()) return
    Surface(color = BM.Bad.copy(alpha = 0.10f), shape = BM.chipShape) {
        Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth().padding(12.dp))
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, enabled: Boolean = true, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick, enabled = enabled,
        modifier = modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(18.dp)
    ) { Text(text, style = MaterialTheme.typography.titleMedium) }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, danger: Boolean = false) {
    OutlinedButton(
        onClick = onClick, modifier = modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (danger) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
    ) { Text(text, style = MaterialTheme.typography.labelLarge) }
}

// ---------- Fields ----------

@Composable
fun BmTextField(value: String, onChange: (String) -> Unit, label: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value, onValueChange = onChange, label = { Text(label) }, singleLine = true,
        shape = RoundedCornerShape(16.dp), modifier = modifier.fillMaxWidth()
    )
}

/** Large rupee amount input. Accepts decimals (up to 2 places). */
@Composable
fun AmountField(value: String, onChange: (String) -> Unit, label: String = "Amount") {
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(cleanAmount(it)) },
        label = { Text(label) },
        prefix = { Text("₹ ", style = MaterialTheme.typography.titleLarge) },
        textStyle = MaterialTheme.typography.titleLarge,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

/** Single-select chips. A null id means "none"/"top level" style options. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> ChipPicker(label: String, options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (options.isEmpty()) {
            Text("Nothing to choose yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                options.forEach { (value, text) ->
                    FilterChip(
                        selected = value == selected,
                        onClick = { onSelect(value) },
                        label = { Text(text) },
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }
        }
    }
}

/** Single-select dropdown, styled like the other fields. Shows "Nothing to choose yet." when empty. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> BmDropdown(
    label: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val text = options.firstOrNull { it.first == selected }?.second ?: ""
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = text, onValueChange = {}, readOnly = true, singleLine = true,
                label = { Text(if (options.isEmpty()) "Nothing to choose yet." else "Choose") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { (value, option) ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = { expanded = false; onSelect(value) }
                    )
                }
            }
        }
    }
}

// ---------- Small building blocks ----------

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier, trailing: @Composable (() -> Unit)? = null) {
    Row(modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        trailing?.invoke()
    }
}

@Composable
fun BmCard(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme.surfaceContainerLowest
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    if (onClick != null) {
        Surface(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = BM.cardShape, color = colors, border = border) {
            Column(Modifier.padding(16.dp), content = content)
        }
    } else {
        Surface(modifier = modifier.fillMaxWidth(), shape = BM.cardShape, color = colors, border = border) {
            Column(Modifier.padding(16.dp), content = content)
        }
    }
}

@Composable
fun GradientCard(brush: Brush, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Box(modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(brush)) {
        Column(Modifier.padding(22.dp), content = content)
    }
}

@Composable
fun Pill(text: String, color: Color, modifier: Modifier = Modifier, filled: Boolean = true) {
    Box(
        modifier
            .clip(RoundedCornerShape(50))
            .background(if (filled) color.copy(alpha = 0.16f) else Color.Transparent)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) { Text(text, color = color, style = MaterialTheme.typography.labelLarge, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
}

@Composable
fun EmojiAvatar(emoji: String, color: Color, size: Int = 44) {
    Box(
        Modifier.size(size.dp).clip(CircleShape).background(color.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) { Text(emoji, fontSize = (size * 0.46f).sp) }
}

@Composable
fun EmptyState(emoji: String, title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(emoji, fontSize = 48.sp)
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@Composable
fun MonthSwitcher(month: YearMonth, onChange: (YearMonth) -> Unit, modifier: Modifier = Modifier) {
    val label = month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onChange(month.minusMonths(1)) }) { Icon(Icons.Default.ChevronLeft, "Previous month") }
        Text(label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.widthIn(min = 120.dp), textAlign = TextAlign.Center)
        IconButton(onClick = { onChange(month.plusMonths(1)) }) { Icon(Icons.Default.ChevronRight, "Next month") }
    }
}

@Composable
fun ClickableRow(onClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically, content = content)
}
