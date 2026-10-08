package com.example.coinly.ui.reward

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coinly.domain.model.CoinBalance
import com.example.coinly.domain.model.TaskItem
import com.example.coinly.domain.model.ClaimResult
import com.example.coinly.domain.usecase.ClaimRewardUseCase
import com.example.coinly.domain.usecase.GetCoinBalanceUseCase
import com.example.coinly.domain.usecase.GetTasksUseCase
import com.example.coinly.domain.usecase.RedeemCoinsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for Coinly rewards and tasks. Manages UI state using StateFlow and structured concurrency.
 * Clean Architecture - Presentation Layer.
 */
class RewardViewModel(
    private val getCoinBalanceUseCase: GetCoinBalanceUseCase,
    private val getTasksUseCase: GetTasksUseCase,
    private val claimRewardUseCase: ClaimRewardUseCase,
    private val redeemCoinsUseCase: RedeemCoinsUseCase
) : ViewModel() {

    val coinBalance: StateFlow<CoinBalance> = getCoinBalanceUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CoinBalance(0, 0.0)
        )

    val tasks: StateFlow<List<TaskItem>> = getTasksUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow<ClaimUiState>(ClaimUiState.Idle)
    val uiState: StateFlow<ClaimUiState> = _uiState.asStateFlow()

    fun claimReward(taskId: String) {
        viewModelScope.launch {
            _uiState.value = ClaimUiState.Loading
            when (val result = claimRewardUseCase(taskId)) {
                is ClaimResult.Success -> {
                    _uiState.value = ClaimUiState.Success(result.message)
                }
                is ClaimResult.FraudDetected -> {
                    _uiState.value = ClaimUiState.FraudAlert(result.reason)
                }
                is ClaimResult.Error -> {
                    _uiState.value = ClaimUiState.Error(result.throwable.localizedMessage ?: "Unknown error occurred")
                }
            }
        }
    }

    fun redeemCoins() {
        viewModelScope.launch {
            _uiState.value = ClaimUiState.Loading
            when (val result = redeemCoinsUseCase()) {
                is ClaimResult.Success -> {
                    _uiState.value = ClaimUiState.Success(result.message)
                }
                is ClaimResult.FraudDetected -> {
                    _uiState.value = ClaimUiState.FraudAlert(result.reason)
                }
                is ClaimResult.Error -> {
                    _uiState.value = ClaimUiState.Error(result.throwable.localizedMessage ?: "Unknown error occurred")
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = ClaimUiState.Idle
    }
}
