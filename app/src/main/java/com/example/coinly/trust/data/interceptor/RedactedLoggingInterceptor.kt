package com.example.coinly.trust.data.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Custom OkHttp interceptor that sanitizes logs, ensuring sensitive tokens, nonces,
 * and authorization headers are never logged.
 * Requirement 6.
 */
class RedactedLoggingInterceptor : Interceptor {
    private val sensitiveHeaders = listOf("Authorization", "X-Integrity-Token", "X-Nonce", "Cookie")

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        // Redact sensitive headers
        for (header in sensitiveHeaders) {
            if (originalRequest.header(header) != null) {
                requestBuilder.header(header, "[REDACTED]")
            }
        }

        val request = requestBuilder.build()
        // Perform request
        val response = chain.proceed(request)
        return response
    }
}
