class ExercisePlanner(private val exercises: List<Exercise>){
    fun calculatePriority(exercise: Exercise): Double {
        return exercise.points/exercise.loadCoefficient
    }
    fun planExercise(remainingPoints: Int): Map<Exercise, Int> {
       /* for (exercise in exercises) {
            println(
                "${exercise.name}: " +
                        "points=${exercise.points}, " +
                        "donePoints=${exercise.donePoints}, " +
                        "load=${exercise.loadCoefficient}, " +
                        "priority=${calculatePriority(exercise)}"
            )
        }*/
        var remaining = remainingPoints
        val plan = mutableMapOf<Exercise, Int>()
        while (remaining > 0) {
            val exercise = exercises
                .filter { it.points <= remaining }
                .maxByOrNull { calculatePriority(it) }
                ?: break

            val repetitions = remaining / exercise.points

            plan[exercise] = repetitions
            remaining -= repetitions * exercise.points
        }
        return plan
    }
}