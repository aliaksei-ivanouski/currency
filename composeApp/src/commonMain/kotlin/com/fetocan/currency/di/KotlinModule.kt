package com.fetocan.currency.di

import com.fetocan.currency.data.domain.CurrencyApiService
import com.fetocan.currency.data.domain.MongoRepository
import com.fetocan.currency.data.domain.PreferencesRepository
import com.fetocan.currency.data.local.MongoImpl
import com.fetocan.currency.data.local.PreferencesImpl
import com.fetocan.currency.data.remote.api.CurrencyApiServiceImpl
import com.fetocan.currency.presentation.screen.HomeViewModel
import com.russhwolf.settings.Settings
import org.koin.core.context.startKoin
import org.koin.dsl.module

val appModule = module {
    single { Settings() }
    single<MongoRepository> { MongoImpl() }
    single<PreferencesRepository> { PreferencesImpl(settings = get()) }
    single<CurrencyApiService> { CurrencyApiServiceImpl(preferences = get()) }
    factory {
        HomeViewModel(
            preferences = get(),
            mongoDb = get(),
            api = get()
        )
    }
}

fun initializeKoin() {
    startKoin {
        modules(appModule)
    }
}