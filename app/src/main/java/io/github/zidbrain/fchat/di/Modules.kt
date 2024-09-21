package io.github.zidbrain.fchat.di

import androidx.credentials.CredentialManager
import app.cash.sqldelight.EnumColumnAdapter
import io.github.zidbrain.Database
import io.github.zidbrain.MessageEntity
import io.github.zidbrain.fchat.android.account.storage.AndroidSsoStorage
import io.github.zidbrain.fchat.common.account.cryptography.AndroidCryptographyService
import io.github.zidbrain.fchat.common.account.cryptography.CryptographyService
import io.github.zidbrain.fchat.common.account.storage.AndroidEncryptedStorage
import io.github.zidbrain.fchat.common.account.storage.EncryptedStorage
import io.github.zidbrain.fchat.common.account.storage.SsoStorage
import io.github.zidbrain.fchat.common.contacts.api.ContactsApi
import io.github.zidbrain.fchat.common.contacts.local.ContactsDao
import io.github.zidbrain.fchat.common.contacts.repository.ContactsRepository
import io.github.zidbrain.fchat.common.contacts.viewmodel.ContactsViewModel
import io.github.zidbrain.fchat.common.conversation.api.ConversationApi
import io.github.zidbrain.fchat.common.conversation.local.ConversationDao
import io.github.zidbrain.fchat.common.conversation.repository.ConversationRepository
import io.github.zidbrain.fchat.common.conversation.viewmodel.ConversationListViewModel
import io.github.zidbrain.fchat.common.conversation.viewmodel.ConversationViewModel
import io.github.zidbrain.fchat.common.database.DriverFactory
import io.github.zidbrain.fchat.common.host.repository.SessionRepository
import io.github.zidbrain.fchat.common.host.viewmodel.HostViewModel
import io.github.zidbrain.fchat.common.login.api.LoginApi
import io.github.zidbrain.fchat.common.login.repository.LoginRepository
import io.github.zidbrain.fchat.common.login.viewmodel.LoginViewModel
import io.github.zidbrain.fchat.common.main.MainViewModel
import io.github.zidbrain.fchat.common.user.api.UserApi
import io.github.zidbrain.fchat.common.user.repository.UserRepository
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.qualifier
import org.koin.dsl.module

val accountModule = module {
    single<EncryptedStorage> { AndroidEncryptedStorage(get()) }
    single<CryptographyService> { AndroidCryptographyService() }
}

val hostModule = module {
    singleOf(::SessionRepository)
    viewModelOf(::HostViewModel)
}

val loginModule = module {
    single { CredentialManager.create(get()) }
    single<SsoStorage> { AndroidSsoStorage(get()) }
    single { LoginApi(get(qualifier(ClientType.Unauthorized))) }
    singleOf(::LoginRepository)
    viewModelOf(::LoginViewModel)
}

val mainModule = module {
    viewModelOf(::MainViewModel)
}

val conversationModule = module {
    singleOf(::ConversationRepository)
    single { ConversationApi(get(qualifier(ClientType.Authorized))) }
    singleOf(::ConversationDao)
    viewModelOf(::ConversationListViewModel)
    viewModelOf(::ConversationViewModel)
}

val userModule = module {
    single { UserApi(get(qualifier(ClientType.Authorized))) }
    singleOf(::UserRepository)
}

val contactsModule = module {
    single { ContactsApi(get(qualifier(ClientType.Authorized))) }
    singleOf(::ContactsDao)
    singleOf(::ContactsRepository)
    viewModelOf(::ContactsViewModel)
}

val databaseModule = module {
    single(createdAtStart = true) {
        val driver = DriverFactory(get()).createDriver()
        Database(
            driver = driver,
            messageEntityAdapter = MessageEntity.Adapter(
                statusAdapter = EnumColumnAdapter()
            )
        )
    }
}

val allModules =
    clientModule + accountModule + hostModule + loginModule + mainModule +
            contactsModule + databaseModule + conversationModule + userModule