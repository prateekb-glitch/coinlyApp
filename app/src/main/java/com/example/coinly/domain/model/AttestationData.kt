package com.example.coinly.domain.model

/**
 * Domain model representing security attestation tokens and cryptographic sign-offs
 * required to prevent fraud (emulators, bots, replayed requests).
 */
data class AttestationData(
    val playIntegrityToken: String,
    val recaptchaToken: String,
    val nonce: String,
    val timestamp: Long,
    val hmacSignature: String
)
