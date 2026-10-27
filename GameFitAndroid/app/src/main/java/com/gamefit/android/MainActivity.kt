package com.gamefit.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import gamefit.data.local.GameFitDatabase
import gamefit.data.local.GameFitLocalRepository
import gamefit.model.Match
import gamefit.provider.OpenDotaMatchProvider
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val provider = OpenDotaMatchProvider()
    private val viewModel: GameFitViewModel by viewModels {
        GameFitViewModelFactory(
            GameFitLocalRepository(GameFitDatabase.getInstance(applicationContext)), provider
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { GameFitTheme { GameFitApp(viewModel) } }
    }
}

private enum class Dialog { Import, Manual, Kda, Reset }

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun GameFitApp(viewModel: GameFitViewModel) {
    val state by viewModel.uiState.collectAsState()
    var dialog by remember { mutableStateOf<Dialog?>(null) }
    var showHistory by remember { mutableStateOf(false) }

    if (showHistory) {
        HistoryScreen(state.account.getMatches()) { showHistory = false }
        return
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("GameFit", fontWeight = FontWeight.Bold) }) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(true, { }, icon = { Text("⌂") }, label = { Text("Главная") })
                NavigationBarItem(false, { showHistory = true }, icon = { Text("▣") }, label = { Text("Матчи") })
                NavigationBarItem(false, { dialog = Dialog.Kda }, icon = { Text("⚙") }, label = { Text("Настройки") })
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (state.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
            Text("Добро пожаловать, ${state.account.username}", style = MaterialTheme.typography.headlineSmall)
            Text("Следи за матчами и превращай штрафы в прогресс", color = MaterialTheme.colorScheme.onSurfaceVariant)
            SummaryCard(state.account)
            WorkoutCard(state.account)
            Text("Действия", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Action("Импорт матчей", true, Modifier.weight(1f)) { dialog = Dialog.Import }
                Action("Ввести матч", false, Modifier.weight(1f)) { dialog = Dialog.Manual }
            }
            Action("Назначить тренировку", true, Modifier.fillMaxWidth()) { viewModel.assignPlan() }
            OutlinedButton({ viewModel.completeWorkout() }, Modifier.fillMaxWidth()) { Text("Завершить тренировку") }
            TextButton({ dialog = Dialog.Reset }, Modifier.align(Alignment.CenterHorizontally)) { Text("Сбросить данные", color = MaterialTheme.colorScheme.error) }
        }
    }

    when (dialog) {
        Dialog.Import -> ImportDialog({ dialog = null }) { id, limit -> dialog = null; viewModel.importMatches(id, limit) }
        Dialog.Manual -> ManualDialog({ dialog = null }) { k, d, a, win -> dialog = null; viewModel.addManualMatch(k, d, a, win) }
        Dialog.Kda -> KdaDialog(state.account.kdaTarget, { dialog = null }) { target -> dialog = null; viewModel.changeTarget(target) }
        Dialog.Reset -> ConfirmDialog({ dialog = null }) { dialog = null; viewModel.resetData() }
        null -> Unit
    }
    state.message?.let { InfoDialog("GameFit", it) { viewModel.clearMessage() } }
}

@Composable private fun SummaryCard(account: gamefit.model.UserAccount) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Column { Text("Текущий штраф", style = MaterialTheme.typography.labelLarge); Text("${account.penalty}", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold) }; Text("очков", style = MaterialTheme.typography.titleMedium) }
            Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .2f))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Цель KDA\n${fmt(account.kdaTarget)}"); Text("Матчей\n${account.getMatches().size}"); Text("Прогресс\n${account.getAllProgress().values.sum()}") }
        }
    }
}

@Composable private fun WorkoutCard(account: gamefit.model.UserAccount) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Текущая тренировка", fontWeight = FontWeight.Bold); Text(if (account.getCurrentPlan().isEmpty()) "Не назначена" else "Активна", color = MaterialTheme.colorScheme.primary) }
        Text(account.getCurrentPlan().entries.joinToString(", ") { "${it.key.name}: ${it.value}" }.ifEmpty { "нет" }, color = MaterialTheme.colorScheme.onSurfaceVariant)
        LinearProgressIndicator(if (account.penalty == 0) 1f else .15f, Modifier.fillMaxWidth())
    } }
}

