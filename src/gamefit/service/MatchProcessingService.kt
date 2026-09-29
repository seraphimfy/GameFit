package gamefit.service

import gamefit.model.Match
import gamefit.model.PenaltyResult
import gamefit.model.UserAccount
import gamefit.repository.MatchRepository

class MatchProcessingService(private val calculator: PenaltyCalculator, private val repository: MatchRepository) {

    fun processMatches(account: UserAccount, matches: List<Match>): List<PenaltyResult> {
        val results = mutableListOf<PenaltyResult>()

        for (match in matches) {
            if(repository.isProceed(match.id))
                continue

            repository.save(match)
            account.addMatch(match)

            val result = calculator.calculatePenalty(match, account.kdaTarget)
            account.addPenalty(result.penaltyPoints)
            results.add(result)
        }

        return results
    }
}