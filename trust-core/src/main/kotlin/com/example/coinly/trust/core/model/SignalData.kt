package com.example.coinly.trust.core.model

data class SignalData(
    val vpnActive: Boolean,
    val emulatorDetected: Boolean,
    val mockLocationEnabled: Boolean,
    val tamperDetected: Boolean
)
