package io.github.zidbrain.fchat.common.user.api

import io.github.zidbrain.fchat.common.contacts.api.dto.UserDto
import io.github.zidbrain.fchat.common.di.AppScope
import io.github.zidbrain.fchat.common.di.Authorized
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import me.tatarka.inject.annotations.Inject

@AppScope
@Inject
class UserApi(
    @Authorized
    private val client: HttpClient
) {

    suspend fun userInfo(userId: String): UserDto = client.get("/user/$userId")
        .body()
}