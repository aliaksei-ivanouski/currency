package com.fetocan.currency.presentation.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.fetocan.currency.data.db.CurrencyRaw
import com.fetocan.currency.data.domain.model.CurrencyCode
import com.fetocan.currency.data.domain.model.CurrencyType
import com.fetocan.currency.data.ui.primaryColor
import com.fetocan.currency.data.ui.textColor

@Composable
fun CurrencyPickerDialog(
    currencies: List<CurrencyRaw>,
    currencyType: CurrencyType,
    onConfirmClick: (CurrencyCode) -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val dialogBackground = if (isDark) Color(0xFF1E2433) else Color.White
    val dialogTextColor = if (isDark) Color(0xFFF6F1E9) else textColor
    val fieldContainerColor = if (isDark) Color(0xFF2A3145) else textColor.copy(alpha = 0.01f)
    val placeholderColor = dialogTextColor.copy(alpha = 0.5f)

    var searchQuery by remember { mutableStateOf("") }
    var selectedCurrencyCode by remember(currencyType) {
        mutableStateOf(currencyType.code)
    }

    val filteredCurrencies by remember(searchQuery, currencies) {
        mutableStateOf(
            if (searchQuery.isBlank()) {
                currencies
            } else {
                val query = searchQuery.uppercase()
                currencies.filter { it.code.startsWith(query) }
            }
        )
    }

    AlertDialog(
        containerColor = dialogBackground,
        title = {
            Text(
                text = "Select a currency",
                color = dialogTextColor
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(size = 99.dp)),
                    value = searchQuery,
                    onValueChange = { query ->
                        searchQuery = query
                    },
                    placeholder = {
                        Text(
                            text = "Search here",
                            color = placeholderColor,
                            fontSize = MaterialTheme.typography.bodySmall.fontSize
                        )
                    },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = dialogTextColor,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = fieldContainerColor,
                        unfocusedContainerColor = fieldContainerColor,
                        disabledContainerColor = fieldContainerColor,
                        errorContainerColor = fieldContainerColor,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        cursorColor = dialogTextColor
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                AnimatedContent(
                    targetState = filteredCurrencies
                ) { availableCurrencies ->
                    if (availableCurrencies.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = availableCurrencies,
                                key = { it.code }
                            ) { currency ->
                                CurrencyCodePickerView(
                                    code = CurrencyCode.valueOf(currency.code),
                                    isSelected = selectedCurrencyCode.name == currency.code,
                                    onSelect = {
                                        selectedCurrencyCode = it
                                        onConfirmClick(it)
                                    }
                                )
                            }
                        }
                    } else {
                        ErrorScreen(modifier = Modifier.height(250.dp))
                    }
                }
            }
        },
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = dialogTextColor.copy(alpha = 0.7f)
                )
            }
        },
        confirmButton = {}
    )
}
