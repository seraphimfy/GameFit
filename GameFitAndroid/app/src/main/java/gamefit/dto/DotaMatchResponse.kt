package gamefit.dto

import gamefit.model.Match
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// DTO ответа OpenDota API для последних матчей игрока.
@Serializable
data class DotaMatchResponse(
    @SerialName("match_id") val matchId: Long,
    @SerialName("player_slot") val playerSlot: Int,
    @SerialName("radiant_win") val radiantWin: Boolean,
    val kills: Int,
    val deaths: Int,
    val assists: Int
) {
    // Преобразует внешний ответ API во внутреннюю модель приложения.
    fun toMatch(): Match {
        // Слоты 0..127 принадлежат Radiant, остальные — Dire.
        val isRadiant = playerSlot < 128
        val matchWon = isRadiant == radiantWin

        return Match(
            id = matchId,
            kills = kills,
            deaths = deaths,
            assists = assists,
            matchWon = matchWon
        )
    }
}
