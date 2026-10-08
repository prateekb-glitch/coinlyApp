package com.example.coinly.data.repository

import com.example.coinly.data.security.SecurityService
import com.example.coinly.domain.model.AttestationData
import com.example.coinly.domain.repository.SecurityRepository

/**
 * Implementation of SecurityRepository using SecurityService.
 * Clean Architecture - Data Layer.
 */
class SecurityRepositoryImpl(
    private val securityService: SecurityService
) : SecurityRepository {

    override suspend fun generateAttestation(action: String): AttestationData {
        return securityService.generateAttestation(action)
    }

    override fun isDeviceSecure(): Boolean {
        return securityService.isDeviceSecure()
    }
}
