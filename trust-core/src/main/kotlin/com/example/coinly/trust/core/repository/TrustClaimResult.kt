package com.example.coinly.trust.core.repository

sealed interface TrustClaimResult {
    data class Success(val message: String) : TrustClaimResult
    data class DeviceCompromised(val reason: String) : TrustClaimResult
    data class ChallengeNeeded(val challengeId: String, val taskId: String) : TrustClaimResult
    data class Failed(val reason: String) : TrustClaimResult
}
