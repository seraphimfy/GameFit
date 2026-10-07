package gamefit.ui

import gamefit.model.Exercise
import gamefit.model.Match
import gamefit.model.PenaltyResult
import java.util.Locale

fun printPenaltyResult(result: PenaltyResult) {
    val formattedKda = String.format(Locale.US, "%.2f", result.kda)
    val formattedTarget = String.format(Locale.US, "%.2f", result.target)
    println(
        """
        --- Match Result ---
        KDA: $formattedKda (Target: $formattedTarget)
        Performance: ${result.kdaResult}
        Outcome: ${if (result.matchWon) "WIN" else "LOSS"}
        Penalty added: ${result.penaltyPoints} points
        --------------------
        """.trimIndent()
    )
}

fun printMatchHistory(matches: List<Match>) {
    if (matches.isEmpty()) {
        println("Match history is empty.")
        return
    }
    println("--- Match History ---")
    for (match in matches) {
        val formattedKda = String.format(Locale.US, "%.2f", match.kda)
        val outcome = if (match.matchWon) "WIN" else "LOSS"
        println("Match #${match.id} | K/D/A: ${match.kills}/${match.deaths}/${match.assists} (KDA: $formattedKda) | $outcome")
    }
    println("---------------------")
}

fun printAssignedPlan(plan: Map<Exercise, Int>) {
    println("--- Assigned Workout Plan ---")
    for ((exercise, repetitions) in plan) {
        println("• ${exercise.name}: $repetitions reps")
    }
    println("-----------------------------")
}

fun printCompletedWork(plan: Map<Exercise, Int>) {
    println("--- Workout Completed ---")
    for ((exercise, repetitions) in plan) {
        println("✔ ${exercise.name}: $repetitions reps done")
    }
    println("Penalty points deducted!")
}

fun printNoPenalty() {
    println("No penalty to assign. You're all clear!")
}

fun printNoAssignedPlan() {
    println("There is no assigned exercise plan. Assign one first.")
}

fun printInvalidTarget() {
    println("Target must be a number greater than 0.")
}

fun printTargetChanged(target: Double) {
    val formatted = String.format(Locale.US, "%.2f", target)
    println("KDA target successfully updated to $formatted")
}

fun printPenalty(penalty: Int) {
    println("Current penalty balance: $penalty points")
}