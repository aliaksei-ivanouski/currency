package com.fetocan.currency.data.db

import com.fetocan.currency.data.domain.CurrencyRepository
import com.fetocan.currency.data.domain.model.RequestState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

class CurrencyRepositoryImpl(
    private val database: CurrencyDatabase?
) : CurrencyRepository {

    override suspend fun getAllCurrency(): RequestState<List<CurrencyRaw>> =
        RequestState.Success(
            data = database?.currencyDatabaseQueries
                ?.selectAllCurrencies(::mapToCurrencyRaw)
                ?.executeAsList()
                ?: listOf()
        )

    override suspend fun insertCurrencies(
        currencies: List<CurrencyRaw>
    ) {
        database?.currencyDatabaseQueries?.transaction {
            currencies.forEach { currencyRaw ->
                database.currencyDatabaseQueries.insertCurrency(
                    currencyRaw.code,
                    currencyRaw.value
                )
            }
        }
    }

    override suspend fun insertCurrency(
        currencyRaw: CurrencyRaw
    ) {
        database?.currencyDatabaseQueries?.insertCurrency(
            currencyRaw.code,
            currencyRaw.value
        )
    }

    override suspend fun clearCurrencies() {
        database?.currencyDatabaseQueries?.removeAllCurrencies()
    }

    private fun mapToCurrencyRaw(
        code: String,
        value: Double
    ) = CurrencyRaw(
        code,
        value
    )
}

@Serializable
data class CurrencyRaw(
    val code: String,
    val value: Double
)