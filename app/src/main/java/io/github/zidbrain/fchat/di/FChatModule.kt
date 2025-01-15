package io.github.zidbrain.fchat.di

import android.content.Context
import androidx.credentials.CredentialManager
import app.cash.sqldelight.EnumColumnAdapter
import io.github.zidbrain.Database
import io.github.zidbrain.MessageEntity
import io.github.zidbrain.fchat.common.database.DriverFactory
import io.github.zidbrain.fchat.common.di.CommonModule
import kotlinx.datetime.Clock
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(
    includes = [CommonModule::class, ClientModule::class]
)
@ComponentScan("io.github.zidbrain.fchat")
class FChatModule {

    @Single
    fun clock(): Clock = Clock.System

    @Single
    fun credentialManager(context: Context): CredentialManager = CredentialManager.create(context)

    @Single
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