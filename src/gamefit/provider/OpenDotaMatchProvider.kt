package gamefit.provider

import gamefit.dto.DotaMatchResponse
import gamefit.model.Match
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.io.Closeable
import java.net.UnknownHostException

class OpenDotaMatchProvider(
    private val accountId: Long,
    private val limit: Int,
    private val client: HttpClient = createDefaultClient()
) : MatchProvider, Closeable {

    override fun getMatches(): List<Match> = runBlocking {
        try {
            val httpResponse: HttpResponse = client.get("https://api.opendota.com/api/players/$accountId/recentMatches")

            when (httpResponse.status) {
                HttpStatusCode.OK -> {
                    val dotaMatches: List<DotaMatchResponse> = httpResponse.body()
                    if (dotaMatches.isEmpty()) {
                        println("OpenDota returned 0 matches. Ensure 'Expose Public Match Data' is enabled in your Dota 2 settings.")
                    }
                    dotaMatches.take(limit).map { it.toMatch() }
                }
                HttpStatusCode.TooManyRequests -> {
                    println("OpenDota rate limit exceeded (HTTP 429 Too Many Requests). Please wait a minute before retrying.")
                    emptyList()
                }
                HttpStatusCode.NotFound -> {
                    println("Player with account ID $accountId not found on OpenDota (HTTP 404).")
                    emptyList()
                }
                else -> {
                    println("Failed to fetch matches: HTTP ${httpResponse.status.value} ${httpResponse.status.description}")
                    emptyList()
                }
            }
        } catch (e: UnknownHostException) {
            println("Network error: Cannot reach OpenDota servers. Please check your internet connection.")
            emptyList()
        } catch (e: Exception) {
            println("Failed to fetch matches from OpenDota: ${e.message}")
            emptyList()
        }
    }

    override fun close() {
        client.close()
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