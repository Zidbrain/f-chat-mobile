package io.github.zidbrain.fchat.common.user.api

import io.github.zidbrain.fchat.common.contacts.api.dto.UserDto
import io.github.zidbrain.fchat.common.di.ClientType
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class UserApi(
    @Named(ClientType.AUTHORIZED)
    private val client: HttpClient
) {

    suspend fun userInfo(userId: String): UserDto = client.get("/user/$userId")
        .body()
}