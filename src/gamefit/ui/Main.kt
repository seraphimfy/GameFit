package gamefit.ui

import gamefit.model.Exercise
import gamefit.model.UserAccount
import gamefit.provider.ManualMatchProvider
import gamefit.provider.MatchProvider
import gamefit.provider.OpenDotaMatchProvider
import gamefit.provider.getPlayerId
import gamefit.provider.getPlayerLim
import gamefit.repository.SqliteDatabase
import gamefit.repository.SqliteMatchRepository
import gamefit.repository.MatchRepository
import gamefit.repository.SqliteUserRepository
import gamefit.repository.UserRepository
import gamefit.service.ExercisePlanner
import gamefit.service.MatchProcessingService
import gamefit.service.PenaltyCalculator
import java.util.Locale

fun main() {
    val database = SqliteDatabase()
    val matchRepository: MatchRepository = SqliteMatchRepository(database)
    val userRepository: UserRepository = SqliteUserRepository(database)
    val manualProvider: MatchProvider = ManualMatchProvider()
    val calculator = PenaltyCalculator()
    val service = MatchProcessingService(calculator, matchRepository)

    println("=== GameFit CLI ===")
    print("Enter username (press Enter for 'Player'): ")
    val inputUsername = readlnOrNull()?.trim()
    val username = if (!inputUsername.isNullOrEmpty()) inputUsername else "Player"
    var first = userRepository.getOrCreateUser(username)
    println("\nWelcome to GameFit, ${first.username}!")
    println("Loaded profile: Penalty: ${first.penalty} points, KDA Target: ${String.format(Locale.US, "%.2f", first.kdaTarget)}")
    if (first.getCurrentPlan().isNotEmpty()) {
        println("Active workout plan restored from database. Select 5 to complete workout.")
    }

    val exercises = listOf(
        Exercise("Squats", 1, 0.5),
        Exercise("Abs", 2, 1.0),
        Exercise("Push-ups", 3, 1.5)
    )
    val planner = ExercisePlanner(exercises)

    while (true) {
        println(
            """

            Choose action:
            1. Import matches (OpenDota)
            2. Enter match manually
            3. Show penalty
            4. Assign exercise plan
            5. Complete workout
            6. Show match history
            7. Change KDA target
            8. Reset database
            0. Exit
            """.trimIndent()
        )
        val choice = readlnOrNull()?.trim()
        when (choice) {
            "1" -> {
                val accountId = getPlayerId()
                val limit = getPlayerLim()
                val openDotaProvider = OpenDotaMatchProvider(accountId, limit)
                try {
                    val matches = openDotaProvider.getMatches()
                    val results = service.processMatches(first, matches)
                    if (results.isEmpty()) {
                        println("No new matches to process (already processed or failed to fetch).")
                    } else {
                        for (result in results) {
                            printPenaltyResult(result)
                        }
                    }
                } finally {
                    openDotaProvider.close()
                }
            }
            "2" -> {
                val matches = manualProvider.getMatches()
                val results = service.processMatches(first, matches)
                if (results.isEmpty()) {
                    println("Match already processed.")
                } else {
                    for (result in results) {
                        printPenaltyResult(result)
                    }
                }
            }
            "3" -> {
                printPenalty(first.penalty)
            }
            "4" -> {
                val plan = planner.planExercise(first.penalty, first)
                if (first.assignPlan(plan)) {
                    userRepository.saveUser(first)
                    printAssignedPlan(plan)
                } else {
                    printNoPenalty()
                }
            }
            "5" -> {
                val completedPlan = first.completeWork()
                if (completedPlan.isEmpty()) {
                    printNoAssignedPlan()
                } else {
                    userRepository.saveUser(first)
                    printCompletedWork(completedPlan)
                }
            }
            "6" -> {
                printMatchHistory(matchRepository.getAll(first.username))
            }
            "7" -> {
                println("Enter target KDA (e.g. 2.5):")
                val inputTarget = readlnOrNull()?.trim()?.replace(',', '.')
                val target = inputTarget?.toDoubleOrNull()
                if (target != null && first.changeTarget(target)) {
                    userRepository.saveUser(first)
                    printTargetChanged(first.kdaTarget)
                } else {
                    printInvalidTarget()
                }
            }
            "8" -> {
                matchRepository.clear()
                userRepository.clear()
                first = userRepository.getOrCreateUser(username)
                println("Database cleared. Profile reset to default.")
            }
            "0" -> {
                database.close()
                println("Goodbye!")
                return
            }
            else -> println("Invalid choice. Please select an option from the menu.")
        }
    }
}
