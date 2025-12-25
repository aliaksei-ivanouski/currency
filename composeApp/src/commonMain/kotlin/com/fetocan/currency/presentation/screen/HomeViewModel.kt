package com.fetocan.currency.presentation.screen

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.fetocan.currency.data.db.CurrencyRaw
import com.fetocan.currency.data.domain.CurrencyApiService
import com.fetocan.currency.data.domain.CurrencyRepository
import com.fetocan.currency.data.domain.PreferencesRepository
import com.fetocan.currency.data.domain.model.RateStatus
import com.fetocan.currency.data.domain.model.RequestState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Clock

sealed class HomeUiEvent {
    data object RefreshRates : HomeUiEvent()
    data object SwitchCurrencies : HomeUiEvent()
    data class SaveSourceCurrencyCode(val code: String) : HomeUiEvent()
    data class SaveTargetCurrencyCode(val code: String) : HomeUiEvent()
}

class HomeViewModel(
    private val preferences: PreferencesRepository,
    private val repository: CurrencyRepository,
    private val api: CurrencyApiService,
    private val ioDispatcher: CoroutineDispatcher
) : ScreenModel {
    private var _rateStatus: MutableState<RateStatus> =
        mutableStateOf(RateStatus.Idle)
    val rateStatus: State<RateStatus> = _rateStatus

    private val _allCurrencies = MutableStateFlow<List<CurrencyRaw>>(emptyList())
    val allCurrencies: StateFlow<List<CurrencyRaw>> = _allCurrencies.asStateFlow()

    private var _sourceCurrency: MutableState<RequestState<CurrencyRaw>> =
        mutableStateOf(RequestState.Idle)
    val sourceCurrency: State<RequestState<CurrencyRaw>> = _sourceCurrency

    private var _targetCurrency: MutableState<RequestState<CurrencyRaw>> =
        mutableStateOf(RequestState.Idle)
    val targetCurrency: State<RequestState<CurrencyRaw>> = _targetCurrency

    private var _refreshState: MutableState<RequestState<Unit>> =
        mutableStateOf(RequestState.Idle)
    val refreshState: State<RequestState<Unit>> = _refreshState

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
                if (refreshState.value.isLoading()) return
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
            preferences.readSourceCurrencyCode()
                .combine(_allCurrencies) { currencyCode, currencies ->
                    Pair(currencyCode, currencies)
                }
                .collectLatest { (currencyCode, currencies) ->
                    if (currencies.isEmpty()) {
                        _sourceCurrency.value = RequestState.Loading
                        return@collectLatest
                    }
                    val selectedCurrency = currencies.find { it.code == currencyCode.name }
                    _sourceCurrency.value = if (selectedCurrency != null)
                        RequestState.Success(data = selectedCurrency)
                    else
                        RequestState.Error(message = "Couldn't find the selected currency")
                }
        }
    }

    private fun readTargetCurrency() {
        screenModelScope.launch(Dispatchers.Main) {
            preferences.readTargetCurrencyCode()
                .combine(_allCurrencies) { currencyCode, currencies ->
                    Pair(currencyCode, currencies)
                }
                .collectLatest { (currencyCode, currencies) ->
                    if (currencies.isEmpty()) {
                        _targetCurrency.value = RequestState.Loading
                        return@collectLatest
                    }
                    val selectedCurrency = currencies.find { it.code == currencyCode.name }
                    _targetCurrency.value = if (selectedCurrency != null)
                        RequestState.Success(data = selectedCurrency)
                    else
                        RequestState.Error(message = "Couldn't find the selected currency")
                }
        }
    }

    private suspend fun fetchNewRates() {
        _refreshState.value = RequestState.Loading
        try {
            val (currencies, freshStatus) = withContext(ioDispatcher) {
                val cachedCurrencies = when (val cachedState = repository.getAllCurrency()) {
                    is RequestState.Success -> cachedState.data
                    is RequestState.Error -> throw IllegalStateException(cachedState.message)
                    else -> emptyList()
                }
                val now = Clock.System.now().toEpochMilliseconds()
                val (data, lastUpdated) = if (cachedCurrencies.isNotEmpty()) {
                    val cacheFresh = preferences.isDataFresh(now)
                    if (cacheFresh) cachedCurrencies to preferences.getLastUpdated()
                    else cacheLatestRates()
                } else {
                    cacheLatestRates()
                }
                lastUpdated?.let { preferences.saveLastUpdated(it) }
                val status = if (preferences.isDataFresh(Clock.System.now().toEpochMilliseconds()))
                    RateStatus.Fresh else RateStatus.Stale
                data to status
            }
            _allCurrencies.value = currencies
            _rateStatus.value = freshStatus
            _refreshState.value = RequestState.Success(Unit)
        } catch (e: Exception) {
            println(e.message)
            _refreshState.value = RequestState.Error(e.message ?: "Unable to refresh currency rates.")
        }
    }

    private suspend fun cacheLatestRates(): Pair<List<CurrencyRaw>, String?> {
        val fetchedData = api.getLatestExchangeRates()
        if (fetchedData.isSuccess()) {
            val mappedCurrencies = fetchedData.getSuccessData().map { CurrencyRaw(it.code, it.value) }
            repository.clearCurrencies()
            repository.insertCurrencies(mappedCurrencies)
            return mappedCurrencies to fetchedData.getSuccessMeta()?.lastUpdatedAt
        } else if (fetchedData.isError()) {
            throw IllegalStateException(fetchedData.getErrorMessage())
        }
        return emptyList<CurrencyRaw>() to null
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
