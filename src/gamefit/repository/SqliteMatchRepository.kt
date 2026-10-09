package gamefit.repository

import gamefit.model.Match
import java.io.Closeable
import java.sql.DriverManager

class SqliteMatchRepository(
    dbPath: String = "gamefit.db"
) : MatchRepository, Closeable {


    private val connection = DriverManager.getConnection("jdbc:sqlite:$dbPath")

    init {

        connection.createStatement().use { statement ->
            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS matches (
                    id INTEGER PRIMARY KEY,
                    kills INTEGER NOT NULL,
                    deaths INTEGER NOT NULL,
                    assists INTEGER NOT NULL,
                    match_won INTEGER NOT NULL
                );
                """.trimIndent()
            )
        }
    }

    override fun isProceed(id: Long): Boolean {
        val query = "SELECT 1 FROM matches WHERE id = ? LIMIT 1"
        connection.prepareStatement(query).use { statement ->
            statement.setLong(1, id)
            val resultSet = statement.executeQuery()
            return resultSet.next()
        }
    }
   override fun clear() {
        connection.createStatement().use { statement ->
            statement.executeUpdate("DELETE FROM matches")
        }
    }
    override fun save(match: Match) {
        val sql = """
            INSERT OR IGNORE INTO matches (id, kills, deaths, assists, match_won)
            VALUES (?, ?, ?, ?, ?)
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->
            statement.setLong(1, match.id)
            statement.setInt(2, match.kills)
            statement.setInt(3, match.deaths)
            statement.setInt(4, match.assists)
            statement.setInt(5, if (match.matchWon) 1 else 0)
            statement.executeUpdate()
        }
    }

    override fun getAll(): List<Match> {
        val query = "SELECT id, kills, deaths, assists, match_won FROM matches"
        val matches = mutableListOf<Match>()

        connection.createStatement().use { statement ->
            val rs = statement.executeQuery(query)
            while (rs.next()) {
                matches.add(
                    Match(
                        id = rs.getLong("id"),
                        kills = rs.getInt("kills"),
                        deaths = rs.getInt("deaths"),
                        assists = rs.getInt("assists"),
                        matchWon = rs.getInt("match_won") == 1
                    )
                )
            }
        }
        return matches
    }

    override fun close() {
        if (!connection.isClosed) {
            connection.close()
        }
    }
}