package com.fetocan.currency

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import cafe.adriel.voyager.navigator.Navigator
import com.fetocan.currency.di.initializeKoin
import com.fetocan.currency.presentation.screen.HomeScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    initializeKoin()
    
    MaterialTheme {
        Navigator(HomeScreen())
    }
}