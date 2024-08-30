package com.fetocan.currency.presentation.screen

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.fetocan.currency.data.db.CurrencyRaw
import com.fetocan.currency.data.domain.CurrencyApiService
import com.fetocan.currency.data.domain.CurrencyRepository
import com.fetocan.currency.data.domain.PreferencesRepository
import com.fetocan.currency.data.domain.model.RateStatus
import com.fetocan.currency.data.domain.model.RequestState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

sealed class HomeUiEvent {
    data object RefreshRates : HomeUiEvent()
    data object SwitchCurrencies : HomeUiEvent()
    data class SaveSourceCurrencyCode(val code: String) : HomeUiEvent()
    data class SaveTargetCurrencyCode(val code: String) : HomeUiEvent()
}

class HomeViewModel(
    private val preferences: PreferencesRepository,
    private val repository: CurrencyRepository,
    private val api: CurrencyApiService
) : ScreenModel {
    private var _rateStatus: MutableState<RateStatus> =
        mutableStateOf(RateStatus.Idle)
    val rateStatus: State<RateStatus> = _rateStatus

    val _allCurrencies = mutableStateListOf<CurrencyRaw>()
    val allCurrencies: List<CurrencyRaw> = _allCurrencies

    private var _sourceCurrency: MutableState<RequestState<CurrencyRaw>> =
        mutableStateOf(RequestState.Idle)
    val sourceCurrency: State<RequestState<CurrencyRaw>> = _sourceCurrency

    private var _targetCurrency: MutableState<RequestState<CurrencyRaw>> =
        mutableStateOf(RequestState.Idle)
    val targetCurrency: State<RequestState<CurrencyRaw>> = _targetCurrency

    init {
        screenModelScope.launch {
            fetchNewRates()
            readSourceCurrency()
            readTargetCurrency()
        }
    }

    fun sendEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.RefreshRates -> {
                screenModelScope.launch {
                    fetchNewRates()
                }
            }
            is HomeUiEvent.SwitchCurrencies -> {
                switchCurrencies()
            }
            is HomeUiEvent.SaveSourceCurrencyCode -> {
                saveSourceCurrencyCode(code = event.code)
            }
            is HomeUiEvent.SaveTargetCurrencyCode -> {
                saveTargetCurrencyCode(code = event.code)
            }
        }
    }

    private fun readSourceCurrency() {
        screenModelScope.launch(Dispatchers.Main) {
            preferences.readSourceCurrencyCode().collectLatest { currencyCode ->
                val selectedCurrency = _allCurrencies.find { it.code == currencyCode.name }
                _sourceCurrency.value = if (selectedCurrency != null)
                    RequestState.Success(data = selectedCurrency) else
                        RequestState.Error(message = "Couldn't find the selected currency")
            }
        }
    }

    private fun readTargetCurrency() {
        screenModelScope.launch(Dispatchers.Main) {
            preferences.readTargetCurrencyCode().collectLatest { currencyCode ->
                val selectedCurrency = _allCurrencies.find { it.code == currencyCode.name }
                _targetCurrency.value = if (selectedCurrency != null)
                    RequestState.Success(data = selectedCurrency) else
                    RequestState.Error(message = "Couldn't find the selected currency")
            }
        }
    }

    private suspend fun fetchNewRates() {
        try {
            repository.getAllCurrency()
                .takeIf { it.isSuccess() }
                ?.getSuccessData()
                ?.takeIf { it.isNotEmpty() }
                ?.also { currencyRaw ->
                    println("HomeViewModel: DATABASE IS FULL")
                    _allCurrencies.addAll(currencyRaw)
                    if (!preferences.isDataFresh(Clock.System.now().toEpochMilliseconds())) {
                        println("HomeViewModel: DATA NOT FRESH")
                        cacheTheData()
                    } else {
                        println("HomeViewModel: DATA IS FRESH")
                    }
                } ?: cacheTheData()
            getRateStatus()
        } catch (e: Exception) {
            println(e.message)
        }
    }

    private suspend fun cacheTheData() {
        println("HomeViewModel: DATABASE NEEDS DATA")
        val fetchedData = api.getLatestExchangeRates()
        if (fetchedData.isSuccess()) {
            repository.clearCurrencies()
            fetchedData.getSuccessData().forEach {
                println("HomeViewModel: ADDING ${it.code}")
                repository.insertCurrency(CurrencyRaw(it.code, it.value))
            }
            println("HomeViewModel: UPDATING ALL CURRENCIES")
            _allCurrencies.addAll(
                fetchedData.getSuccessData().map { CurrencyRaw(it.code, it.value) }
            )
        } else if (fetchedData.isError()) {
            println("HomeViewModel: FETCHING FAILED ${fetchedData.getErrorMessage()}")
        }
    }

    private suspend fun getRateStatus() {
        _rateStatus.value = if (preferences.isDataFresh(
                currentTimestamp = Clock.System.now().toEpochMilliseconds()
            )
        ) RateStatus.Fresh
        else RateStatus.Stale

        println("Status ${_rateStatus.value.title}")
    }

    private fun switchCurrencies() {
        val source = _sourceCurrency.value
        val target = _targetCurrency.value
        _sourceCurrency.value = target
        _targetCurrency.value = source
    }

    private fun saveSourceCurrencyCode(code: String) {
        screenModelScope.launch(Dispatchers.IO) {
            preferences.saveSourceCurrencyCode(code)
        }
    }

    private fun saveTargetCurrencyCode(code: String) {
        screenModelScope.launch(Dispatchers.IO) {
            preferences.saveTargetCurrencyCode(code)
        }
    }
}