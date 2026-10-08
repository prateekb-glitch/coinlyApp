package com.example.coinly.trust.data.provider

import com.example.coinly.trust.core.provider.IntegrityProvider
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

/**
 * Implementation of IntegrityProvider with caching, expiry, and single-flight concurrency control.
 * Requirement 1.
 */
class IntegrityProviderImpl : IntegrityProvider {
    private val mutex = Mutex()
    private var cachedToken: String? = null
    private var tokenExpiryTime: Long = 0L
    private val tokenTtlMs = 60_000L // 60 seconds TTL

    override suspend fun getIntegrityToken(nonce: String): Result<String> = mutex.withLock {
        val currentTime = System.currentTimeMillis()
        if (cachedToken != null && currentTime < tokenExpiryTime) {
            return@withLock Result.success(cachedToken!!)
        }

        // Single-flight execution: generate/fetch new token bound to nonce
        try {
            // Simulating Play Integrity API call bound to nonce
            kotlinx.coroutines.delay(200)
            val token = "integrity_token_${nonce}_${UUID.randomUUID().toString().take(6)}"
            cachedToken = token
            tokenExpiryTime = currentTime + tokenTtlMs
            Result.success(token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
