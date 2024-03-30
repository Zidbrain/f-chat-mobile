package io.github.zidbrain.fchat

import android.app.Application
import io.github.zidbrain.fchat.di.allModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class FChatApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@FChatApplication)

            modules(allModules)
        }
    }
}