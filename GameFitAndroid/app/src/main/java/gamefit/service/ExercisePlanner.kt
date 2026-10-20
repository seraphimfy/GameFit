package gamefit.service

import gamefit.model.Exercise
import gamefit.model.UserAccount

// Строит план тренировки на основе штрафа и выполненного прогресса.
class ExercisePlanner(private val exercises: List<Exercise>) {

    // Чем выше результат, тем выгоднее выбрать упражнение для следующего сета.
    fun calculatePriority(exercise: Exercise, userDonePoints: Int): Double {
        return exercise.points / exercise.calculateLoadCoefficient(userDonePoints)
    }

    fun planExercise(remainingPoints: Int, user: UserAccount): Map<Exercise, Int> {
        // Локальная копия прогресса не изменяет аккаунт во время планирования.
        var remaining = remainingPoints
        val plan = mutableMapOf<Exercise, Int>()
        val simulatedProgress = mutableMapOf<String, Int>()
        exercises.forEach { ex ->
            simulatedProgress[ex.name] = user.getProgress(ex.name)
        }

        val batchRepetitions = 5

        while (remaining > 0) {
            val exercise = exercises
                .filter { it.points <= remaining }
                .maxByOrNull { ex ->
                    val done = simulatedProgress.getOrDefault(ex.name, 0)
                    calculatePriority(ex, done)
                }
                ?: break

            val maxPossibleReps = remaining / exercise.points
            val repsToAdd = maxPossibleReps.coerceAtMost(batchRepetitions).coerceAtLeast(1)

            plan[exercise] = plan.getOrDefault(exercise, 0) + repsToAdd
            val earnedPoints = repsToAdd * exercise.points
            remaining -= earnedPoints

            simulatedProgress[exercise.name] = simulatedProgress.getOrDefault(exercise.name, 0) + earnedPoints
        }

        return plan
    }
}
