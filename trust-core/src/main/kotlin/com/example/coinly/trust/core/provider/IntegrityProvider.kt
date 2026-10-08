package com.example.coinly.trust.core.provider

interface IntegrityProvider {
    suspend fun getIntegrityToken(nonce: String): Result<String>
}
