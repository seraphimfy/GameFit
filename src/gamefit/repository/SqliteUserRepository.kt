package gamefit.repository

import gamefit.model.UserAccount
import java.io.Closeable
import java.sql.DriverManager

class SqliteUserRepository(
    dbPath: String = "gamefit.db"
) : UserRepository, Closeable {

    private val connection = DriverManager.getConnection("jdbc:sqlite:$dbPath")

    init {
        connection.createStatement().use { statement ->
            // Таблица пользователей
            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS users (
                    username TEXT PRIMARY KEY,
                    penalty INTEGER NOT NULL,
                    kda_target REAL NOT NULL
                );
                """.trimIndent()
            )
            // Таблица прогресса по выполненным упражнениям
            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS user_progress (
                    username TEXT NOT NULL,
                    exercise_name TEXT NOT NULL,
                    done_points INTEGER NOT NULL,
                    PRIMARY KEY (username, exercise_name)
                );
                """.trimIndent()
            )
        }
    }

    override fun getOrCreateUser(username: String): UserAccount {
        val queryUser = "SELECT penalty, kda_target FROM users WHERE username = ? LIMIT 1"
        var penalty = 0
        var kdaTarget = 2.0
        var userExists = false

        connection.prepareStatement(queryUser).use { stmt ->
            stmt.setString(1, username)
            stmt.executeQuery().use { rs ->
                if (rs.next()) {
                    penalty = rs.getInt("penalty")
                    kdaTarget = rs.getDouble("kda_target")
                    userExists = true
                }
            }
        }

        if (userExists) {
            val progress = mutableMapOf<String, Int>()
            val queryProgress = "SELECT exercise_name, done_points FROM user_progress WHERE username = ?"
            connection.prepareStatement(queryProgress).use { stmt ->
                stmt.setString(1, username)
                stmt.executeQuery().use { rs ->
                    while (rs.next()) {
                        progress[rs.getString("exercise_name")] = rs.getInt("done_points")
                    }
                }
            }
            return UserAccount(username, penalty, kdaTarget, progress)
        } else {
            val newUser = UserAccount(username)
            saveUser(newUser)
            return newUser
        }
    }

    override fun saveUser(user: UserAccount) {
        val upsertUserSql = """
            INSERT INTO users (username, penalty, kda_target)
            VALUES (?, ?, ?)
            ON CONFLICT(username) DO UPDATE SET
                penalty = excluded.penalty,
                kda_target = excluded.kda_target
        """.trimIndent()

        connection.prepareStatement(upsertUserSql).use { stmt ->
            stmt.setString(1, user.username)
            stmt.setInt(2, user.penalty)
            stmt.setDouble(3, user.kdaTarget)
            stmt.executeUpdate()
        }

        val upsertProgressSql = """
            INSERT INTO user_progress (username, exercise_name, done_points)
            VALUES (?, ?, ?)
            ON CONFLICT(username, exercise_name) DO UPDATE SET
                done_points = excluded.done_points
        """.trimIndent()

        connection.prepareStatement(upsertProgressSql).use { stmt ->
            for ((exerciseName, donePoints) in user.getAllProgress()) {
                stmt.setString(1, user.username)
                stmt.setString(2, exerciseName)
                stmt.setInt(3, donePoints)
                stmt.executeUpdate()
            }
        }
    }

    override fun clear() {
        connection.createStatement().use { stmt ->
            stmt.executeUpdate("DELETE FROM user_progress")
            stmt.executeUpdate("DELETE FROM users")
        }
    }

    override fun close() {
        if (!connection.isClosed) {
            connection.close()
        }
    }
}
