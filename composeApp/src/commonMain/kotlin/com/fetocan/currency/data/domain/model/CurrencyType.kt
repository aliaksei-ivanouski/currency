package com.fetocan.currency.data.domain.model

sealed class CurrencyType(
    val code: CurrencyCode
) {
    data class Source(val currencyCode: CurrencyCode): CurrencyType(code = currencyCode)
    data class Target(val currencyCode: CurrencyCode): CurrencyType(code = currencyCode)
    data object None: CurrencyType(code = CurrencyCode.USD)
}