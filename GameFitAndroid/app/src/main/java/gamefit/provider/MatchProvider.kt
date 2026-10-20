package gamefit.provider

import gamefit.model.Match

// Контракт источника матчей, не зависящий от Ktor или конкретной платформы.
/**
 * Источник матчей. Контракт не зависит от HTTP-клиента и конкретной платформы.
 *
 * Реализация может получать матчи из OpenDota, локального кэша или тестового
 * источника, не меняя бизнес-логику приложения.
 */
interface MatchProvider {
    suspend fun getMatches(accountId: Long, limit: Int): MatchProviderResult
}

sealed interface MatchProviderResult {
    // Успешный ответ с импортированными матчами.
    data class Success(val matches: List<Match>) : MatchProviderResult

    // Типизированные ошибки, которые UI может показать пользователю.
    sealed interface Failure : MatchProviderResult {
        data object Network : Failure
        data object RateLimited : Failure
        data object NotFound : Failure
        data class InvalidRequest(val message: String) : Failure
        data class Unknown(val cause: Throwable? = null) : Failure
    }
}
