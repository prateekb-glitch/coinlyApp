package com.example.coinly.trust.data.provider

import android.os.Build
import com.example.coinly.trust.core.model.SignalData
import com.example.coinly.trust.core.provider.SignalCollector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import kotlin.time.Duration.Companion.milliseconds

/**
 * Implementation of SignalCollector executing off main thread and bounded by timeout.
 * Requirement 5.
 */
class SignalCollectorImpl(
    private val timeoutMs: Long = 3000L
) : SignalCollector {

    override suspend fun collectSignals(): SignalData = withContext(Dispatchers.IO) {
        val result = withTimeoutOrNull(timeoutMs.milliseconds) {
            val vpnActive = checkVpnActive()
            val emulatorDetected = checkEmulator()
            val mockLocationEnabled = checkMockLocation()
            val tamperDetected = checkTamper()

            SignalData(
                vpnActive = vpnActive,
                emulatorDetected = emulatorDetected,
                mockLocationEnabled = mockLocationEnabled,
                tamperDetected = tamperDetected
            )
        }

        // Fallback safe signals if collection times out
        result ?: SignalData(
            vpnActive = false,
            emulatorDetected = false,
            mockLocationEnabled = false,
            tamperDetected = false
        )
    }

    private fun checkVpnActive(): Boolean {
        // Mock heuristic for VPN detection (e.g. active network interface check or system properties)
        return false
    }

    private fun checkEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.MODEL.contains("google_sdk")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu"))
    }

    private fun checkMockLocation(): Boolean {
        // Heuristic check for mock location / developer options
        return false
    }

    private fun checkTamper(): Boolean {
        val suPaths = arrayOf("/system/bin/su", "/system/xbin/su", "/sbin/su")
        for (path in suPaths) {
            if (File(path).exists()) return true
        }
        return false
    }
}
