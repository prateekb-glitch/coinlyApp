package com.example.coinly.domain.usecase

import com.example.coinly.domain.model.TaskItem
import com.example.coinly.domain.repository.RewardRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to retrieve available user tasks.
 */
class GetTasksUseCase(
    private val rewardRepository: RewardRepository
) {
    operator fun invoke(): Flow<List<TaskItem>> {
        return rewardRepository.observeTasks()
    }
}
