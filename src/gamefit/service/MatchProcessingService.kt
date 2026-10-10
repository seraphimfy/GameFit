package gamefit.service

import gamefit.model.Match
import gamefit.model.PenaltyResult
import gamefit.model.UserAccount
import gamefit.repository.MatchRepository

class MatchProcessingService(private val calculator: PenaltyCalculator, private val repository: MatchRepository) {

    fun processMatches(account: UserAccount, matches: List<Match>): List<PenaltyResult> {
        val results = mutableListOf<PenaltyResult>()

        for (match in matches) {
            val result = calculator.calculatePenalty(match, account.kdaTarget)

            val saved = repository.saveIfNewWithPenalty(
                account.username,
                match,
                result.penaltyPoints
            )

            if (!saved) {
                continue
            }

            account.addMatch(match)
            account.addPenalty(result.penaltyPoints)
            results.add(result)
        }

        return results
    }
}
