package com.example.coinly.trust.di

import com.example.coinly.trust.data.analytics.AnalyticsImpl
import com.example.coinly.trust.data.interceptor.RedactedLoggingInterceptor
import com.example.coinly.trust.data.provider.CaptchaProviderImpl
import com.example.coinly.trust.data.provider.IntegrityProviderImpl
import com.example.coinly.trust.data.provider.SignalCollectorImpl
import com.example.coinly.trust.data.remote.TrustApiService
import com.example.coinly.trust.data.remote.TrustMockServer
import com.example.coinly.trust.data.repository.TrustRepositoryImpl
import com.example.coinly.trust.data.secure.SecureStorage
import com.example.coinly.trust.core.analytics.Analytics
import com.example.coinly.trust.core.provider.CaptchaProvider
import com.example.coinly.trust.core.provider.IntegrityProvider
import com.example.coinly.trust.core.provider.SignalCollector
import com.example.coinly.trust.core.repository.TrustRepository
import com.example.coinly.trust.core.repository.TrustStorage
import com.example.coinly.trust.ui.TrustGateViewModel
import okhttp3.OkHttpClient
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Koin Dependency Injection module for the Trust and Claim feature.
 */
val trustModule = module {
    single<TrustStorage> { SecureStorage(get()) }
    single<Analytics> { AnalyticsImpl() }
    single<IntegrityProvider> { IntegrityProviderImpl() }
    single<CaptchaProvider> { CaptchaProviderImpl() }
    single<SignalCollector> { SignalCollectorImpl() }

    single {
        // Start TrustMockServer for local mock connection
        val mockServer = TrustMockServer()
        try {
            mockServer.start()
        } catch (e: Exception) {
            // Already started or bound
        }
        mockServer
    }

    single<TrustApiService> {
        val mockServer: TrustMockServer = get()
        val client = OkHttpClient.Builder()
            .addInterceptor(RedactedLoggingInterceptor())
            .build()

        Retrofit.Builder()
            .baseUrl(mockServer.url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TrustApiService::class.java)
    }

    single<TrustRepository> {
        TrustRepositoryImpl(get(), get(), get(), get(), get(), get())
    }

    viewModel { TrustGateViewModel(get(), get()) }
}
