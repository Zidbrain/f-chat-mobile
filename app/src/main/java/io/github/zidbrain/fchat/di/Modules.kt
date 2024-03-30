package io.github.zidbrain.fchat.di

import androidx.credentials.CredentialManager
import io.github.zidbrain.fchat.android.account.storage.AndroidSsoStorage
import io.github.zidbrain.fchat.common.account.encryption.AndroidEncryptionService
import io.github.zidbrain.fchat.common.account.encryption.EncryptionService
import io.github.zidbrain.fchat.common.account.storage.AndroidEncryptedStorage
import io.github.zidbrain.fchat.common.account.storage.EncryptedStorage
import io.github.zidbrain.fchat.common.account.storage.SsoStorage
import io.github.zidbrain.fchat.common.contacts.api.ContactsApi
import io.github.zidbrain.fchat.common.contacts.viewmodel.AddContactViewModel
import io.github.zidbrain.fchat.common.contacts.viewmodel.ContactsViewModel
import io.github.zidbrain.fchat.common.host.repository.SessionRepository
import io.github.zidbrain.fchat.common.host.viewmodel.HostViewModel
import io.github.zidbrain.fchat.common.login.api.LoginApi
import io.github.zidbrain.fchat.common.login.repository.LoginRepository
import io.github.zidbrain.fchat.common.login.viewmodel.LoginViewModel
import io.github.zidbrain.fchat.common.main.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.qualifier
import org.koin.dsl.module

val accountModule = module {
    single<EncryptedStorage> { AndroidEncryptedStorage(get()) }
    single<EncryptionService> { AndroidEncryptionService() }
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

val contactsModule = module {
    single { ContactsApi(get(qualifier(ClientType.Authorized))) }
    viewModelOf(::ContactsViewModel)
    viewModelOf(::AddContactViewModel)
}

val allModules =
    clientModule + accountModule + hostModule + loginModule + mainModule + contactsModule