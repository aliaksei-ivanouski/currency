package com.fetocan.currency.data.domain

import com.fetocan.currency.data.domain.model.Currency
import com.fetocan.currency.data.domain.model.RequestState

interface CurrencyApiService {
    suspend fun getLatestExchangeRates(): RequestState<List<Currency>>
}