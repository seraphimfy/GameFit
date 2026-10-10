package gamefit.repository

import java.io.Closeable
import java.sql.Connection
import java.sql.DriverManager

class SqliteDatabase(
    dbPath: String = "gamefit.db"
) : Closeable {

    val connection: Connection = DriverManager.getConnection("jdbc:sqlite:$dbPath")

    fun <T> transaction(action: (Connection) -> T): T {
        val oldAutoCommit = connection.autoCommit
        connection.autoCommit = false

        try {
            val result = action(connection)
            connection.commit()
            return result
        } catch (exception: Exception) {
            connection.rollback()
            throw exception
        } finally {
            connection.autoCommit = oldAutoCommit
        }
    }

    override fun close() {
        if (!connection.isClosed) {
            connection.close()
        }
    }
}
