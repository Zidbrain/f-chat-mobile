package io.github.zidbrain.fchat.di

import android.content.Context
import androidx.credentials.CredentialManager
import app.cash.sqldelight.EnumColumnAdapter
import io.github.zidbrain.Database
import io.github.zidbrain.MessageEntity
import io.github.zidbrain.fchat.android.BuildConfig
import io.github.zidbrain.fchat.common.database.DriverFactory
import io.github.zidbrain.fchat.common.di.AppComponent
import io.github.zidbrain.fchat.common.login.GoogleAuthForm
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Inject
import me.tatarka.inject.annotations.Scope
import kotlin.time.Clock

@Scope
annotation class FChatScope

@FChatScope
@Component
abstract class FChatComponent(
    @Component protected val viewModelModule: ViewModelModule,
    @Component protected val appComponent: AppComponent
) {

    @FChatScope
    @Inject
    fun clock(): Clock = Clock.System

    @FChatScope
    @Inject
    fun credentialManager(context: Context): CredentialManager = CredentialManager.create(context)

    @FChatScope
    @Inject
    fun googleAuthForm(context: Context, credentialManager: CredentialManager): GoogleAuthForm =
        GoogleAuthForm(
            context = context,
            credentialManager = credentialManager,
            serverClientId = BuildConfig.SERVER_CLIENT_ID
        )

    @FChatScope
    @Inject
    fun database(context: Context): Database {
        val driver = DriverFactory(context).createDriver()
        return Database(
            driver = driver,
            messageEntityAdapter = MessageEntity.Adapter(
                statusAdapter = EnumColumnAdapter()
            )
        )
    }
}