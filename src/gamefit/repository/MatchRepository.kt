package gamefit.repository

import gamefit.model.Match

interface MatchRepository {
    /**
     * Сохраняет новый матч и начисляет штраф в одной транзакции.
     * Возвращает false, если пользователь уже обрабатывал этот матч.
     */
    fun saveIfNewWithPenalty(
        username: String,
        match: Match,
        penaltyPoints: Int
    ): Boolean

    fun getAll(username: String): List<Match>
    fun clear()
}
/*class InMemoryMatchRepository : MatchRepository {
    private val processedMatches = mutableMapOf<Long, Match>()

    override fun isProceed(id: Long): Boolean {
        return processedMatches.containsKey(id)
    }

    override fun save(match: Match) {
        processedMatches[match.id] = match
    }

    override fun getAll(): List<Match> {
        return processedMatches.values.toList()
    }
}*/
