package com.example.coinly.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Retrofit API interface for backend communication.
 * Clean Architecture - Data Layer.
 */
interface CoinlyApiService {
    @GET("api/v1/user/balance")
    suspend fun getBalance(): Response<BalanceDto>

    @GET("api/v1/tasks")
    suspend fun getTasks(): Response<List<TaskDto>>

    @POST("api/v1/rewards/claim")
    suspend fun claimReward(@Body request: ClaimRequestDto): Response<ClaimResponseDto>
}

data class BalanceDto(
    val totalCoins: Int,
    val cashValueUsd: Double
)

data class TaskDto(
    val id: String,
    val title: String,
    val description: String,
    val rewardCoins: Int,
    val category: String,
    val isCompleted: Boolean
)

data class ClaimRequestDto(
    val taskId: String,
    val playIntegrityToken: String,
    val recaptchaToken: String,
    val nonce: String,
    val timestamp: Long,
    val hmacSignature: String
)

data class ClaimResponseDto(
    val success: Boolean,
    val message: String,
    val newBalance: BalanceDto
)
