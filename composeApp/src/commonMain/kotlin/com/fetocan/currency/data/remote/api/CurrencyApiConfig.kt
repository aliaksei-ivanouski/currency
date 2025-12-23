package com.fetocan.currency.data.remote.api

data class CurrencyApiConfig(
    val endpoint: String,
    val apiKey: String
)

expect fun platformCurrencyApiConfig(): CurrencyApiConfig
