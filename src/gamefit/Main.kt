
fun main()
{
    val provider: MatchProvider = ManualMatchProvider()
    val calculator: PenaltyCalculator = PenaltyCalculator()
    val service: MatchProcessingService = MatchProcessingService(calculator)
    val first: UserAccount = UserAccount("John")
    while(true) {
        println("Choose action: 1.Import match 2.Show penalty 3.Complete penalty 4.reset 5.show match history 6.change KDA target 7.exit")
        val choice = readln()
        when (choice) {
            "1" -> {
                val matches = provider.getMatches()
                service.processMatches(first, matches)
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

