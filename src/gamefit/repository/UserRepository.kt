package gamefit.repository

import gamefit.model.UserAccount

interface UserRepository {
    /** Загружает пользователя из базы или создаёт нового по умолчанию */
    fun getOrCreateUser(username: String): UserAccount

    /** Сохраняет текущие штрафы, цель KDA и прогресс тренировок */
    fun saveUser(user: UserAccount)

    /** Полная очистка таблицы пользователей */
    fun clear()
}
