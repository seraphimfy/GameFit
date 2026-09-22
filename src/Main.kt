class UserAccount(val username: String) {
    var penalty: Int = 0
        private set
    private val matches = mutableListOf<Match>()
    var kdaTarget:Double = 2.0
    fun addPenalty(penaltyValue:Int){
        if(penaltyValue > 0) {
            penalty += penaltyValue
            println("Penalty: $penalty")
        }
    }
    fun resetPenalty(){
        penalty = 0
        println("Penalty: $penalty, reset completed")
    }
    fun completeWork(repsDone: Int)
    {
        if(repsDone <= 0 || penalty == 0) return
        if(repsDone < penalty)
        {
            println("Too weak, son")
            penalty -= repsDone
        }
        else {
            println("Well done")
            resetPenalty()
        }
    }
    fun addMatch(match: Match){
        matches.add(match)
    }
    fun showMatches()
    {
        for(match in matches)
        {
            println("Match: $match")
        }
    }
    fun changeTarget(target: Double) {
        if (target <= 0) {
            println("Target must be greater than 0")
            return
        }
        kdaTarget = target
        println("KDA target changed to $kdaTarget")
    }
}
data class Match(val kills: Int, val deaths: Int, val assists: Int, val matchWon: Boolean){
    val kda:Double
        get() = (kills+assists).toDouble()/ deaths.coerceAtLeast(1)
}
class PenaltyCalculator(){
    fun evaluate(match: Match, target: Double): Int{
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

fun importMatch(): Match {
    println("Enter KDA and match result divided by space")
    val input = readln()
    val parts = input.split(" ")
    return Match(kills = parts[0].toInt(), deaths = parts[1].toInt(), assists = parts[2].toInt(), matchWon = parts[3].toBoolean())
}
fun main()
{
    val calculator: PenaltyCalculator = PenaltyCalculator()
    val first: UserAccount = UserAccount("John")
    while(true) {
        println("Choose action: 1.Import match 2.Show penalty 3.Complete penalty 4.reset 5.show match history 6.change KDA target 7.exit")
        val choice = readln()
        when (choice) {
            "1" -> {
                val lastGame = importMatch()
                first.addMatch(lastGame)
                first.addPenalty(calculator.evaluate(lastGame, first.kdaTarget))

            }
            "2" -> {
                val a = first.penalty
                println("Penalty: $a")

            }
            "3" -> {
                println("Enter num of reps")
                val reps = readln()
                first.completeWork(reps.toInt())

            }
            "4" ->
            {
                first.resetPenalty()
            }
            "5" -> {
                first.showMatches()
            }
            "6" -> {
                println("Enter target KDA")
                first.changeTarget(readln().toDouble())
            }
            "7" -> return
            else -> println("Invalid choice")
        }
    }

}