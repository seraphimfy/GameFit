package gamefit.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import gamefit.model.Match

// Состояние пользователя, сохранённое в таблице users.
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val username: String,
    val penalty: Int,
    @ColumnInfo(name = "kda_target")
    val kdaTarget: Double
)

// Выполненный матч, принадлежащий конкретному пользователю.
@Entity(
    tableName = "matches",
    primaryKeys = ["username", "match_id"]
)
data class MatchEntity(
    val username: String,
    @ColumnInfo(name = "match_id")
    val matchId: Long,
    val kills: Int,
    val deaths: Int,
    val assists: Int,
    @ColumnInfo(name = "match_won")
    val matchWon: Boolean
) {
    // Преобразование строки Room в доменную модель.
    fun toModel(): Match = Match(
        id = matchId,
        kills = kills,
        deaths = deaths,
        assists = assists,
        matchWon = matchWon
    )

    companion object {
        // Преобразование доменной модели в сущность Room.
        fun fromModel(username: String, match: Match): MatchEntity = MatchEntity(
            username = username,
            matchId = match.id,
            kills = match.kills,
            deaths = match.deaths,
            assists = match.assists,
            matchWon = match.matchWon
        )
    }
}

// Накопленный прогресс пользователя по упражнению.
@Entity(tableName = "user_progress", primaryKeys = ["username", "exercise_name"])
data class ProgressEntity(
    val username: String,
    @ColumnInfo(name = "exercise_name")
    val exerciseName: String,
    @ColumnInfo(name = "done_points")
    val donePoints: Int
)

// Активный план тренировки пользователя.
@Entity(tableName = "user_plan", primaryKeys = ["username", "exercise_name"])
data class PlanEntity(
    val username: String,
    @ColumnInfo(name = "exercise_name")
    val exerciseName: String,
    val repetitions: Int,
    val points: Int,
    @ColumnInfo(name = "base_load")
    val baseLoad: Double
)
