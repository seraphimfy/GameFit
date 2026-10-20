package gamefit.provider

import gamefit.dto.DotaMatchResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import java.io.Closeable
import java.io.IOException
import java.net.UnknownHostException
import kotlinx.serialization.json.Json

// Android-реализация MatchProvider на базе Ktor и API OpenDota.
class OpenDotaMatchProvider(
    private val client: HttpClient = createDefaultClient()
) : MatchProvider, Closeable {

    override suspend fun getMatches(accountId: Long, limit: Int): MatchProviderResult {
        // Проверяем параметры до выполнения сетевого запроса.
        if (accountId <= 0) {
            return MatchProviderResult.Failure.InvalidRequest(
                message = "Account ID должен быть положительным"
            )
        }

        if (limit <= 0) {
            return MatchProviderResult.Failure.InvalidRequest(
                message = "Количество матчей должно быть положительным"
            )
        }

        return try {
            // OpenDota возвращает последние матчи указанного игрока.
            val response: HttpResponse = client.get(
                "https://api.opendota.com/api/players/$accountId/recentMatches"
            )

            when (response.status) {
                HttpStatusCode.OK -> {
                    val matches = response
                        .body<List<DotaMatchResponse>>()
                        .take(limit)
                        .map(DotaMatchResponse::toMatch)

                    MatchProviderResult.Success(matches)
                }

                HttpStatusCode.TooManyRequests -> {
                    MatchProviderResult.Failure.RateLimited
                }

                HttpStatusCode.NotFound -> {
                    MatchProviderResult.Failure.NotFound
                }

                else -> {
                    MatchProviderResult.Failure.Unknown(
                        IllegalStateException(
                            "OpenDota вернул HTTP ${response.status.value} " +
                                response.status.description
                        )
                    )
                }
            }
        } catch (_: UnknownHostException) {
            MatchProviderResult.Failure.Network
        } catch (_: IOException) {
            MatchProviderResult.Failure.Network
        } catch (exception: Exception) {
            MatchProviderResult.Failure.Unknown(exception)
        }
    }

    override fun close() {
        // Освобождаем ресурсы HTTP-клиента при уничтожении Activity.
        client.close()
    }

    companion object {
        // Создаёт клиент с Android engine и JSON-десериализацией.
        fun createDefaultClient(): HttpClient = HttpClient(Android) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        coerceInputValues = true
                    }
                )
            }
        }
    }
}
