package com.example.coinly.trust.core.analytics

interface Analytics {
    fun trackRequested(taskId: String)
    fun trackResult(taskId: String, result: String)
    fun trackAction(taskId: String, action: String)
    fun trackRetry(taskId: String, reason: String, attempt: Int)
}
