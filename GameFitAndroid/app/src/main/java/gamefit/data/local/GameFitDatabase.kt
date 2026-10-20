package gamefit.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

// Room-база приложения. По структуре повторяет SQLite-схему CLI.
@Database(
    entities = [
        UserEntity::class,
        MatchEntity::class,
        ProgressEntity::class,
        PlanEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class GameFitDatabase : RoomDatabase() {
    // Единственная точка доступа к SQL-операциям.
    abstract fun gameFitDao(): GameFitDao

    companion object {
        @Volatile
        private var instance: GameFitDatabase? = null

        fun getInstance(context: Context): GameFitDatabase {
            // Singleton предотвращает создание нескольких подключений к одной БД.
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    GameFitDatabase::class.java,
                    "gamefit.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Версия 2 добавляет прогресс и активный план без удаления матчей.
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS user_progress (
                        username TEXT NOT NULL,
                        exercise_name TEXT NOT NULL,
                        done_points INTEGER NOT NULL,
                        PRIMARY KEY(username, exercise_name)
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS user_plan (
                        username TEXT NOT NULL,
                        exercise_name TEXT NOT NULL,
                        repetitions INTEGER NOT NULL,
                        points INTEGER NOT NULL,
                        base_load REAL NOT NULL,
                        PRIMARY KEY(username, exercise_name)
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
