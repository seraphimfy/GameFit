data class Exercise(
    val name: String,
    val points: Int,
    val baseLoadCoefficient: Double
) {
    fun calculateLoadCoefficient(donePoints: Int): Double {
        return baseLoadCoefficient * (1 + donePoints / 100.0)
    }
}