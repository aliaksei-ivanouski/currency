package com.fetocan.currency.data.remote.api

import com.fetocan.currency.data.domain.CurrencyApiService
import com.fetocan.currency.data.domain.PreferencesRepository
import com.fetocan.currency.data.domain.model.ApiResponse
import com.fetocan.currency.data.domain.model.Currency
import com.fetocan.currency.data.domain.model.CurrencyCode
import com.fetocan.currency.data.domain.model.RequestState
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.headers
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class CurrencyApiServiceImpl(
    private val preferences: PreferencesRepository
): CurrencyApiService {
    companion object {
        const val ENDPOINT = "https://api.currencyapi.com/v3/latest"
        const val API_KEY = "cur_live_sLmy1WmIqez1BZrMVEfY1cJEWVCV2XsZD35e5o3Z"
    }

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 15000
        }
        install(DefaultRequest) {
            headers {
                header("apiKey", API_KEY)
            }
        }
    }

    override suspend fun getLatestExchangeRates(): RequestState<List<Currency>> {
        return try {
            val response = httpClient.get(ENDPOINT)
            if (response.status.value == 200) {
                val apiResponse = Json.decodeFromString<ApiResponse>(response.body())
                
                val availableCurrencyCodes = apiResponse.data.keys
                    .filter {
                        CurrencyCode.entries
                            .map { code -> code.name }
                            .toSet()
                            .contains(it)
                    }
                
                val availableCurrencies = apiResponse.data.values
                    .filter { currency ->
                        availableCurrencyCodes.contains(currency.code)
                    }
                
                val lastUpdated = apiResponse.meta.lastUpdatedAt
                preferences.saveLastUpdated(lastUpdated)
                
                RequestState.Success(data = availableCurrencies)
            } else {
                println("HTTP error code: ${response.status.value}")
                RequestState.Error(message = "HTTP error code: ${response.status.value}")
            }
        } catch (e: Exception) {
            println("API RESPONSE: ${e.message.toString()}")
            RequestState.Error(message = e.message.toString())
        }
    }
}