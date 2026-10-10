package gamefit.repository

import gamefit.model.Match

class SqliteMatchRepository(
    private val database: SqliteDatabase
) : MatchRepository {

    init {
        database.connection.createStatement().use { statement ->
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
            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS user_matches (
                    username TEXT NOT NULL,
                    match_id INTEGER NOT NULL,
                    PRIMARY KEY (username, match_id)
                );
                """.trimIndent()
            )
        }
    }

    override fun saveIfNewWithPenalty(
        username: String,
        match: Match,
        penaltyPoints: Int
    ): Boolean {
        return database.transaction { connection ->
            val saveUserMatchSql = """
                INSERT OR IGNORE INTO user_matches (username, match_id)
                VALUES (?, ?)
            """.trimIndent()

            val userMatchWasSaved = connection.prepareStatement(saveUserMatchSql).use { statement ->
                statement.setString(1, username)
                statement.setLong(2, match.id)
                statement.executeUpdate() > 0
            }

            if (!userMatchWasSaved) {
                return@transaction false
            }

            val saveMatchSql = """
                INSERT OR IGNORE INTO matches (id, kills, deaths, assists, match_won)
                VALUES (?, ?, ?, ?, ?)
            """.trimIndent()

            connection.prepareStatement(saveMatchSql).use { statement ->
                statement.setLong(1, match.id)
                statement.setInt(2, match.kills)
                statement.setInt(3, match.deaths)
                statement.setInt(4, match.assists)
                statement.setInt(5, if (match.matchWon) 1 else 0)
                statement.executeUpdate()
            }

            val updatePenaltySql = """
                UPDATE users
                SET penalty = penalty + ?
                WHERE username = ?
            """.trimIndent()

            val userWasUpdated = connection.prepareStatement(updatePenaltySql).use { statement ->
                statement.setInt(1, penaltyPoints)
                statement.setString(2, username)
                statement.executeUpdate() > 0
            }

            if (!userWasUpdated) {
                throw IllegalStateException("User '$username' was not found")
            }

            true
        }
    }

    override fun getAll(username: String): List<Match> {
        val query = """
            SELECT m.id, m.kills, m.deaths, m.assists, m.match_won
            FROM matches m
            JOIN user_matches um ON um.match_id = m.id
            WHERE um.username = ?
        """.trimIndent()
        val matches = mutableListOf<Match>()

        database.connection.prepareStatement(query).use { statement ->
            statement.setString(1, username)
            val rs = statement.executeQuery()
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

    override fun clear() {
        database.connection.createStatement().use { statement ->
            statement.executeUpdate("DELETE FROM user_matches")
            statement.executeUpdate("DELETE FROM matches")
        }
    }

}
