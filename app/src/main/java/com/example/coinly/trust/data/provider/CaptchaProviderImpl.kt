package com.example.coinly.trust.data.provider

import com.example.coinly.trust.core.provider.CaptchaProvider
import kotlinx.coroutines.delay

/**
 * Implementation of CaptchaProvider.
 * Requirement 4.
 */
class CaptchaProviderImpl : CaptchaProvider {
    override suspend fun showCaptchaChallenge(challengeId: String): Result<String> {
        // Simulate user solving reCAPTCHA challenge
        delay(500)
        return Result.success("captcha_token_solved_$challengeId")
    }
}
