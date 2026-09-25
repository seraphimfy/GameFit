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