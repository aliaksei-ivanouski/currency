package com.fetocan.currency.data.domain

import com.fetocan.currency.data.domain.model.Currency
import com.fetocan.currency.data.domain.model.RequestState
import kotlinx.coroutines.flow.Flow

interface MongoRepository {
    fun configureTheRaelm()
    suspend fun insertCurrencyData(currency: Currency)
    fun readCurrencyData(): Flow<RequestState<List<Currency>>>
    suspend fun cleanUp()
}