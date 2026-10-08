package com.example.coinly.trust.data.remote

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import java.util.UUID

/**
 * OkHttp MockWebServer implementation representing the server contract.
 * Supports testing and offline mock operation.
 */
class TrustMockServer {
    private val server = MockWebServer()
    private var nonceCount = 0

    fun start(port: Int = 8080) {
        try {
            server.start(port)
        } catch (e: Exception) {
            server.start() // start on random available port if 8080 is bound
        }
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.path ?: ""
                return when {
                    path.contains("/nonce") -> {
                        nonceCount++
                        val nonce = "nonce_${UUID.randomUUID().toString().take(8)}_${nonceCount}"
                        MockResponse()
                            .setResponseCode(200)
                            .setBody("{\"nonce\":\"$nonce\",\"expiresInSec\":300}")
                    }
                    path.contains("/claim/captcha") -> {
                        MockResponse()
                            .setResponseCode(200)
                            .setBody("{\"challengePass\":\"pass_${UUID.randomUUID().toString().take(8)}\"}")
                    }
                    path.contains("/claim") -> {
                        // For testing scenarios or general claims
                        val body = request.body.readUtf8()
                        if (body.contains("compromise_device")) {
                            MockResponse()
                                .setResponseCode(423)
                                .setBody("{\"success\":false,\"message\":\"Device compromised\"}")
                        } else if (body.contains("trigger_captcha") && !body.contains("challengePass")) {
                            MockResponse()
                                .setResponseCode(428)
                                .setBody("{\"success\":false,\"message\":\"Captcha required\",\"challengeId\":\"chal_123\"}")
                        } else if (body.contains("simulate_409")) {
                            MockResponse()
                                .setResponseCode(409)
                                .setBody("{\"success\":false,\"message\":\"Nonce expired or already used\"}")
                        } else {
                            MockResponse()
                                .setResponseCode(200)
                                .setBody("{\"success\":true,\"message\":\"Reward claimed successfully!\"}")
                        }
                    }
                    else -> MockResponse().setResponseCode(404)
                }
            }
        }
    }

    fun shutdown() {
        server.shutdown()
    }

    val url: String
        get() = server.url("/").toString()
}
