package com.fetocan.currency.data.domain

import com.fetocan.currency.data.db.CurrencyRaw
import com.fetocan.currency.data.domain.model.Currency
import com.fetocan.currency.data.domain.model.RequestState
import kotlinx.coroutines.flow.Flow

interface CurrencyRepository {
    suspend fun getAllCurrency(): RequestState<List<CurrencyRaw>>
    suspend fun insertCurrencies(currencies: List<CurrencyRaw>)
    suspend fun insertCurrency(currencyRaw: CurrencyRaw)
    suspend fun clearCurrencies()
}