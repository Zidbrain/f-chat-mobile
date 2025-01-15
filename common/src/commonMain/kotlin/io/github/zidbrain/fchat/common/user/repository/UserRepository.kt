package io.github.zidbrain.fchat.common.user.repository

import io.github.zidbrain.fchat.common.host.repository.SessionRepository
import io.github.zidbrain.fchat.common.user.api.UserApi
import io.github.zidbrain.fchat.common.user.model.User
import io.github.zidbrain.fchat.common.user.model.toModel
import org.koin.core.annotation.Single

@Single
class UserRepository(private val api: UserApi, private val sessionRepository: SessionRepository) {

    private val users = mutableMapOf<String, User>()

    suspend fun getUser(id: String) = users.getOrPut(id) {
        api.userInfo(id).toModel()
    }

    val currentUser: User
        get() = sessionRepository.session.let {
            User(id = it.userId, name = it.email, email = it.email)
        }
}