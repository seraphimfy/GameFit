package gamefit.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

// SQL-операции локальной базы данных GameFit.
@Dao
interface GameFitDao {
    // Профиль пользователя.
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun findUser(username: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUser(user: UserEntity)

    // Прогресс выполненных упражнений.
    @Query("SELECT * FROM user_progress WHERE username = :username")
    suspend fun findProgress(username: String): List<ProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: List<ProgressEntity>)

    @Query("DELETE FROM user_progress WHERE username = :username")
    suspend fun deleteProgress(username: String)

    // Текущий активный план тренировки.
    @Query("SELECT * FROM user_plan WHERE username = :username")
    suspend fun findPlan(username: String): List<PlanEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlan(plan: List<PlanEntity>)

    @Query("DELETE FROM user_plan WHERE username = :username")
    suspend fun deletePlan(username: String)

    // История матчей пользователя.
    @Query(
        "SELECT * FROM matches " +
            "WHERE username = :username ORDER BY match_id"
    )
    suspend fun findMatches(username: String): List<MatchEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMatches(matches: List<MatchEntity>)

    @Query("DELETE FROM matches WHERE username = :username")
    suspend fun deleteMatches(username: String)

    @Query("DELETE FROM users WHERE username = :username")
    suspend fun deleteUser(username: String)
}
