package com.example.coinly.trust.data.repository

import com.example.coinly.trust.data.remote.*
import com.example.coinly.trust.core.analytics.Analytics
import com.example.coinly.trust.core.model.TrustPolicy
import com.example.coinly.trust.core.provider.CaptchaProvider
import com.example.coinly.trust.core.provider.IntegrityProvider
import com.example.coinly.trust.core.provider.SignalCollector
import com.example.coinly.trust.core.repository.TrustClaimResult
import com.example.coinly.trust.core.repository.TrustRepository
import com.example.coinly.trust.core.repository.TrustStorage
import kotlinx.coroutines.delay
import java.util.UUID
import kotlin.math.min
import kotlin.random.Random

/**
 * Implementation of TrustRepository handling robust failure semantics, 423 terminal errors,
 * 428 captcha flows, 409 nonce refresh, 429 rate limiting with Retry-After, and idempotency.
 */
class TrustRepositoryImpl(
    private val apiService: TrustApiService,
    private val integrityProvider: IntegrityProvider,
    private val captchaProvider: CaptchaProvider,
    private val signalCollector: SignalCollector,
    private val secureStorage: TrustStorage,
    private val analytics: Analytics
) : TrustRepository {

    override suspend fun claimRewardWithTrust(taskId: String, policy: TrustPolicy): TrustClaimResult {
        analytics.trackRequested(taskId)
        secureStorage.savePendingClaim(taskId)

        var attempt = 0
        val maxAttempts = 3
        var challengePass: String? = null

        while (attempt < maxAttempts) {
            attempt++
            try {
                // 1. Collect signals off main thread
                val signals = signalCollector.collectSignals()

                // Check VPN product rule
                if (signals.vpnActive && policy.vpnBlocks) {
                    analytics.trackAction(taskId, "BLOCK")
                    analytics.trackResult(taskId, "FAIL")
                    secureStorage.savePendingClaim(null)
                    return TrustClaimResult.DeviceCompromised("Active VPN detected. Please disable VPN to claim rewards.")
                }

                // 2. Fetch fresh nonce
                val nonceResponse = apiService.getNonce()
                if (!nonceResponse.isSuccessful || nonceResponse.body() == null) {
                    throw IllegalStateException("Failed to fetch server nonce")
                }
                val nonce = nonceResponse.body()!!.nonce

                // 3. Obtain integrity token via provider (single-flight supported)
                val tokenResult = integrityProvider.getIntegrityToken(nonce)
                val integrityToken = tokenResult.getOrElse { "fallback_token" }

                // 4. Generate idempotency key for taskId
                val idempotencyKey = UUID.nameUUIDFromBytes(taskId.toByteArray()).toString()

                // 5. Build claim request
                val requestDto = ClaimRequestDto(
                    taskId = taskId,
                    nonce = nonce,
                    integrityToken = integrityToken,
                    signals = SignalsDto(
                        vpnActive = signals.vpnActive,
                        emulatorDetected = signals.emulatorDetected,
                        mockLocationEnabled = signals.mockLocationEnabled,
                        tamperDetected = signals.tamperDetected
                    ),
                    idempotencyKey = idempotencyKey,
                    challengePass = challengePass
                )

                val response = apiService.claimReward(requestDto)

                when (response.code()) {
                    200 -> {
                        secureStorage.savePendingClaim(null)
                        analytics.trackAction(taskId, "ALLOW")
                        analytics.trackResult(taskId, "PASS")
                        return TrustClaimResult.Success(response.body()?.message ?: "Rewarded successfully!")
                    }
                    423 -> {
                        // Terminal device compromised
                        secureStorage.savePendingClaim(null)
                        analytics.trackAction(taskId, "BLOCK")
                        analytics.trackResult(taskId, "FAIL")
                        return TrustClaimResult.DeviceCompromised("Device compromised or emulated. Claim blocked.")
                    }
                    428 -> {
                        // Captcha required
                        val challengeId = response.body()?.challengeId ?: "chal_default"
                        val captchaResult = captchaProvider.showCaptchaChallenge(challengeId)
                        val captchaToken = captchaResult.getOrNull()
                        if (captchaToken != null) {
                            val captchaResp = apiService.verifyCaptcha(CaptchaRequestDto(challengeId, captchaToken))
                            if (captchaResp.isSuccessful && captchaResp.body() != null) {
                                challengePass = captchaResp.body()!!.challengePass
                                continue // retry claim with challengePass
                            }
                        }
                        return TrustClaimResult.ChallengeNeeded(challengeId, taskId)
                    }
                    409 -> {
                        // Nonce expired or already used -> refresh nonce and retry once
                        analytics.trackRetry(taskId, "Nonce expired (409), refreshing", attempt)
                        continue
                    }
                    429 -> {
                        // Rate limited, respect Retry-After header
                        val retryAfterHeader = response.headers()["Retry-After"]
                        val waitSec = retryAfterHeader?.toLongOrNull() ?: (2L * attempt)
                        delay(waitSec * 1000)
                        continue
                    }
                    else -> {
                        if (response.code() in 500..599 && attempt < maxAttempts) {
                            val backoff = min(1000L * (1 shl attempt), 8000L) + Random.nextLong(200)
                            analytics.trackRetry(taskId, "Transient server error ${response.code()}", attempt)
                            delay(backoff)
                            continue
                        } else {
                            analytics.trackResult(taskId, "ERROR")
                            return TrustClaimResult.Failed("Server error: ${response.code()}")
                        }
                    }
                }
            } catch (e: Exception) {
                if (attempt >= maxAttempts) {
                    analytics.trackResult(taskId, "ERROR")
                    secureStorage.savePendingClaim(null)
                    return TrustClaimResult.Failed(e.localizedMessage ?: "Unknown network exception")
                }
                val backoff = min(1000L * (1 shl attempt), 8000L) + Random.nextLong(200)
                analytics.trackRetry(taskId, e.localizedMessage ?: "Network exception", attempt)
                delay(backoff)
            }
        }

        secureStorage.savePendingClaim(null)
        analytics.trackResult(taskId, "ERROR")
        return TrustClaimResult.Failed("Max retry attempts reached")
    }
}
