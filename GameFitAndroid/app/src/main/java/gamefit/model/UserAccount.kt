package gamefit.model

// Состояние пользователя: штраф, цель KDA, матчи, прогресс и активный план.
class UserAccount(
    val username: String,
    initialPenalty: Int = 0,
    initialKdaTarget: Double = 2.0,
    initialProgress: Map<String, Int> = emptyMap(),
    initialPlan: Map<Exercise, Int> = emptyMap()
) {
    // Текущий объём штрафных очков.
    var penalty: Int = initialPenalty
        private set

    // Минимальный KDA, используемый при расчёте штрафа.
    var kdaTarget: Double = initialKdaTarget
        private set

    // Активный план: упражнение -> количество повторений.
    private var currentPlan: Map<Exercise, Int> = initialPlan.toMap()
    // История матчей текущего пользователя.
    private val matches = mutableListOf<Match>()
    // Накопленный прогресс в очках по каждому упражнению.
    private val exerciseProgress = initialProgress.toMutableMap()

    fun getProgress(exerciseName: String): Int {
        return exerciseProgress.getOrDefault(exerciseName, 0)
    }

    fun getAllProgress(): Map<String, Int> = exerciseProgress.toMap()

    fun getCurrentPlan(): Map<Exercise, Int> = currentPlan.toMap()

    fun assignPlan(plan: Map<Exercise, Int>): Boolean {
        if (penalty == 0) {
            return false
        }
        currentPlan = plan
        return true
    }

    fun addPenalty(penaltyValue: Int) {
        if (penaltyValue > 0) {
            penalty += penaltyValue
        }
    }

    fun completeWork(): Map<Exercise, Int> {
        if (currentPlan.isEmpty()) {
            return emptyMap()
        }

        for ((exercise, repetitions) in currentPlan) {
            val completedPoints = repetitions * exercise.points
            val currentDone = exerciseProgress.getOrDefault(exercise.name, 0)

            exerciseProgress[exercise.name] = currentDone + completedPoints
            penalty -= completedPoints
        }

        if (penalty < 0) {
            penalty = 0
        }

        val completedPlan = currentPlan
        currentPlan = emptyMap()
        return completedPlan
    }

    fun addMatch(match: Match) {
        matches.add(match)
    }

    fun getMatches(): List<Match> {
        return matches.toList()
    }

    fun changeTarget(target: Double): Boolean {
        if (target <= 0) {
            return false
        }
        kdaTarget = target
        return true
    }
}
