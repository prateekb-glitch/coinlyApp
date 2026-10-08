package com.example.coinly.trust.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coinly.trust.core.repository.TrustStorage
import com.example.coinly.trust.core.model.TrustPolicy
import com.example.coinly.trust.core.model.TrustState
import com.example.coinly.trust.core.repository.TrustClaimResult
import com.example.coinly.trust.core.repository.TrustRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel managing the explicit sealed TrustState for the Trust Gate.
 * Requirement 2: UI renders state only, no business logic in Composables.
 */
class TrustGateViewModel(
    private val trustRepository: TrustRepository,
    private val secureStorage: TrustStorage,
    private val defaultPolicy: TrustPolicy = TrustPolicy()
) : ViewModel() {

    private val _trustState = MutableStateFlow<TrustState>(TrustState.Idle)
    val trustState: StateFlow<TrustState> = _trustState.asStateFlow()

    init {
        // Check for process-death restore of pending claims
        val pendingTaskId = secureStorage.getPendingClaim()
        if (pendingTaskId != null) {
            // Restore flow or auto-retry pending claim
            // println("TrustGate: Restored pending claim for taskId=$pendingTaskId")
        }
    }

    fun claimReward(taskId: String, policy: TrustPolicy = defaultPolicy) {
        viewModelScope.launch {
            _trustState.value = TrustState.Checking

            when (val result = trustRepository.claimRewardWithTrust(taskId, policy)) {
                is TrustClaimResult.Success -> {
                    _trustState.value = TrustState.Allowed
                }
                is TrustClaimResult.DeviceCompromised -> {
                    _trustState.value = TrustState.Blocked(result.reason, isTerminal = true)
                }
                is TrustClaimResult.ChallengeNeeded -> {
                    _trustState.value = TrustState.ChallengeRequired(result.challengeId, result.taskId)
                }
                is TrustClaimResult.Failed -> {
                    _trustState.value = TrustState.Blocked(result.reason, isTerminal = false)
                }
            }
        }
    }

    fun resetState() {
        _trustState.value = TrustState.Idle
    }
}
