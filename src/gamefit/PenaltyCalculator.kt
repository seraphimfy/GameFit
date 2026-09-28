class PenaltyCalculator {
    fun calculatePenalty(match: Match, target: Double): PenaltyResult {
        val currKda = match.kda

        val kdaResult = when {
            currKda >= target -> KdaPerformance.GOOD
            currKda > 1.0 -> KdaPerformance.OK
            else -> KdaPerformance.BAD
        }

        val penalty = when (kdaResult) {
            KdaPerformance.GOOD -> if (!match.matchWon) 15 else 0
            KdaPerformance.OK -> if (!match.matchWon) 30 else 15
            KdaPerformance.BAD -> if (!match.matchWon) 40 else 30
        }

        return PenaltyResult(
            kda = currKda,
            target = target,
            kdaResult = kdaResult,
            matchWon = match.matchWon,
            penaltyPoints = penalty
        )
    }
}