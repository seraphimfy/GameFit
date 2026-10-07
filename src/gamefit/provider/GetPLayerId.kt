package gamefit.provider

/**
 * Запрашивает SteamID у пользователя и преобразует его в 32-битный Account ID (Long).
 * Поддерживает форматы SteamID32 и SteamID64 с защитой от некорректного ввода.
 */
fun getPlayerId(): Long {
    val steamId64Base = 76561197960265728L

    while (true) {
        println("SteamId32 or SteamId64? (enter 1/2):")
        val formatChoice = readlnOrNull()?.trim()
        if (formatChoice != "1" && formatChoice != "2") {
            println("Invalid choice. Please enter 1 for SteamID32 or 2 for SteamID64.")
            continue
        }

        println("Enter your Steam ID:")
        val rawId = readlnOrNull()?.trim()?.toLongOrNull()
        if (rawId == null || rawId <= 0) {
            println("Invalid Steam ID. It must be a positive number. Try again.")
            continue
        }

        return when (formatChoice) {
            "1" -> rawId
            "2" -> {
                val accountId = rawId - steamId64Base
                if (accountId <= 0) {
                    println("The provided SteamID64 is invalid (resulted in non-positive account ID). Try again.")
                    continue
                }
                accountId
            }
            else -> continue
        }
    }
}

/**
 * Запрашивает у пользователя количество последних матчей для загрузки (от 1 до 20).
 */
fun getPlayerLim(): Int {
    while (true) {
        println("Enter limit of recent matches (1 to 20, default 10):")
        val input = readlnOrNull()?.trim()
        if (input.isNullOrEmpty()) {
            return 10
        }
        val limit = input.toIntOrNull()
        if (limit != null && limit in 1..20) {
            return limit
        }
        println("Limit must be a number between 1 and 20.")
    }
}