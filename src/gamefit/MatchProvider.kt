interface MatchProvider {
    fun getMatches(): List<Match>
}
class ManualMatchProvider : MatchProvider {
    override fun getMatches(): List<Match> {
        while (true) {
            println("Enter KDA and match result divided by space (e.g. '10 2 5 win'):")
            val input = readlnOrNull()?.trim()

            if (input.isNullOrEmpty()) {
                println("Input cannot be empty. Try again.")
                continue
            }

            // \\s+ разбивает строку по любому числу пробелов или табов подряд
            val parts = input.split("\\s+".toRegex())
            if (parts.size != 4) {
                println("Error: expected exactly 4 values (Kills Deaths Assists Result).")
                continue
            }

            val kills = parts[0].toIntOrNull()
            val deaths = parts[1].toIntOrNull()
            val assists = parts[2].toIntOrNull()
            val isWin = parseMatchResult(parts[3])

            if (kills == null || deaths == null || assists == null || isWin == null) {
                println("Error: K, D, A must be valid numbers, and result must be win/loss (or true/false).")
                continue
            }

            val match = Match(kills, deaths, assists, isWin)
            return listOf(match)
        }
    }

    private fun parseMatchResult(value: String): Boolean? = when (value.lowercase()) {
        "true", "w", "win", "1" -> true
        "false", "l", "loss", "lose", "0" -> false
        else -> null
    }
}