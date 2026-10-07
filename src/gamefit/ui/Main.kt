package gamefit.ui

import gamefit.model.Exercise
import gamefit.model.UserAccount
import gamefit.provider.ManualMatchProvider
import gamefit.provider.MatchProvider
import gamefit.provider.OpenDotaMatchProvider
import gamefit.provider.getPlayerId
import gamefit.provider.getPlayerLim
import gamefit.repository.SqliteMatchRepository
import gamefit.repository.MatchRepository
import gamefit.repository.SqliteUserRepository
import gamefit.repository.UserRepository
import gamefit.service.ExercisePlanner
import gamefit.service.MatchProcessingService
import gamefit.service.PenaltyCalculator
import java.io.Closeable
import java.util.Locale

fun main() {
    val matchRepository: MatchRepository = SqliteMatchRepository()
    val userRepository: UserRepository = SqliteUserRepository()
    val openDotaProvider: MatchProvider = OpenDotaMatchProvider(getPlayerId(), getPlayerLim())
    val manualProvider: MatchProvider = ManualMatchProvider()
    val calculator = PenaltyCalculator()
    val service = MatchProcessingService(calculator, matchRepository)
    val first = userRepository.getOrCreateUser("John")
    println("\nWelcome to GameFit, ${first.username}!")
    println("Loaded profile: Penalty: ${first.penalty} points, KDA Target: ${String.format(Locale.US, "%.2f", first.kdaTarget)}")

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
                val matches = openDotaProvider.getMatches()
                val results = service.processMatches(first, matches)
                if (results.isEmpty()) {
                    println("No new matches to process (already processed or failed to fetch).")
                } else {
                    userRepository.saveUser(first)
                    for (result in results) {
                        printPenaltyResult(result)
                    }
                }
            }
            "2" -> {
                val matches = manualProvider.getMatches()
                val results = service.processMatches(first, matches)
                if (results.isEmpty()) {
                    println("Match already processed.")
                } else {
                    userRepository.saveUser(first)
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
                printMatchHistory(first.getMatches())
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
                println("Database cleared.")
            }
            "0" -> {
                (userRepository as? Closeable)?.close()
                (matchRepository as? Closeable)?.close()
                (openDotaProvider as? Closeable)?.close()
                println("Goodbye!")
                return
            }
            else -> println("Invalid choice. Please select an option from the menu.")
        }
    }
}