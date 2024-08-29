package com.fetocan.currency.di

import app.cash.sqldelight.db.SqlDriver
import com.fetocan.currency.data.db.CurrencyDatabase
import com.fetocan.currency.data.db.DriverFactory
import org.koin.dsl.module

val databaseModule = module {

    single<SqlDriver> { DriverFactory().createDriver() }
    single<CurrencyDatabase> { CurrencyDatabase(get()) }

}