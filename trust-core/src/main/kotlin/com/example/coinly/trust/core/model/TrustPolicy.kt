package com.example.coinly.trust.core.model

data class TrustPolicy(
    val vpnBlocks: Boolean = true,
    val emulatorBlocks: Boolean = true,
    val mockLocationEnabled: Boolean = false,
    val tamperBlocks: Boolean = true,
    val signalTimeoutMs: Long = 3000L
)
