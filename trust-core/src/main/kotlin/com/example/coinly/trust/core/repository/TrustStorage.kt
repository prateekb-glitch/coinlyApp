package com.example.coinly.trust.core.repository

interface TrustStorage {
    fun savePendingClaim(taskId: String?)
    fun getPendingClaim(): String?
    fun saveLastVerdict(verdict: String)
    fun getLastVerdict(): String?
}
