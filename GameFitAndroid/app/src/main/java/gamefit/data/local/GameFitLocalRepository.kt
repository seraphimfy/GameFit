package gamefit.data.local

import androidx.room.withTransaction
import gamefit.model.Exercise
import gamefit.model.UserAccount

// Репозиторий скрывает Room от Activity и восстанавливает UserAccount целиком.
class GameFitLocalRepository(
    private val database: GameFitDatabase
) {
    private val dao = database.gameFitDao()

    suspend fun loadAccount(username: String): UserAccount {
        // Новому пользователю создаётся профиль с начальными значениями.
        val user = dao.findUser(username)
        if (user == null) {
            val account = UserAccount(username)
            saveAccount(account)
            return account
        }

        // Загружаем все части состояния так же, как SqliteUserRepository в CLI.
        val matches = dao.findMatches(username).map { it.toModel() }
        val progress = dao.findProgress(username).associate {
            it.exerciseName to it.donePoints
        }
        val plan = dao.findPlan(username).associate {
            Exercise(
                name = it.exerciseName,
                points = it.points,
                baseLoadCoefficient = it.baseLoad
            ) to it.repetitions
        }
        val account = UserAccount(
            username = user.username,
            initialPenalty = user.penalty,
            initialKdaTarget = user.kdaTarget,
            initialProgress = progress,
            initialPlan = plan
        )
        matches.forEach(account::addMatch)
        return account
    }

    suspend fun saveAccount(account: UserAccount) {
        // Одна транзакция сохраняет профиль, прогресс, план и историю согласованно.
        database.withTransaction {
            dao.saveUser(
                UserEntity(
                    username = account.username,
                    penalty = account.penalty,
                    kdaTarget = account.kdaTarget
                )
            )
            dao.deleteProgress(account.username)
            dao.saveProgress(
                account.getAllProgress().map { (exerciseName, donePoints) ->
                    ProgressEntity(
                        username = account.username,
                        exerciseName = exerciseName,
                        donePoints = donePoints
                    )
                }
            )
            dao.deletePlan(account.username)
            dao.savePlan(
                account.getCurrentPlan().map { (exercise, repetitions) ->
                    PlanEntity(
                        username = account.username,
                        exerciseName = exercise.name,
                        repetitions = repetitions,
                        points = exercise.points,
                        baseLoad = exercise.baseLoadCoefficient
                    )
                }
            )
            dao.saveMatches(
                account.getMatches().map { match ->
                    MatchEntity.fromModel(account.username, match)
                }
            )
        }
    }

    suspend fun clearAccount(username: String) {
        // Полный сброс локальных данных пользователя.
        database.withTransaction {
            dao.deleteMatches(username)
            dao.deleteProgress(username)
            dao.deletePlan(username)
            dao.deleteUser(username)
        }
    }
}
