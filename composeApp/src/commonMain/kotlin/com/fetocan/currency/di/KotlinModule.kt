package com.fetocan.currency.di

import com.fetocan.currency.data.db.CurrencyRepositoryImpl
import com.fetocan.currency.data.domain.CurrencyApiService
import com.fetocan.currency.data.domain.CurrencyRepository
import com.fetocan.currency.data.domain.PreferencesRepository
import com.fetocan.currency.data.local.PreferencesImpl
import com.fetocan.currency.data.remote.api.CurrencyApiServiceImpl
import com.fetocan.currency.data.remote.api.platformCurrencyApiConfig
import com.fetocan.currency.presentation.screen.HomeViewModel
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val DISPATCHER_IO = "dispatcherIo"

val appModule = module {
    single { Settings() }
    single { platformCurrencyApiConfig() }
    single<CurrencyRepository> { CurrencyRepositoryImpl(database = get()) }

    single<CoroutineDispatcher>(named(DISPATCHER_IO)) { Dispatchers.Default }

    single<PreferencesRepository> { PreferencesImpl(settings = get()) }
    single<CurrencyApiService> { CurrencyApiServiceImpl(config = get()) }
    factory {
        HomeViewModel(
            preferences = get(),
            repository = get(),
            api = get(),
            ioDispatcher = get(named(DISPATCHER_IO))
        )
    }
}
