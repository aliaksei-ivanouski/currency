package com.fetocan.currency

import android.app.Application
import com.fetocan.currency.di.appModule
import com.fetocan.currency.di.databaseModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class CurrencyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@CurrencyApplication)
            modules(appModule + databaseModule)
        }
    }
}
