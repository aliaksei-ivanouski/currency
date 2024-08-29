package com.fetocan.currency.data.db

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CurrencyRaw(
    @SerialName("code")
    val code: String,
    @SerialName("value")
    val value: Double
)