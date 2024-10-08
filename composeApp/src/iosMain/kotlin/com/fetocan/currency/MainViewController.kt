package com.fetocan.currency

import androidx.compose.ui.window.ComposeUIViewController
import com.fetocan.currency.di.appModule
import com.fetocan.currency.di.databaseModule
import org.koin.core.context.startKoin

fun MainViewController() = ComposeUIViewController {
    App()
}

fun initializeKoin() {
    startKoin {
        modules(appModule + databaseModule)
    }
}