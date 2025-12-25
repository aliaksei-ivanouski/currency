package com.fetocan.currency.data.remote.api

import com.fetocan.currency.shared.BuildConfig

actual fun platformCurrencyApiConfig(): CurrencyApiConfig {
    val apiKey = BuildConfig.CURRENCY_API_KEY.orEmpty()
    require(apiKey.isNotBlank()) {
        "Missing Currency API key. Add currencyApiKey to local.properties."
    }
    val endpoint = BuildConfig.CURRENCY_API_ENDPOINT.orEmpty()
    require(endpoint.isNotBlank()) {
        "Missing Currency API endpoint configuration."
    }
    return CurrencyApiConfig(
        endpoint = endpoint,
        apiKey = apiKey
    )
}
