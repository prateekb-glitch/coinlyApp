package com.example.coinly.domain.repository

import com.example.coinly.domain.model.AttestationData

/**
 * Repository interface for gathering device integrity and bot protection attestation tokens.
 */
interface SecurityRepository {
    suspend fun generateAttestation(action: String): AttestationData
    fun isDeviceSecure(): Boolean
}
