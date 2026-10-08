package com.example.coinly.trust.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Retrofit API interface adhering to the Coinly Trust & Claim server contract.
 */
interface TrustApiService {

    @GET("nonce")
    suspend fun getNonce(): Response<NonceResponseDto>

    @POST("claim")
    suspend fun claimReward(@Body request: ClaimRequestDto): Response<ClaimResponseDto>

    @POST("claim/captcha")
    suspend fun verifyCaptcha(@Body request: CaptchaRequestDto): Response<CaptchaResponseDto>
}

data class NonceResponseDto(
    val nonce: String,
    val expiresInSec: Long
)

data class ClaimRequestDto(
    val taskId: String,
    val nonce: String,
    val integrityToken: String,
    val signals: SignalsDto,
    val idempotencyKey: String,
    val challengePass: String? = null
)

data class SignalsDto(
    val vpnActive: Boolean,
    val emulatorDetected: Boolean,
    val mockLocationEnabled: Boolean,
    val tamperDetected: Boolean
)

data class ClaimResponseDto(
    val success: Boolean,
    val message: String,
    val challengeId: String? = null // Present on 428
)

data class CaptchaRequestDto(
    val challengeId: String,
    val captchaToken: String
)

data class CaptchaResponseDto(
    val challengePass: String
)
