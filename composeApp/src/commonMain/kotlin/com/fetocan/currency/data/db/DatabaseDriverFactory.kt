package com.fetocan.currency.data.db

import app.cash.sqldelight.db.SqlDriver

const val DATABASE_NAME = "currency_db"

expect class DriverFactory {
    fun createDriver(): SqlDriver
}