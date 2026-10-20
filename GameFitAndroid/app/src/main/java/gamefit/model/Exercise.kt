package gamefit.model

// Описание упражнения и его вклад в погашение штрафных очков.
data class Exercise(
    val name: String,
    val points: Int,
    val baseLoadCoefficient: Double
) {
    // Нагрузка постепенно увеличивается по мере накопления выполненных очков.
    fun calculateLoadCoefficient(donePoints: Int): Double {
        return baseLoadCoefficient * (1 + donePoints / 100.0)
    }
}
