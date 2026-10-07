package gamefit.repository

import gamefit.model.Match

interface MatchRepository {
    fun isProceed(id: Long): Boolean

    fun save(match: Match)

    fun getAll(): List<Match>
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