package com.gamefit.android

// Импорты стандартных библиотек Android для построения интерфейса
import android.os.Bundle
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope

// Сгенерированный класс разметки активности (res/layout/activity_main.xml)
import com.gamefit.android.databinding.ActivityMainBinding

// Импорты моделей и бизнес-логики приложения
import gamefit.model.Exercise
import gamefit.model.Match
import gamefit.model.UserAccount
import gamefit.data.local.GameFitDatabase
import gamefit.data.local.GameFitLocalRepository
import gamefit.provider.MatchProviderResult
import gamefit.provider.OpenDotaMatchProvider
import gamefit.service.ExercisePlanner
import gamefit.service.PenaltyCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MainActivity : AppCompatActivity() {

    // Ссылка на View Binding для доступа к кнопкам и текстовым полям на экране
    private lateinit var binding: ActivityMainBinding

    // Сервис, который рассчитывает штраф за матч
    private val penaltyCalculator = PenaltyCalculator()

    // Предустановленный список упражнений с их "весом" (сложностью)
    private val exercises = listOf(
        Exercise("Приседания", 1, 0.5),
        Exercise("Пресс", 2, 1.0),
        Exercise("Отжимания", 3, 1.5)
    )

    // Планировщик, который будет распределять штрафные очки по списку упражнений
    private val planner = ExercisePlanner(exercises)

    // Текущий профиль пользователя с начальным именем "TestUser"
    private var account = UserAccount("TestUser")

    // Простой автоинкрементный счётчик для присвоения ID новым матчам
    private var nextMatchId = 1L
    private var isAccountLoaded = false

    private val matchProvider = OpenDotaMatchProvider()
    private val localRepository by lazy {
        // Репозиторий создаётся только при первом обращении к локальной БД.
        GameFitLocalRepository(GameFitDatabase.getInstance(applicationContext))
    }

    // Точка входа в жизненный цикл экрана Android
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализируем верстку через View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Навешиваем слушатели кликов на все кнопки
        setupButtons()

        lifecycleScope.launch {
            // Room работает в IO-потоке, чтобы не блокировать главный экран.
            account = withContext(Dispatchers.IO) {
                localRepository.loadAccount(account.username)
            }
            nextMatchId = (account.getMatches().maxOfOrNull { it.id } ?: 0L) + 1L
            isAccountLoaded = true
            updateUI()
        }
    }

    // Привязка действий к кнопкам интерфейса
    private fun setupButtons() {
        // Кнопка импорта матчей из OpenDota
        binding.btnImportMatches.setOnClickListener {
            showImportMatchesDialog()
        }

        // Вызов диалоговых окон и методов бизнес-логики
        binding.btnManualMatch.setOnClickListener { showManualMatchDialog() }
        binding.btnAssignPlan.setOnClickListener { assignPlan() }
        binding.btnCompleteWorkout.setOnClickListener { completeWorkout() }
        binding.btnMatchHistory.setOnClickListener { showMatchHistory() }
        binding.btnChangeKda.setOnClickListener { showChangeKdaDialog() }
        binding.btnResetData.setOnClickListener { resetData() }
    }

    private fun isReady(): Boolean {
        if (!isAccountLoaded) {
            showToast("Данные ещё загружаются")
            return false
        }
        return true
    }

    private fun showImportMatchesDialog() {
        // Пользователь указывает OpenDota account ID и число импортируемых матчей.
        if (!isReady()) return

        val accountIdInput = numberInput("OpenDota account ID").apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        val limitInput = numberInput("Количество матчей").apply {
            setText("10")
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        val content = dialogLayout(accountIdInput, limitInput)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Импорт матчей из OpenDota")
            .setView(content)
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Загрузить", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val accountId = accountIdInput.text.toString().toLongOrNull()
                val limit = limitInput.text.toString().toIntOrNull()

                if (accountId == null || accountId <= 0 || limit == null || limit <= 0) {
                    showToast("Введите корректные account ID и количество матчей")
                    return@setOnClickListener
                }

                dialog.dismiss()
                importMatches(accountId, limit)
            }
        }
        dialog.show()
    }

    private fun importMatches(accountId: Long, limit: Int) {
        // Сетевой запрос выполняется вне главного потока.
        binding.btnImportMatches.isEnabled = false
        showToast("Загружаем матчи из OpenDota...")

        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                matchProvider.getMatches(accountId, limit)
            }

            binding.btnImportMatches.isEnabled = true
            handleImportResult(result)
        }
    }

    private fun handleImportResult(result: MatchProviderResult) {
        // Результат провайдера превращается в состояние аккаунта и UI-сообщение.
        when (result) {
            is MatchProviderResult.Success -> {
                if (result.matches.isEmpty()) {
                    showToast("OpenDota не вернул матчи")
                    return
                }

                val existingMatchIds = account.getMatches().mapTo(mutableSetOf()) { it.id }
                val newMatches = result.matches.filter { it.id !in existingMatchIds }

                if (newMatches.isEmpty()) {
                    showToast("Новых матчей не найдено")
                    return
                }

                var totalPenalty = 0
                newMatches.forEach { match ->
                    val penalty = penaltyCalculator.calculatePenalty(match, account.kdaTarget)
                    account.addMatch(match)
                    account.addPenalty(penalty.penaltyPoints)
                    totalPenalty += penalty.penaltyPoints
                }
                nextMatchId = maxOf(nextMatchId, newMatches.maxOf { it.id } + 1)
                updateUI()
                saveAccount()

                val message = newMatches.joinToString("\n") { match ->
                    "#${match.id}: ${match.kills}/${match.deaths}/${match.assists}, " +
                        "KDA ${String.format(Locale.US, "%.2f", match.kda)}, " +
                        if (match.matchWon) "победа" else "поражение"
                }

                AlertDialog.Builder(this)
                    .setTitle("Импортировано матчей: ${newMatches.size}")
                    .setMessage("$message\n\nНачислено штрафа: $totalPenalty")
                    .setPositiveButton("ОК", null)
                    .show()
            }

            MatchProviderResult.Failure.Network -> {
                showToast("Не удалось подключиться к OpenDota")
            }

            MatchProviderResult.Failure.RateLimited -> {
                showToast("OpenDota временно ограничил запросы. Попробуйте позже")
            }

            MatchProviderResult.Failure.NotFound -> {
                showToast("Игрок с таким account ID не найден")
            }

            is MatchProviderResult.Failure.InvalidRequest -> {
                showToast(result.message)
            }

            is MatchProviderResult.Failure.Unknown -> {
                showToast("Не удалось загрузить матчи")
            }
        }
    }

    override fun onDestroy() {
        matchProvider.close()
        super.onDestroy()
    }

    // Диалог ручного ввода параметров сыгранного матча
    private fun showManualMatchDialog() {
        if (!isReady()) return

        // Создаем динамические поля ввода прямо в коде
        val kills = numberInput("Убийства")
        val deaths = numberInput("Смерти")
        val assists = numberInput("Помощи")
        val won = CheckBox(this).apply { text = "Победа" }

        // Собираем поля в один вертикальный контейнер
        val content = dialogLayout(kills, deaths, assists, won)

        AlertDialog.Builder(this)
            .setTitle("Ввести матч")
            .setView(content)
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Сохранить") { _, _ ->
                // Собираем объект матча из введённых данных
                val match = Match(
                    id = nextMatchId++,
                    kills = kills.intValue(),
                    deaths = deaths.intValue(),
                    assists = assists.intValue(),
                    matchWon = won.isChecked
                )

                // Вычисляем штраф на основе KDA и целевого показателя игрока
                val result = penaltyCalculator.calculatePenalty(match, account.kdaTarget)

                // Записываем матч и прибавляем штраф к аккаунту
                account.addMatch(match)
                account.addPenalty(result.penaltyPoints)

                // Обновляем текст на экране и показываем всплывающее сообщение
                updateUI()
                saveAccount()
                showToast("Матч сохранён. Штраф: ${result.penaltyPoints} очков")
            }
            .show()
    }

    // Формирование тренировки на основе накопленных штрафных очков
    private fun assignPlan() {
        if (!isReady()) return

        // Планировщик рассчитывает, сколько раз нужно выполнить каждое упражнение
        val plan = planner.planExercise(account.penalty, account)

        // Метод assignPlan возвращает false, если очков 0 или назначить план не удалось
        if (!account.assignPlan(plan)) {
            showToast("Нет штрафных очков для тренировки")
            return
        }

        // Сразу показываем назначенные упражнения на главном экране.
        updateUI()
        saveAccount()

        // Преобразуем словарь (упражнение -> количество) в читаемый текст со строками
        val description = plan.entries.joinToString("\n") { (exercise, repetitions) ->
            "${exercise.name}: $repetitions раз"
        }

        AlertDialog.Builder(this)
            .setTitle("Тренировка назначена")
            .setMessage(description.ifEmpty { "Не удалось составить план" })
            .setPositiveButton("ОК", null)
            .show()
    }

    // Подтверждение выполнения текущей тренировки
    private fun completeWorkout() {
        if (!isReady()) return

        // Списывает выполненную тренировку и возвращает завершённый план
        val completedPlan = account.completeWork()

        if (completedPlan.isEmpty()) {
            showToast("Сначала назначьте тренировку")
            return
        }

        // Обновляем интерфейс (штрафные очки должны уменьшиться/сброситься)
        updateUI()
        saveAccount()
        showToast("Тренировка завершена")
    }

    // Показ истории сыгранных матчей
    private fun showMatchHistory() {
        if (!isReady()) return

        val matches = account.getMatches()

        // Форматируем список матчей в одну строку с переносами (\n)
        val message = matches.joinToString("\n") { match ->
            "#${match.id}: ${match.kills}/${match.deaths}/${match.assists}, " +
                    "KDA ${String.format(Locale.US, "%.2f", match.kda)}, " +
                    if (match.matchWon) "победа" else "поражение"
        }

        AlertDialog.Builder(this)
            .setTitle("История матчей")
            .setMessage(message.ifEmpty { "Матчей пока нет" })
            .setPositiveButton("ОК", null)
            .show()
    }

    // Диалог изменения целевого KDA
    private fun showChangeKdaDialog() {
        if (!isReady()) return

        val input = numberInput("Цель KDA").apply {
            setText(String.format(Locale.US, "%.2f", account.kdaTarget))
        }

        AlertDialog.Builder(this)
            .setTitle("Изменить цель KDA")
            .setView(input)
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Сохранить") { _, _ ->
                // Заменяем запятую на точку для поддержки дробного ввода с любых клавиатур
                val target = input.text.toString().replace(',', '.').toDoubleOrNull()

                // Проверяем валидность числа и меняем значение в аккаунте
                if (target != null && account.changeTarget(target)) {
                    updateUI()
                    saveAccount()
                    showToast("Цель KDA изменена")
                } else {
                    showToast("Введите положительное число")
                }
            }
            .show()
    }

    // Сброс всех накопленных данных
    private fun resetData() {
        if (!isReady()) return

        AlertDialog.Builder(this)
            .setTitle("Сбросить данные?")
            .setMessage("Будут удалены матчи, штраф и текущий план.")
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Сбросить") { _, _ ->
                // Пересоздаём объект аккаунта и сбрасываем ID матчей
                account = UserAccount("TestUser")
                nextMatchId = 1L
                updateUI()
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        localRepository.clearAccount(account.username)
                        localRepository.saveAccount(account)
                    }
                }
                showToast("Данные сброшены")
            }
            .show()
    }

    // Метод обновления текстовых блоков на экране
    private fun updateUI() {
        binding.tvUsername.text = "Пользователь: ${account.username}"
        binding.tvPenalty.text = "Штраф: ${account.penalty} очков"
        binding.tvKdaTarget.text = "Цель KDA: ${String.format(Locale.US, "%.2f", account.kdaTarget)}"
        binding.tvWorkoutStatus.text = if (account.getCurrentPlan().isEmpty()) {
            "Текущая тренировка: нет"
        } else {
            val plan = account.getCurrentPlan().entries.joinToString(", ") { (exercise, repetitions) ->
                "${exercise.name}: $repetitions"
            }
            "Текущая тренировка: $plan"
        }
        binding.tvExerciseProgress.text = if (account.getAllProgress().isEmpty()) {
            "Прогресс упражнений: нет данных"
        } else {
            val progress = account.getAllProgress().entries.joinToString(", ") { (name, points) ->
                "$name: $points"
            }
            "Прогресс упражнений: $progress"
        }
    }

    private fun saveAccount() {
        // Сохраняем снимок текущего аккаунта в локальную Room-БД.
        val accountToSave = account
        lifecycleScope.launch(Dispatchers.IO) {
            localRepository.saveAccount(accountToSave)
        }
    }

    // Вспомогательная функция: программное создание поля ввода чисел
    private fun numberInput(hint: String): EditText = EditText(this).apply {
        this.hint = hint
        // Разрешаем ввод только цифр с поддержкой десятичной точки
        inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    // Вспомогательная функция: упаковка переданных View в вертикальный контейнер с отступами
    private fun dialogLayout(vararg views: android.view.View): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(48, 0, 48, 0)
        views.forEach { addView(it) }
    }

    // Расширение (extension-функция) для безопасного получения целого неотрицательного числа из EditText
    private fun EditText.intValue(): Int = text.toString().toIntOrNull()?.coerceAtLeast(0) ?: 0

    // Вспомогательная функция для быстрого вывода системных всплывающих подсказок (Toast)
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
