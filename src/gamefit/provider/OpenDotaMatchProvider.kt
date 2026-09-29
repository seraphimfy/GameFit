package gamefit.provider

import gamefit.dto.DotaMatchResponse
import gamefit.model.Match
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

class OpenDotaMatchProvider(
    private val accountId: Long,
    private val limit: Int,
    private val client: HttpClient = createDefaultClient()
) : MatchProvider {

    override fun getMatches(): List<Match> = runBlocking {
        try {
            val response: List<DotaMatchResponse> = client
                .get("https://api.opendota.com/api/players/$accountId/recentMatches")
                .body()

            response.take(limit).map { it.toMatch() }
        } catch (e: Exception) {
            println("Failed to fetch matches from OpenDota: ${e.message}")
            emptyList()
        }
    }

    companion object {
        fun createDefaultClient() = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                })
            }
        }
    }
}