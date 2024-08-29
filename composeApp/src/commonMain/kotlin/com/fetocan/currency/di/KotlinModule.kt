package com.fetocan.currency.di

import com.fetocan.currency.data.db.CurrencyRepositoryImpl
import com.fetocan.currency.data.domain.CurrencyApiService
import com.fetocan.currency.data.domain.CurrencyRepository
import com.fetocan.currency.data.domain.LocalCacheRepository
import com.fetocan.currency.data.domain.PreferencesRepository
import com.fetocan.currency.data.local.Cache4kImpl
import com.fetocan.currency.data.local.PreferencesImpl
import com.fetocan.currency.data.remote.api.CurrencyApiServiceImpl
import com.fetocan.currency.presentation.screen.HomeViewModel
import com.russhwolf.settings.Settings
import org.koin.dsl.module

val appModule = module {
    single { Settings() }
    single<LocalCacheRepository> { Cache4kImpl() }
//    single<LocalCacheRepository> { MongoImpl() }

    single<CurrencyRepository> { CurrencyRepositoryImpl(database = getOrNull()) }

    single<PreferencesRepository> { PreferencesImpl(settings = get()) }
    single<CurrencyApiService> { CurrencyApiServiceImpl(preferences = get()) }
    factory {
        HomeViewModel(
            preferences = get(),
            repository = get(),
            api = get()
        )
    }
}