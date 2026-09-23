class PenaltyCalculator(){
    fun calculatePenalty(match: Match, target: Double): Int{
        val currKDA = match.kda
        println("Your KDA: $currKDA")
        return when {
            currKDA >= target -> {
                println("NICE WORK")
                if(!match.matchWon)
                    15 else 0
            }
            currKDA > 1 -> {
                println("OK")
                if(!match.matchWon)
                    20 else 10
            }
            else -> {
                println("BAD")
                if(!match.matchWon)
                    40 else 30
            }
        }
    }
}