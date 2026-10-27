package com.gamefit.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gamefit.data.local.GameFitLocalRepository
import gamefit.model.Exercise
import gamefit.model.Match
import gamefit.model.UserAccount
import gamefit.provider.MatchProvider
import gamefit.provider.MatchProviderResult
import gamefit.service.ExercisePlanner
import gamefit.service.PenaltyCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class GameFitUiState(
    val account: UserAccount = UserAccount("TestUser"),
    val isLoading: Boolean = true,
    val isImporting: Boolean = false,
    val message: String? = null
)

class GameFitViewModel(
    private val repository: GameFitLocalRepository,
    private val provider: MatchProvider,
    private val calculator: PenaltyCalculator = PenaltyCalculator(),
    private val exercises: List<Exercise> = listOf(
        Exercise("Приседания", 1, .5),
        Exercise("Пресс", 2, 1.0),
        Exercise("Отжимания", 3, 1.5)
    )
) : ViewModel() {
    private val planner = ExercisePlanner(exercises)
    private val _uiState = MutableStateFlow(GameFitUiState())
    val uiState: StateFlow<GameFitUiState> = _uiState.asStateFlow()
    private var nextMatchId = 1L

    init { loadAccount() }

    private fun loadAccount() = viewModelScope.launch {
        val account = withContext(Dispatchers.IO) { repository.loadAccount("TestUser") }
        nextMatchId = (account.getMatches().maxOfOrNull { it.id } ?: 0L) + 1L
        _uiState.value = GameFitUiState(account = account, isLoading = false)
    }

    fun addManualMatch(kills: Int, deaths: Int, assists: Int, won: Boolean) {
        val account = _uiState.value.account
        val match = Match(nextMatchId++, kills.coerceAtLeast(0), deaths.coerceAtLeast(0), assists.coerceAtLeast(0), won)
        account.addMatch(match)
        val penalty = calculator.calculatePenalty(match, account.kdaTarget).penaltyPoints
        account.addPenalty(penalty)
        updateAccount(account, "Матч сохранён. Штраф: $penalty очков")
    }

    fun importMatches(accountId: Long, limit: Int) = viewModelScope.launch {
        _uiState.update { it.copy(isImporting = true, message = null) }
        val result = withContext(Dispatchers.IO) { provider.getMatches(accountId, limit) }
        val account = _uiState.value.account
        val message = when (result) {
            is MatchProviderResult.Success -> {
                val existing = account.getMatches().mapTo(mutableSetOf()) { it.id }
                val newMatches = result.matches.filter { it.id !in existing }
                newMatches.forEach { match ->
                    account.addMatch(match)
                    account.addPenalty(calculator.calculatePenalty(match, account.kdaTarget).penaltyPoints)
                }
                if (newMatches.isEmpty()) "Новых матчей не найдено" else "Импортировано матчей: ${newMatches.size}"
            }
            is MatchProviderResult.Failure.RateLimited -> "OpenDota временно ограничил запросы"
            is MatchProviderResult.Failure.NotFound -> "Игрок с таким account ID не найден"
            is MatchProviderResult.Failure.InvalidRequest -> result.message
            else -> "Не удалось загрузить матчи"
        }
        nextMatchId = maxOf(nextMatchId, account.getMatches().maxOfOrNull { it.id }?.plus(1) ?: nextMatchId)
        updateAccount(account, message, importing = false)
    }

    fun assignPlan() {
        val account = _uiState.value.account
        val plan = planner.planExercise(account.penalty, account)
        if (!account.assignPlan(plan)) {
            setMessage("Нет штрафных очков для тренировки")
        } else {
            val description = plan.entries.joinToString("\n") { "${it.key.name}: ${it.value} раз" }
            updateAccount(account, description)
        }
    }

    fun completeWorkout() {
        val account = _uiState.value.account
        if (account.completeWork().isEmpty()) setMessage("Сначала назначьте тренировку")
        else updateAccount(account, "Тренировка завершена")
    }

    fun changeTarget(target: Double) {
        val account = _uiState.value.account
        if (!account.changeTarget(target)) setMessage("Введите положительное число")
        else updateAccount(account, "Цель KDA изменена")
    }

    fun resetData() = viewModelScope.launch {
        val account = UserAccount("TestUser")
        nextMatchId = 1L
        withContext(Dispatchers.IO) {
            repository.clearAccount(account.username)
            repository.saveAccount(account)
        }
        _uiState.value = GameFitUiState(account = account, isLoading = false, message = "Данные сброшены")
    }

    fun clearMessage() { _uiState.update { it.copy(message = null) } }

    private fun updateAccount(account: UserAccount, message: String, importing: Boolean = false) {
        _uiState.update { it.copy(account = account, isImporting = importing, message = message) }
        viewModelScope.launch(Dispatchers.IO) { repository.saveAccount(account) }
    }

    private fun setMessage(message: String) { _uiState.update { it.copy(message = message) } }

    override fun onCleared() { super.onCleared() }
}

class GameFitViewModelFactory(
    private val repository: GameFitLocalRepository,
    private val provider: MatchProvider
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        GameFitViewModel(repository, provider) as T
}
