fun printPenaltyResult(result: PenaltyResult) {
    println(
        """
        KDA: ${result.kda}
        KDA target: ${result.target}
        KDA result: ${result.kdaResult}
        Match result: ${if (result.matchWon) "WIN" else "LOSS"}
        Penalty: ${result.penaltyPoints} points
        """.trimIndent()
    )
}

fun printMatchHistory(matches: List<Match>) {
    for (match in matches) {
        println("Match: $match")
    }
}

fun printAssignedPlan(plan: Map<Exercise, Int>) {
    println("Assigned plan:")

    for ((exercise, repetitions) in plan) {
        println("${exercise.name}: $repetitions repetitions")
    }
}

fun printCompletedWork(plan: Map<Exercise, Int>) {
    for ((exercise, repetitions) in plan) {
        println("${exercise.name}: $repetitions repetitions completed")
    }

    println("Work completed.")
}

fun printNoPenalty() {
    println("No penalty to assign")
}

fun printNoAssignedPlan() {
    println("There is no assigned exercise plan.")
}

fun printInvalidTarget() {
    println("Target must be greater than 0")
}

fun printTargetChanged(target: Double) {
    println("KDA target changed to $target")
}

fun printPenalty(penalty: Int) {
    println("Penalty: $penalty")
}