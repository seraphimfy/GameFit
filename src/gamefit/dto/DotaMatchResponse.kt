package gamefit.dto

import gamefit.model.Match
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DotaMatchResponse(
    @SerialName("match_id") val matchId: Long,
    @SerialName("player_slot") val playerSlot: Int,
    @SerialName("radiant_win") val radiantWin: Boolean,
    @SerialName("kills") val kills: Int,
    @SerialName("deaths") val deaths: Int,
    @SerialName("assists") val assists: Int
) {
    fun toMatch(): Match {
        // В Dota 2 слоты 0..127 — Radiant, 128..255 — Dire
        val isRadiant = playerSlot < 128
        val isWin = (isRadiant == radiantWin)

        return Match(
            id = matchId,
            kills = kills,
            deaths = deaths,
            assists = assists,
            matchWon = isWin
        )
    }
}