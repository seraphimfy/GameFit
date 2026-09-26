
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
                val results = service.processMatches(first, matches)
                for (result in results) {
                    printPenaltyResult(result)
                }
            }
            "2" -> {
                printPenalty(first.penalty)
            }
            "3" -> {
                val completedPlan = first.completeWork()

                if (completedPlan.isEmpty()) {
                    printNoAssignedPlan()
                } else {
                    printCompletedWork(completedPlan)
                }
            }
            "4" -> {
                val plan = planner.planExercise(first.penalty)

                if (first.assignPlan(plan)) {
                    printAssignedPlan(plan)
                } else {
                    printNoPenalty()
                }
            }
            "5" -> {
                printMatchHistory(first.getMatches())
            }
            "6" -> {
                println("Enter target KDA")

                val target = readln().toDouble()

                if (first.changeTarget(target)) {
                    printTargetChanged(first.kdaTarget)
                } else {
                    printInvalidTarget()
                }
            }
            "7" -> return
            else -> println("Invalid choice")
        }
    }
}

