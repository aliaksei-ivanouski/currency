package com.fetocan.currency.presentation.component

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fetocan.currency.data.db.CurrencyRaw
import com.fetocan.currency.data.domain.model.CurrencyCode
import com.fetocan.currency.data.domain.model.CurrencyType
import com.fetocan.currency.data.domain.model.DisplayResult
import com.fetocan.currency.data.domain.model.RateStatus
import com.fetocan.currency.data.domain.model.RequestState
import com.fetocan.currency.data.ui.headerColor
import com.fetocan.currency.data.ui.staleColor
import com.fetocan.currency.data.utils.displayCurrentDateTime
import com.fetocan.currency.getPlatform
import currency.composeapp.generated.resources.Res
import currency.composeapp.generated.resources.close_ic
import currency.composeapp.generated.resources.exchange_illustration
import currency.composeapp.generated.resources.refresh_ic
import currency.composeapp.generated.resources.switch_ic
import org.jetbrains.compose.resources.painterResource

@Composable
fun HomeHeader(
    status: RateStatus,
    source: RequestState<CurrencyRaw>,
    target: RequestState<CurrencyRaw>,
    refreshState: RequestState<Unit>,
    amountText: String,
    onAmountChange: (String) -> Unit,
    onSwitchClick: () -> Unit,
    onRatesRefresh: () -> Unit,
    onCurrencyTypeSelect: (CurrencyType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
            .background(headerColor)
            .padding(top = if (getPlatform().name == "Android") 0.dp else 24.dp)
            .padding(all = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        RatesStatus(
            status = status,
            refreshState = refreshState,
            onRatesRefresh = onRatesRefresh
        )
        Spacer(modifier = Modifier.height(24.dp))
        CurrencyInputs(
            source = source,
            target = target,
            onSwitchClick = onSwitchClick,
            onCurrencyTypeSelect = onCurrencyTypeSelect
        )
        Spacer(modifier = Modifier.height(24.dp))
        AmountInput(
            amountText = amountText,
            onAmountChange = onAmountChange
        )
    }
}

@Composable
fun RatesStatus(
    status: RateStatus,
    refreshState: RequestState<Unit>,
    onRatesRefresh: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row {
            Image(
                modifier = Modifier.size(50.dp),
                painter = painterResource(Res.drawable.exchange_illustration),
                contentDescription = "Exchange Rate Illustration"
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = displayCurrentDateTime(),
                    color = Color.White
                )
                Text(
                    text = status.title,
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    color = status.color
                )
            }
        }
        
        when {
            refreshState.isLoading() -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            }
            status == RateStatus.Stale || refreshState.isError() -> {
                IconButton(onClick = onRatesRefresh) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(Res.drawable.refresh_ic),
                        contentDescription = "Refresh Icon",
                        tint = if (refreshState.isError()) staleColor else Color.White
                    )
                }
            }
        }
    }
    if (refreshState.isError()) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = refreshState.getErrorMessage(),
            color = staleColor,
            fontSize = MaterialTheme.typography.bodySmall.fontSize,
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun CurrencyInputs(
    source: RequestState<CurrencyRaw>,
    target: RequestState<CurrencyRaw>,
    onSwitchClick: () -> Unit,
    onCurrencyTypeSelect: (CurrencyType) -> Unit
) {
    var animationStarted by remember { mutableStateOf(false) }
    val animatedRotation by animateFloatAsState(
        targetValue = if (animationStarted) 180f else 0f,
        animationSpec = tween(durationMillis = 500)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CurrencyView(
            placeholder = "from",
            currency = source,
            onClick = {
                if (source.isSuccess()) {
                    onCurrencyTypeSelect(
                        CurrencyType.Source(
                            currencyCode = CurrencyCode.valueOf(
                                source.getSuccessData().code
                            )
                        )
                    )
                }
            }
        )
        
        Spacer(modifier = Modifier.height(14.dp))
        
        IconButton(
            modifier = Modifier
                .padding(top = 24.dp)
                .graphicsLayer {
                    rotationY = animatedRotation
                },
            onClick = {
                animationStarted = !animationStarted
                onSwitchClick()
            }
        ) {
            Icon(
                painter = painterResource(Res.drawable.switch_ic),
                contentDescription = "Switch Icon",
                tint = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(14.dp))
        
        CurrencyView(
            placeholder = "to",
            currency = target,
            onClick = {
                if (target.isSuccess()) {
                    onCurrencyTypeSelect(
                        CurrencyType.Target(
                            currencyCode = CurrencyCode.valueOf(
                                target.getSuccessData().code
                            )
                        )
                    )
                }
            }
        )
    }
}

@Composable
fun RowScope.CurrencyView(
    placeholder: String,
    currency: RequestState<CurrencyRaw>,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.weight(1f)) {
        Text(
            modifier = Modifier.padding(start = 12.dp),
            text = placeholder,
            fontSize = MaterialTheme.typography.bodySmall.fontSize,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(size = 8.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .height(54.dp)
                .clickable { onClick() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            currency.DisplayResult(
                onSuccess = { data ->
                Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(
                            CurrencyCode.valueOf(data.code).flag
                        ),
                        tint = Color.Unspecified,
                        contentDescription = "Country Flag"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = CurrencyCode.valueOf(data.code).name,
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                        color = Color.White
                    )
                }
            )
        }
    }
}

@Composable
fun AmountInput(
    amountText: String,
    onAmountChange: (String) -> Unit
) {
    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(size = 8.dp))
            .animateContentSize()
            .height(54.dp),
        value = amountText,
        onValueChange = { newValue ->
            val sanitized = sanitizeAmountInput(newValue)
            if (sanitized != null) {
                onAmountChange(sanitized)
            }
        },
        visualTransformation = AmountVisualTransformation,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White.copy(alpha = 0.05f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
            disabledContainerColor = Color.White.copy(alpha = 0.05f),
            errorContainerColor = Color.White.copy(alpha = 0.05f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = Color.White
        ),
        textStyle = TextStyle(
            color = Color.White,
            fontSize = MaterialTheme.typography.titleLarge.fontSize,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        ),
        leadingIcon = {
            if (amountText.isNotEmpty()) {
                Spacer(modifier = Modifier.width(40.dp))
            }
        },
        trailingIcon = {
            if (amountText.isNotEmpty()) {
                IconButton(onClick = { onAmountChange("0") }) {
                    Icon(
                        painter = painterResource(Res.drawable.close_ic),
                        contentDescription = "Clear amount",
                        tint = Color.White
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal
        )
    )
}

private fun sanitizeAmountInput(rawInput: String): String? {
    val filtered = rawInput.filter { it.isDigit() || it == '.' }
    val dotCount = filtered.count { it == '.' }
    if (dotCount > 1) return null

    val normalized = when {
        filtered.isEmpty() -> ""
        filtered.startsWith("0") && filtered.length > 1 && filtered[1] != '.' -> filtered.trimStart('0').ifEmpty { "0" }
        else -> filtered
    }

    val limitReached = normalized.replace(".", "").length > 15
    return if (limitReached) null else normalized
}

private object AmountVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text
        if (original.isEmpty()) {
            return TransformedText(AnnotatedString(""), OffsetMapping.Identity)
        }

        val dotIndex = original.indexOf('.')
        val intEnd = if (dotIndex == -1) original.length else dotIndex
        val integerPart = original.substring(0, intEnd)
        val fractionalPart = if (dotIndex == -1) "" else original.substring(dotIndex)

        val grouped = groupInteger(integerPart)
        val transformedText = grouped.grouped + fractionalPart

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return if (offset <= intEnd) offset + grouped.commasBefore[offset]
                else offset + grouped.commasBefore[intEnd]
            }

            override fun transformedToOriginal(offset: Int): Int {
                val groupLen = grouped.grouped.length
                return if (offset <= groupLen) {
                    grouped.transformedToOriginal[offset]
                } else {
                    val extra = offset - groupLen
                    grouped.transformedToOriginal[groupLen] + extra
                }
            }
        }

        return TransformedText(AnnotatedString(transformedText), offsetMapping)
    }

    private fun groupInteger(integerPart: String): GroupResult {
        if (integerPart.isEmpty()) {
            val commasBefore = IntArray(1)
            val mapping = IntArray(1)
            return GroupResult("", commasBefore, mapping)
        }
        val length = integerPart.length
        val commasBefore = IntArray(length + 1)
        val transformedList = mutableListOf<Int>()
        val builder = StringBuilder()
        var digitsProcessed = 0
        var commasInserted = 0
        for (offset in 0..length) {
            commasBefore[offset] = commasInserted
            if (offset == length) break
            val char = integerPart[offset]
            builder.append(char)
            transformedList.add(offset)
            digitsProcessed++
            val remaining = length - digitsProcessed
            if (remaining > 0 && remaining % 3 == 0) {
                builder.append(',')
                transformedList.add(offset)
                commasInserted++
            }
        }
        val transformedToOriginal = IntArray(transformedList.size + 1)
        for (i in transformedList.indices) {
            transformedToOriginal[i] = transformedList[i]
        }
        transformedToOriginal[transformedList.size] = length
        return GroupResult(builder.toString(), commasBefore, transformedToOriginal)
    }

    private data class GroupResult(
        val grouped: String,
        val commasBefore: IntArray,
        val transformedToOriginal: IntArray
    )
}
