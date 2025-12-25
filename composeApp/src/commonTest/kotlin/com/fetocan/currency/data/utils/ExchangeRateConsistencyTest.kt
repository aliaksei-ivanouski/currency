package com.fetocan.currency.data.utils
import com.fetocan.currency.data.domain.model.CurrencyCode
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class ExchangeRateConsistencyTest {

    private val tolerance = 1e-6

    @Test
    fun `aed to usd conversions use rounded rates`() {
        val aedPerUsd = 3.6705
        val usdPerUsd = 1.0
        val amount = 367.0

        val rate = calculateExchangeRate(aedPerUsd, usdPerUsd)
        val converted = convert(amount, rate)

        assertTrue(
            abs(converted.doubleValue(false) - 100.0) < tolerance,
            "367 AED should convert to 100 USD when rounding to 2 decimals"
        )

        val reverseRate = calculateExchangeRate(usdPerUsd, aedPerUsd)
        val reversed = convert(100.0, reverseRate)
        assertTrue(
            abs(reversed.doubleValue(false) - amount) < tolerance,
            "100 USD should convert back to 367 AED"
        )
    }

    @Test
    fun `very small rates keep precision`() {
        val tinyPerUsd = 0.000045678
        val usdPerUsd = 1.0
        val amount = 250_000.0

        val rate = calculateExchangeRate(tinyPerUsd, usdPerUsd)
        val converted = convert(amount, rate)

        val expectedRate = usdPerUsd / roundForRateValue(tinyPerUsd)
        val expected = amount * expectedRate

        assertTrue(
            abs(converted.doubleValue(false) - expected) < tolerance,
            "Small rate conversions should not underflow"
        )
    }

    @Test
    fun `round trip conversion preserves amount across currencies`() {
        val syntheticRates = CurrencyCode.entries.mapIndexed { index, code ->
            code.name to (1.0 + (index * 0.03))
        }.toMap()

        val amount = 987.65

        syntheticRates.forEach { (sourceCode, sourceValue) ->
            syntheticRates.forEach { (targetCode, targetValue) ->
                val forwardRate = calculateExchangeRate(sourceValue, targetValue)
                val converted = convert(amount, forwardRate)

                val backwardRate = calculateExchangeRate(targetValue, sourceValue)
                val reverted = convert(converted.doubleValue(false), backwardRate)

                val difference = abs(reverted.doubleValue(false) - amount)
                assertTrue(
                    difference < tolerance,
                )
            }
        }
    }

    private fun roundForRateValue(value: Double): Double {
        val decimals = when {
            value >= 1.0 -> 2
            value >= 0.1 -> 4
            else -> 6
        }
        return roundDecimal(value, decimals)
    }
}
