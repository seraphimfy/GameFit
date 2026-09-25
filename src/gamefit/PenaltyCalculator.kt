class PenaltyCalculator {
    fun calculatePenalty(match: Match, target: Double): PenaltyResult {
        val currKDA = match.kda

        val kdaResult = when {
            currKDA >= target -> "GOOD"
            currKDA > 1.0 -> "OK"
            else -> "BAD"
        }

        val penalty = when (kdaResult) {
            "GOOD" -> if (!match.matchWon) 15 else 0
            "OK" -> if (!match.matchWon) 20 else 10
            else -> if (!match.matchWon) 40 else 30
        }

        return PenaltyResult(
            kda = currKDA,
            target = target,
            kdaResult = kdaResult,
            matchWon = match.matchWon,
            penaltyPoints = penalty
        )
    }

}