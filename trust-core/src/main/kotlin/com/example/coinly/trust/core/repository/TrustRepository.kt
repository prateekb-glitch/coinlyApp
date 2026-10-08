package com.example.coinly.trust.core.repository

import com.example.coinly.trust.core.model.TrustPolicy

interface TrustRepository {
    suspend fun claimRewardWithTrust(taskId: String, policy: TrustPolicy): TrustClaimResult
}
