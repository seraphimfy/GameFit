class ExercisePlanner(private val exercises: List<Exercise>) {

    fun calculatePriority(exercise: Exercise, userDonePoints: Int): Double {
        return exercise.points / exercise.calculateLoadCoefficient(userDonePoints)
    }

    fun planExercise(remainingPoints: Int, user: UserAccount): Map<Exercise, Int> {
        var remaining = remainingPoints
        val plan = mutableMapOf<Exercise, Int>()

        while (remaining > 0) {
            val exercise = exercises
                .filter { it.points <= remaining }
                .maxByOrNull { ex ->
                    val userDone = user.getProgress(ex.name)
                    calculatePriority(ex, userDone)
                }
                ?: break

            val repetitions = remaining / exercise.points
            plan[exercise] = repetitions
            remaining -= repetitions * exercise.points
        }

        return plan
    }
}