class MatchProcessingService(private val calculator: PenaltyCalculator){
    fun processMatches(account: UserAccount, matches: List<Match>) {
        for (match in matches)
        {
            account.addMatch(match)
            val penalty =calculator.calculatePenalty(match, account.kdaTarget)
            account.addPenalty(penalty)
        }
    }
}