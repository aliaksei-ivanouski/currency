package com.fetocan.currency.data.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import currency.composeapp.generated.resources.Res
import currency.composeapp.generated.resources.bebas_neue_regular
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToInt

fun calculateExchangeRate(
    source: Double,
    target: Double
): Double = target / source

fun convert(
    amount: Double,
    exchangeRate: Double
): Double = amount * exchangeRate

fun roundDecimal(value: Double, decimals: Int = 2): Double {
    val factor = 10.0.pow(decimals.coerceAtLeast(0))
    return round(value * factor) / factor
}

fun formatDecimal(value: Double, decimals: Int = 2): String {
    val rounded = roundDecimal(value, decimals)
    val effectiveDecimals = decimals.coerceAtLeast(0)
    if (effectiveDecimals == 0) return rounded.toLong().toString()

    val parts = rounded.toString().split(".")
    val whole = parts[0]
    val fractionalSource = if (parts.size > 1) parts[1] else ""
    val fractional = fractionalSource.padEnd(effectiveDecimals, '0').take(effectiveDecimals)
    return "$whole.$fractional"
}

fun formatCompactNumber(value: Double): String {
    val absValue = abs(value)
    val digitsBeforeDecimal = if (absValue < 1.0) 1 else floor(log10(absValue)).toInt() + 1

    if (digitsBeforeDecimal < 10) {
        return formatWithCommas(value, 2)
    }

    val (divider, suffix) = when {
        absValue >= 1E15 -> 1E15 to "Q"
        absValue >= 1E12 -> 1E12 to "T"
        absValue >= 1E9 -> 1E9 to "B"
        else -> 1E6 to "M"
    }

    val shortValue = value / divider
    val formatted = formatWithCommas(shortValue, 2)
    return "$formatted$suffix"
}

private fun formatWithCommas(value: Double, decimals: Int): String {
    val clippedDecimals = decimals.coerceIn(0, 2)
    val factor = 10.0.pow(clippedDecimals)
    var rounded = round(value * factor) / factor
    if (rounded == -0.0) rounded = 0.0

    val sign = if (rounded < 0) "-" else ""
    val absValue = abs(rounded)
    val wholePart = absValue.toLong()
    val fractional = absValue - wholePart
    val fractionalInt = (fractional * factor).roundToInt()

    val wholeWithCommas = insertThousandsSeparator(wholePart)
    val fractionString = if (fractionalInt == 0 || clippedDecimals == 0) {
        ""
    } else {
        val padded = fractionalInt.toString().padStart(clippedDecimals, '0')
        val trimmed = padded.trimEnd('0')
        if (trimmed.isEmpty()) "" else ".$trimmed"
    }

    return "$sign$wholeWithCommas$fractionString"
}

private fun insertThousandsSeparator(value: Long): String {
    val digits = value.toString()
    if (digits.length <= 3) return digits
    val sb = StringBuilder()
    digits.forEachIndexed { index, c ->
        sb.append(c)
        val positionFromEnd = digits.length - index - 1
        if (positionFromEnd % 3 == 0 && index != digits.lastIndex) {
            sb.append(',')
        }
    }
    return sb.toString()
}

fun displayCurrentDateTime(): String {
    val currentTimestamp = Clock.System.now()
    val date = currentTimestamp.toLocalDateTime(TimeZone.currentSystemDefault())
    
    val dayOfMonth = date.day
    val month = date.month.toString().lowercase()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    
    val year = date.year
    
    val suffix = when {
        dayOfMonth in 11 .. 13 -> "th"
        dayOfMonth % 10 == 1 -> "st"
        dayOfMonth % 10 == 2 -> "nd"
        dayOfMonth % 10 == 3 -> "rd"
        else -> "th"
    }
    
    return "$dayOfMonth$suffix $month, $year"
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun GetBebasFontFamily() = FontFamily(Font(Res.font.bebas_neue_regular))
