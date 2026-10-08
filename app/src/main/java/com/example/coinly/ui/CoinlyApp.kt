package com.example.coinly.ui

import android.app.Application
import com.example.coinly.di.appModule
import com.example.coinly.trust.di.trustModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Application class for Coinly. Initializes Koin dependency injection container on startup.
 */
class CoinlyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@CoinlyApp)
            modules(appModule, trustModule)
        }
    }
}
