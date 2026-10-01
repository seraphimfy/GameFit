package gamefit.ui

import gamefit.model.Exercise
import gamefit.model.UserAccount
import gamefit.provider.ManualMatchProvider
import gamefit.provider.MatchProvider
import gamefit.provider.OpenDotaMatchProvider
import gamefit.provider.getPlayerId
import gamefit.provider.getPlayerLim
import gamefit.repository.InMemoryMatchRepository
import gamefit.repository.MatchRepository
import gamefit.service.ExercisePlanner
import gamefit.service.MatchProcessingService
import gamefit.service.PenaltyCalculator
/*
=====TODO_LIST======
1.1 ручной ввод айдишника и кол-ва матчей
1.2 Реализовать простое локальное хранилище:( сохраянть айди в тхт или джсон, локальная бд) - сделано в озу
1.3 внедрить проверку на наличие матча в бд - проверка в озу




4.Добавить безопасную обработку сети через runCatching: перехватывать отсутствие интернета и ошибку 429 Too Many Requests (лимит бесплатного тарифа OpenDota).
5. довести до ума баланс планировщика упражнений
6. постепенный переход к тг боту или андроид приложению(когда будет готов бек начать помогать эле с фронтом)
*/
fun main() {
    val repository: MatchRepository = InMemoryMatchRepository()
    val provider: MatchProvider = OpenDotaMatchProvider(getPlayerId().toLong(), getPlayerLim())
    val calculator = PenaltyCalculator()
    val service = MatchProcessingService(calculator, repository)
    val first = UserAccount("John")

    val exercises = listOf(
        Exercise("Squats", 1, 0.5),
        Exercise("Abs", 2, 1.0),
        Exercise("Push-ups", 3, 1.5)
    )
    val planner = ExercisePlanner(exercises)

    while (true) {
        println("Choose action: 1.Import match 2.Show penalty 3.Complete penalty 4.assign plan 5.show match history 6.change KDA target 7.exit")
        val choice = readln()
        when (choice) {
            "1" -> {
                val matches = provider.getMatches()
                val results = service.processMatches(first, matches)
                if (results.isEmpty()) {
                    println("No new matches to process (already processed).")
                } else {
                    for (result in results) {
                        printPenaltyResult(result)
                    }
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
                val plan = planner.planExercise(first.penalty, first)
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