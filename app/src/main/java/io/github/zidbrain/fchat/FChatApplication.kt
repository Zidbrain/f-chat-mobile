package io.github.zidbrain.fchat

import android.app.Application
import io.github.zidbrain.fchat.di.FChatModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module

class FChatApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@FChatApplication)
            modules(FChatModule().module)
        }
    }
}