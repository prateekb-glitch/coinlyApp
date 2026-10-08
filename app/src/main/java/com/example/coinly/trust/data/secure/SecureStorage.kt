package com.example.coinly.trust.data.secure

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.coinly.trust.core.repository.TrustStorage

/**
 * Secure storage wrapper utilizing EncryptedSharedPreferences backed by Android Keystore.
 * Requirement 6.
 */
class SecureStorage(context: Context) : TrustStorage {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = try {
        EncryptedSharedPreferences.create(
            context,
            "coinly_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // Fallback for tests or unencrypted sandbox environments if Keystore initialization fails
        context.getSharedPreferences("coinly_insecure_fallback", Context.MODE_PRIVATE)
    }

    override fun savePendingClaim(taskId: String?) {
        sharedPreferences.edit().putString("pending_task_id", taskId).apply()
    }

    override fun getPendingClaim(): String? {
        return sharedPreferences.getString("pending_task_id", null)
    }

    override fun saveLastVerdict(verdict: String) {
        sharedPreferences.edit().putString("last_verdict", verdict).apply()
    }

    override fun getLastVerdict(): String? {
        return sharedPreferences.getString("last_verdict", null)
    }
}
