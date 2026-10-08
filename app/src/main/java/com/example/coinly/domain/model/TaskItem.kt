package com.example.coinly.domain.model

/**
 * Domain model representing a completable task (e.g., app install, survey) yielding coin rewards.
 * Clean Architecture - Domain Layer.
 */
data class TaskItem(
    val id: String,
    val title: String,
    val description: String,
    val rewardCoins: Int,
    val category: TaskCategory,
    val isCompleted: Boolean = false
)

enum class TaskCategory {
    APP_INSTALL,
    SURVEY,
    ENGAGEMENT
}
