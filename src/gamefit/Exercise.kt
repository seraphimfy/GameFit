data class Exercise(
    val name: String,
    val points: Int,
    val baseLoadCoefficient: Double,
    var donePoints: Int = 0
) {
    val loadCoefficient: Double
        get() = baseLoadCoefficient * (1 + donePoints / 100.0)
}