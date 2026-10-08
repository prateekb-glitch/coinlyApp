package com.example.coinly.data.security

import android.os.Build
import com.example.coinly.domain.model.AttestationData
import java.io.File
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.util.UUID

/**
 * Service responsible for device security attestation, anti-fraud checks (root, emulator,
 * bot detection via Play Integrity / reCAPTCHA), and cryptographic request signing.
 * Clean Architecture - Data Layer.
 */
class SecurityService {

    /**
     * Checks if the device is rooted or running on an emulator.
     * Heuristics based on OWASP MASVS guidance.
     */
    fun isDeviceSecure(): Boolean {
        return !isRooted() && !isEmulator()
    }

    private fun isRooted(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }
        val buildTags = Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }

    private fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.BOARD == "unknown"
                || Build.DEVICE == "generic"
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu"))
    }

    /**
     * Generates cryptographic attestation tokens (Play Integrity & reCAPTCHA) along with an HMAC signature
     * to prevent replayed requests and bot claims.
     */
    fun generateAttestation(action: String): AttestationData {
        val nonce = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        
        // In production, integrate com.google.android.play.core.integrity.IntegrityManager
        val playIntegrityToken = "mock_integrity_token_${nonce.take(8)}"
        
        // In production, integrate com.google.android.recaptcha.Recaptcha
        val recaptchaToken = "mock_recaptcha_token_${action}_${timestamp}"

        val payloadToSign = "$action:$nonce:$timestamp"
        val hmacSignature = hmacSha256(payloadToSign, "coinly_secret_signing_key_secure")

        return AttestationData(
            playIntegrityToken = playIntegrityToken,
            recaptchaToken = recaptchaToken,
            nonce = nonce,
            timestamp = timestamp,
            hmacSignature = hmacSignature
        )
    }

    private fun hmacSha256(data: String, secret: String): String {
        val sha256Hmac = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256")
        sha256Hmac.init(secretKey)
        val hash = sha256Hmac.doFinal(data.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }
}
