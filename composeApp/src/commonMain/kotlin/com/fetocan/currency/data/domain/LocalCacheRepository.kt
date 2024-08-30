package com.fetocan.currency.data.domain

import com.fetocan.currency.data.db.CurrencyRaw
import com.fetocan.currency.data.domain.model.RequestState
import kotlinx.coroutines.flow.Flow

interface LocalCacheRepository {
    fun configureTheRealm()
    suspend fun insertCurrencyData(currency: CurrencyRaw)
    fun readCurrencyData(): RequestState<List<CurrencyRaw>>
    suspend fun cleanUp()
}