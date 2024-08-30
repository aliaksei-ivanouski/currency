package com.fetocan.currency.data.local

import com.fetocan.currency.data.db.CurrencyRaw
import com.fetocan.currency.data.domain.LocalCacheRepository
import com.fetocan.currency.data.domain.model.RequestState
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class Cache4kImpl: LocalCacheRepository {

    val cache = Cache.Builder<String, CurrencyRaw>().build()

    override fun configureTheRealm() {
        TODO("Not yet implemented")
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun insertCurrencyData(currency: CurrencyRaw) {
        cache.put(Uuid.random().toString(), currency)
    }

    override fun readCurrencyData(): RequestState<List<CurrencyRaw>> {
        return RequestState.Success(data = cache.asMap().values.toList())
    }

    override suspend fun cleanUp() {
        cache.invalidateAll()
    }

}