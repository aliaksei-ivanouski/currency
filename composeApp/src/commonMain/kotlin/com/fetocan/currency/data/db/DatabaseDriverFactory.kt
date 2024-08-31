package com.fetocan.currency.data.db

import app.cash.sqldelight.db.SqlDriver

expect class DriverFactory {
    fun createDriver(): SqlDriver
}

fun createDatabase(
    driverFactory: DriverFactory
): CurrencyDatabase {
    val driver = driverFactory.createDriver()
    val database = CurrencyDatabase(driver)
    return database // Do more work with the database (see below).
}