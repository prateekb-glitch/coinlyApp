package com.example.coinly.di

import com.example.coinly.data.repository.RewardRepositoryImpl
import com.example.coinly.data.repository.SecurityRepositoryImpl
import com.example.coinly.data.security.SecurityService
import com.example.coinly.domain.repository.RewardRepository
import com.example.coinly.domain.repository.SecurityRepository
import com.example.coinly.domain.usecase.ClaimRewardUseCase
import com.example.coinly.domain.usecase.GetCoinBalanceUseCase
import com.example.coinly.domain.usecase.GetTasksUseCase
import com.example.coinly.domain.usecase.RedeemCoinsUseCase
import com.example.coinly.ui.reward.RewardViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin Dependency Injection module for Coinly app.
 * Adheres to stack constraints (no Hilt, lightweight Koin DI).
 */
val appModule = module {
    // Security & Data Services
    single { SecurityService() }
    single<SecurityRepository> { SecurityRepositoryImpl(get()) }
    single<RewardRepository> { RewardRepositoryImpl(get()) }

    // Domain Use Cases
    factory { ClaimRewardUseCase(get(), get()) }
    factory { GetTasksUseCase(get()) }
    factory { GetCoinBalanceUseCase(get()) }
    factory { RedeemCoinsUseCase(get(), get()) }

    // Presentation ViewModels
    viewModel { RewardViewModel(get(), get(), get(), get()) }
}
