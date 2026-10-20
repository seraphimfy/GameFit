package gamefit.model
data class PenaltyResult(
    val kda: Double,
    val target: Double,
    val kdaResult: KdaPerformance,
    val matchWon: Boolean,
    val penaltyPoints: Int
)