@Composable private fun Action(text: String, primary: Boolean, modifier: Modifier, onClick: () -> Unit) { if (primary) Button(onClick, modifier, shape = RoundedCornerShape(14.dp)) { Text(text) } else FilledTonalButton(onClick, modifier, shape = RoundedCornerShape(14.dp)) { Text(text) } }

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun HistoryScreen(matches: List<Match>, onBack: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("История матчей") }, navigationIcon = { IconButton(onBack) { Text("‹", style = MaterialTheme.typography.headlineMedium) } }) }) { padding ->
        if (matches.isEmpty()) Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text("Матчей пока нет", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        else Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { matches.asReversed().forEach { match -> MatchCard(match) } }
    }
}

@Composable private fun MatchCard(match: Match) { Card(Modifier.fillMaxWidth()) { Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Column { Text("Матч #${match.id}", fontWeight = FontWeight.Bold); Text("${match.kills} / ${match.deaths} / ${match.assists}", color = MaterialTheme.colorScheme.onSurfaceVariant) }; Column(horizontalAlignment = Alignment.End) { Text(if (match.matchWon) "ПОБЕДА" else "ПОРАЖЕНИЕ", color = if (match.matchWon) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold); Text("KDA ${fmt(match.kda)}") } } } }

@Composable private fun ImportDialog(close: () -> Unit, confirm: (Long, Int) -> Unit) { var id by remember { mutableStateOf("") }; var limit by remember { mutableStateOf("10") }; AlertDialog(onDismissRequest = close, title = { Text("Импорт матчей") }, text = { Column { Field("OpenDota account ID", id) { id = it }; Field("Количество матчей", limit) { limit = it } } }, confirmButton = { TextButton({ id.toLongOrNull()?.let { i -> limit.toIntOrNull()?.let { l -> if (i > 0 && l > 0) confirm(i, l) } } }) { Text("Загрузить") } }, dismissButton = { TextButton(close) { Text("Отмена") } }) }
@Composable private fun ManualDialog(close: () -> Unit, confirm: (Int, Int, Int, Boolean) -> Unit) { var k by remember { mutableStateOf("") }; var d by remember { mutableStateOf("") }; var a by remember { mutableStateOf("") }; var win by remember { mutableStateOf(false) }; AlertDialog(onDismissRequest = close, title = { Text("Ввести матч") }, text = { Column { Field("Убийства", k) { k = it }; Field("Смерти", d) { d = it }; Field("Помощи", a) { a = it }; Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(win, { win = it }); Text("Победа") } } }, confirmButton = { TextButton({ confirm(k.toIntOrNull() ?: 0, d.toIntOrNull() ?: 0, a.toIntOrNull() ?: 0, win) }) { Text("Сохранить") } }, dismissButton = { TextButton(close) { Text("Отмена") } }) }
@Composable private fun KdaDialog(current: Double, close: () -> Unit, confirm: (Double) -> Unit) { var value by remember { mutableStateOf(fmt(current)) }; AlertDialog(onDismissRequest = close, title = { Text("Цель KDA") }, text = { Field("Цель KDA", value) { value = it } }, confirmButton = { TextButton({ value.replace(',', '.').toDoubleOrNull()?.let(confirm) }) { Text("Сохранить") } }, dismissButton = { TextButton(close) { Text("Отмена") } }) }
@Composable private fun ConfirmDialog(close: () -> Unit, confirm: () -> Unit) = AlertDialog(onDismissRequest = close, title = { Text("Сбросить данные?") }, text = { Text("Будут удалены матчи, штраф и текущий план.") }, confirmButton = { TextButton(confirm) { Text("Сбросить") } }, dismissButton = { TextButton(close) { Text("Отмена") } })
@Composable private fun InfoDialog(title: String, text: String, close: () -> Unit) = AlertDialog(onDismissRequest = close, title = { Text(title) }, text = { Text(text) }, confirmButton = { TextButton(close) { Text("ОК") } })
@Composable private fun Field(label: String, value: String, change: (String) -> Unit) = OutlinedTextField(value, change, label = { Text(label) }, singleLine = true)

private fun fmt(value: Double) = String.format(Locale.US, "%.2f", value)

@Composable private fun GameFitTheme(content: @Composable () -> Unit) { MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF6750A4), primaryContainer = Color(0xFFEADDFF)), content = content) }
