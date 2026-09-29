package gamefit.model
data class PenaltyResult(
    val kda: Double,
    val target: Double,
    val kdaResult: KdaPerformance, // Теперь строго типизировано
    val matchWon: Boolean,
    val penaltyPoints: Int
)