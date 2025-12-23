package com.fetocan.currency.data.remote.api

import platform.Foundation.NSBundle

private const val ENDPOINT_KEY = "CurrencyApiEndpoint"
private const val API_KEY_KEY = "CurrencyApiKey"

actual fun platformCurrencyApiConfig(): CurrencyApiConfig {
    val bundle = NSBundle.mainBundle
    val endpoint = bundle.objectForInfoDictionaryKey(ENDPOINT_KEY) as? String
    val apiKey = bundle.objectForInfoDictionaryKey(API_KEY_KEY) as? String
    require(!endpoint.isNullOrBlank()) {
        "Missing $ENDPOINT_KEY entry in Info.plist / xcconfig."
    }
    require(!apiKey.isNullOrBlank()) {
        "Missing $API_KEY_KEY entry in Info.plist / xcconfig."
    }
    return CurrencyApiConfig(
        endpoint = endpoint,
        apiKey = apiKey
    )
}
