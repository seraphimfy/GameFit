package gamefit.model

// Результат расчёта штрафа за один матч.
data class PenaltyResult(
    val kda: Double,
    val target: Double,
    val kdaResult: KdaPerformance,
    val matchWon: Boolean,
    val penaltyPoints: Int
)
