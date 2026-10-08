package com.example.coinly.trust

import app.cash.turbine.test
import com.example.coinly.trust.core.model.TrustPolicy
import com.example.coinly.trust.core.model.TrustState
import com.example.coinly.trust.core.repository.TrustClaimResult
import com.example.coinly.trust.core.repository.TrustRepository
import com.example.coinly.trust.core.repository.TrustStorage
import com.example.coinly.trust.ui.TrustGateViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TrustGateViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeTrustRepository
    private lateinit var fakeStorage: FakeSecureStorage
    private lateinit var viewModel: TrustGateViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeTrustRepository()
        fakeStorage = FakeSecureStorage()
        viewModel = TrustGateViewModel(fakeRepository, fakeStorage)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSuccessfulClaimFlow() = runTest {
        fakeRepository.resultToReturn = TrustClaimResult.Success("Claimed successfully")

        viewModel.trustState.test {
            assertEquals(TrustState.Idle, awaitItem())

            viewModel.claimReward("task_1")
            assertEquals(TrustState.Checking, awaitItem())
            assertEquals(TrustState.Allowed, awaitItem())
        }
    }

    @Test
    fun testDeviceCompromisedTerminalState() = runTest {
        fakeRepository.resultToReturn = TrustClaimResult.DeviceCompromised("Root or emulator detected")

        viewModel.trustState.test {
            assertEquals(TrustState.Idle, awaitItem())

            viewModel.claimReward("task_1")
            assertEquals(TrustState.Checking, awaitItem())
            assertEquals(TrustState.Blocked("Root or emulator detected", isTerminal = true), awaitItem())
        }
    }

    @Test
    fun testChallengeRequiredState() = runTest {
        fakeRepository.resultToReturn = TrustClaimResult.ChallengeNeeded("chal_123", "task_1")

        viewModel.trustState.test {
            assertEquals(TrustState.Idle, awaitItem())

            viewModel.claimReward("task_1")
            assertEquals(TrustState.Checking, awaitItem())
            assertEquals(TrustState.ChallengeRequired("chal_123", "task_1"), awaitItem())
        }
    }
}

class FakeTrustRepository : TrustRepository {
    var resultToReturn: TrustClaimResult = TrustClaimResult.Success("Default success")

    override suspend fun claimRewardWithTrust(taskId: String, policy: TrustPolicy): TrustClaimResult {
        return resultToReturn
    }
}

class FakeSecureStorage : TrustStorage {
    private var pending: String? = null
    private var verdict: String? = null

    override fun savePendingClaim(taskId: String?) {
        pending = taskId
    }

    override fun getPendingClaim(): String? {
        return pending
    }

    override fun saveLastVerdict(verdict: String) {
        this.verdict = verdict
    }

    override fun getLastVerdict(): String? {
        return verdict
    }
}
