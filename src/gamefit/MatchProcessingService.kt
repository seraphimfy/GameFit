class MatchProcessingService(private val calculator: PenaltyCalculator) {

    fun processMatches(account: UserAccount, matches: List<Match>): List<PenaltyResult> {

        val results = mutableListOf<PenaltyResult>()

        for (match in matches) {
            account.addMatch(match)

            val result = calculator.calculatePenalty(match, account.kdaTarget)

            account.addPenalty(result.penaltyPoints)
            results.add(result)
        }

        return results
    }
}