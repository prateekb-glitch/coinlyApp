package com.example.coinly.ui.reward

/**
 * UI State representing the reward claiming progress and feedback.
 */
sealed interface ClaimUiState {
    data object Idle : ClaimUiState
    data object Loading : ClaimUiState
    data class Success(val message: String) : ClaimUiState
    data class Error(val message: String) : ClaimUiState
    data class FraudAlert(val reason: String) : ClaimUiState
}
