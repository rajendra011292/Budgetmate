package com.example.budgetmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgetmate.data.computeSnapshot
import com.example.budgetmate.ui.*
import kotlinx.coroutines.launch
import java.time.YearMonth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BudgetMateTheme {
                BudgetMateApp()
            }
        }
    }
}

private val tabLabels = listOf("Budget", "Accounts", "Activity", "Goals", "Reports")

@Composable
fun BudgetMateApp(vm: BudgetViewModel = viewModel()) {
    val accounts by vm.accounts.collectAsState()
    val envelopes by vm.envelopes.collectAsState()
    val entries by vm.entries.collectAsState()
    val goals by vm.goals.collectAsState()
    val recurring by vm.recurring.collectAsState()

    var tab by remember { mutableIntStateOf(0) }
    var month by remember { mutableStateOf(YearMonth.now()) }
    var sheet by remember { mutableStateOf<Sheet?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val snap = remember(accounts, envelopes, entries, month) { computeSnapshot(accounts, envelopes, entries, month) }
    val readyNow = remember(accounts, envelopes, entries) { computeSnapshot(accounts, envelopes, entries, YearMonth.now()).readyToAssign }

    fun say(message: String) { scope.launch { snackbar.showSnackbar(message) } }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest) {
                val icons = listOf(Icons.Default.Wallet, Icons.Default.AccountBalance, Icons.Default.ReceiptLong, Icons.Default.Flag, Icons.Default.BarChart)
                tabLabels.forEachIndexed { i, label ->
                    NavigationBarItem(
                        selected = tab == i, onClick = { tab = i },
                        icon = { Icon(icons[i], contentDescription = label) },
                        label = { Text(label) }
                    )
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { sheet = Sheet.AddMenu },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Add") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        val m = Modifier.padding(padding).statusBarsPadding()
        when (tab) {
            0 -> BudgetScreen(
                snap = snap, month = month, onMonth = { month = it },
                open = { sheet = it },
                onCopyLast = {
                    scope.launch {
                        val n = vm.assignLikeLastMonth(month)
                        say(if (n > 0) "Assigned $n envelope${if (n > 1) "s" else ""} like last month" else "Nothing to copy — or nothing left to assign")
                    }
                },
                modifier = m
            )
            1 -> AccountsScreen(snap, accounts, { sheet = it }, m)
            2 -> ActivityScreen(entries, accounts, envelopes, onEdit = { e ->
                sheet = Sheet.EditEntry(e)
            }, modifier = m)
            3 -> GoalsScreen(goals, readyNow, { sheet = it }, onDelete = { g -> scope.launch { vm.deleteGoal(g) } }, modifier = m)
            else -> ReportsScreen(
                snap = snap, entries = entries, recurring = recurring, envelopes = envelopes,
                month = month, onMonth = { month = it }, open = { sheet = it },
                onToggleRecurring = { r, on -> scope.launch { vm.setRecurringActive(r, on) } },
                onDeleteRecurring = { r -> scope.launch { vm.deleteRecurring(r) } },
                modifier = m
            )
        }
    }

    SheetHost(vm, sheet, month) { sheet = it }
}
