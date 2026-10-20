package gamefit.repository

import gamefit.model.UserAccount

interface UserRepository {

    fun getOrCreateUser(username: String): UserAccount


    fun saveUser(user: UserAccount)


    fun clear()
}
