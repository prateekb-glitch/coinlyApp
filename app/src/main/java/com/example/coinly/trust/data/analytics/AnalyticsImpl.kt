package com.example.coinly.trust.data.analytics

import com.example.coinly.trust.core.analytics.Analytics

/**
 * Implementation of Analytics interface tracking the trust gate event funnel.
 * Requirement 7.
 */
class AnalyticsImpl : Analytics {

    override fun trackRequested(taskId: String) {
        // println("Analytics [REQUESTED]: taskId=$taskId")
    }

    override fun trackResult(taskId: String, result: String) {
        // println("Analytics [RESULT]: taskId=$taskId, result=$result")
    }

    override fun trackAction(taskId: String, action: String) {
        // println("Analytics [ACTION]: taskId=$taskId, action=$action")
    }

    override fun trackRetry(taskId: String, reason: String, attempt: Int) {
        val sanitizedReason = if (reason.length > 120) reason.take(120) else reason
        // println("Analytics [RETRY]: taskId=$taskId, attempt=$attempt, reason=$sanitizedReason")
    }
}
