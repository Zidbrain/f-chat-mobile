package io.github.zidbrain.fchat.common.di

import io.github.zidbrain.fchat.common.account.storage.EncryptedStorage
import io.github.zidbrain.fchat.common.chat.repository.ChatRepository
import io.github.zidbrain.fchat.common.host.repository.SessionRepository
import io.github.zidbrain.fchat.common.login.repository.LoginRepository
import me.tatarka.inject.annotations.Component

@Component
@SessionScope
abstract class SessionComponent(@Component protected val appComponent: AppComponent) {
    abstract val encryptedStorage: EncryptedStorage
    abstract val chatRepository: ChatRepository
    abstract val sessionRepository: SessionRepository
    abstract val loginRepository: LoginRepository
}