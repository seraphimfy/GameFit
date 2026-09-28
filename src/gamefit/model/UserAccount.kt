class UserAccount(val username: String) {
    var penalty: Int = 0
        private set

    var kdaTarget: Double = 2.0
        private set

    private var currentPlan: Map<Exercise, Int> = emptyMap()
    private val matches = mutableListOf<Match>()
    private val exerciseProgress = mutableMapOf<String, Int>()

    fun getProgress(exerciseName: String): Int {
        return exerciseProgress.getOrDefault(exerciseName, 0)
    }

    fun getAllProgress(): Map<String, Int> = exerciseProgress.toMap()

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