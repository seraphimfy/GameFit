
fun main()
{
    val provider: MatchProvider = ManualMatchProvider()
    val calculator: PenaltyCalculator = PenaltyCalculator()
    val service: MatchProcessingService = MatchProcessingService(calculator)
    val first: UserAccount = UserAccount("John")
    val exercises = listOf(
        Exercise("Squats", 1, 0.5, 0),
        Exercise("Abs", 2,1.0,0),
        Exercise("Push-ups", 3,1.5,0)
    )

    val planner = ExercisePlanner(exercises)


    while(true) {
        println("Choose action: 1.Import match 2.Show penalty 3.Complete penalty 4.assign plan 5.show match history 6.change KDA target 7.exit")
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
                first.completeWork()

            }
            "4" ->
            {
                val plan = planner.planExercise(first.penalty)
                first.assignPlan(plan)
                println("Assigned plan:")

                for ((exercise, repetitions) in plan) {
                    println("${exercise.name}: $repetitions repetitions")
                }
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

