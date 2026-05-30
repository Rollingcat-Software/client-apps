package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.model.IdentityEmail
import com.fivucsas.shared.domain.model.IdentityMe
import com.fivucsas.shared.domain.model.IdentityMembership
import com.fivucsas.shared.domain.repository.AccountLinkingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AccountLinkingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeAccountLinkingRepository
    private lateinit var viewModel: AccountLinkingViewModel

    private class FakeAccountLinkingRepository : AccountLinkingRepository {
        var identity: IdentityMe = IdentityMe(
            identityId = "id-1",
            emails = listOf(IdentityEmail("a@x.com", true)),
            memberships = listOf(
                IdentityMembership("u1", "t1", "Tenant One", "TENANT_MEMBER", isActive = true),
                IdentityMembership("u2", "t2", "Tenant Two", "TENANT_ADMIN", isActive = false)
            )
        )
        var failGet = false
        var lastInitiateEmail: String? = null
        var lastConfirm: Triple<String, String, String>? = null
        var lastUnlink: String? = null
        var lastSwitch: String? = null
        var switchSucceeds = true

        override suspend fun getIdentityMe(): Result<IdentityMe> =
            if (failGet) Result.failure(RuntimeException("boom")) else Result.success(identity)

        override suspend fun initiateLink(email: String): Result<Unit> {
            lastInitiateEmail = email; return Result.success(Unit)
        }

        override suspend fun confirmLink(email: String, otp: String, password: String): Result<Unit> {
            lastConfirm = Triple(email, otp, password); return Result.success(Unit)
        }

        override suspend fun unlink(membershipUserId: String): Result<Unit> {
            lastUnlink = membershipUserId; return Result.success(Unit)
        }

        override suspend fun switchMembership(targetUserId: String): Result<Unit> {
            lastSwitch = targetUserId
            return if (switchSucceeds) Result.success(Unit)
            else Result.failure(RuntimeException("not yours"))
        }
    }

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeAccountLinkingRepository()
        viewModel = AccountLinkingViewModel(repo)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load populates identity and computes canSwitch when memberships gt 1`() = runTest {
        viewModel.load()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.identity)
        assertEquals(2, state.memberships.size)
        assertTrue(state.canSwitch)
    }

    @Test
    fun `load surfaces error message on failure`() = runTest {
        repo.failGet = true
        viewModel.load()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.identity)
        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `initiateLink sends OTP and flips otpSent`() = runTest {
        viewModel.showLinkDialog()
        viewModel.initiateLink("new@x.com")
        advanceUntilIdle()

        assertEquals("new@x.com", repo.lastInitiateEmail)
        assertTrue(viewModel.uiState.value.linkOtpSent)
    }

    @Test
    fun `confirmLink forwards otp and password then closes dialog`() = runTest {
        viewModel.showLinkDialog()
        viewModel.confirmLink("new@x.com", "123456", "Secret1!")
        advanceUntilIdle()

        assertEquals(Triple("new@x.com", "123456", "Secret1!"), repo.lastConfirm)
        assertFalse(viewModel.uiState.value.showLinkDialog)
    }

    @Test
    fun `unlink forwards membership id`() = runTest {
        viewModel.unlink("u2")
        advanceUntilIdle()

        assertEquals("u2", repo.lastUnlink)
        assertNull(viewModel.uiState.value.unlinkingUserId)
    }

    @Test
    fun `switchMembership success flips switchSucceeded for navigation`() = runTest {
        viewModel.switchMembership("u2")
        advanceUntilIdle()

        assertEquals("u2", repo.lastSwitch)
        assertTrue(viewModel.uiState.value.switchSucceeded)

        viewModel.consumeSwitchSucceeded()
        assertFalse(viewModel.uiState.value.switchSucceeded)
    }

    @Test
    fun `switchMembership failure surfaces error and does not signal navigation`() = runTest {
        repo.switchSucceeds = false
        viewModel.switchMembership("u2")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.switchSucceeded)
        assertNotNull(viewModel.uiState.value.errorMessage)
    }
}
