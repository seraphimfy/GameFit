package gamefit.model

// Унифицированная модель матча для UI, бизнес-логики и локальной БД.
data class Match(
    val id: Long,
    val kills: Int,
    val deaths: Int,
    val assists: Int,
    val matchWon: Boolean
) {
    // При смерти 0 используется 1, чтобы избежать деления на ноль.
    val kda: Double
        get() = (kills + assists).toDouble() / deaths.coerceAtLeast(1)
}
