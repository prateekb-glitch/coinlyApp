package com.example.coinly.trust.core.provider

interface CaptchaProvider {
    suspend fun showCaptchaChallenge(challengeId: String): Result<String>
}
