package com.fetocan.currency.data.local

import com.fetocan.currency.data.domain.LocalCacheRepository
import com.fetocan.currency.data.domain.model.Currency
import com.fetocan.currency.data.domain.model.RequestState
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class Cache4kImpl: LocalCacheRepository {

    val cache = Cache.Builder<String, Currency>().build()

    override fun configureTheRealm() {
        TODO("Not yet implemented")
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun insertCurrencyData(currency: Currency) {
        cache.put(Uuid.random().toString(), currency)
    }

    override fun readCurrencyData(): Flow<RequestState<List<Currency>>> {
        return flow {
            RequestState.Success(data = cache.asMap().values.toList())
        }
    }

    override suspend fun cleanUp() {
        cache.invalidateAll()
    }

}