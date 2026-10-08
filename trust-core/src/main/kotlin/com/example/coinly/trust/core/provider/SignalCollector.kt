package com.example.coinly.trust.core.provider

import com.example.coinly.trust.core.model.SignalData

interface SignalCollector {
    suspend fun collectSignals(): SignalData
}
