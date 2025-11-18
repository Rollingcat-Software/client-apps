package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.presentation.state.KioskScreen
import com.fivucsas.shared.test.mocks.MockCheckLivenessUseCase
import com.fivucsas.shared.test.mocks.MockEnrollUserUseCase
import com.fivucsas.shared.test.mocks.MockVerifyUserUseCase
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

/**
 * Unit tests for KioskViewModel
 *
 * Tests cover:
 * - Navigation between screens
 * - Enrollment data updates
 * - Enrollment submission
 * - Verification process
 * - Camera capture
 * - Message handling
 * - Error states
 */
@OptIn(ExperimentalCoroutinesApi::class)
class KioskViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var enrollUserUseCase: MockEnrollUserUseCase
    private lateinit var verifyUserUseCase: MockVerifyUserUseCase
    private lateinit var checkLivenessUseCase: MockCheckLivenessUseCase
    private lateinit var viewModel: KioskViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        enrollUserUseCase = MockEnrollUserUseCase()
        verifyUserUseCase = MockVerifyUserUseCase()
        checkLivenessUseCase = MockCheckLivenessUseCase()

        viewModel = KioskViewModel(
            enrollUserUseCase = enrollUserUseCase,
            verifyUserUseCase = verifyUserUseCase,
            checkLivenessUseCase = checkLivenessUseCase
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========================================
    // Navigation Tests
    // ========================================

    @Test
    fun `initial screen should be WELCOME`() {
        assertEquals(KioskScreen.WELCOME, viewModel.uiState.value.currentScreen)
    }

    @Test
    fun `navigateToWelcome should set screen to WELCOME`() {
        viewModel.navigateToEnroll()
        viewModel.navigateToWelcome()

        assertEquals(KioskScreen.WELCOME, viewModel.uiState.value.currentScreen)
    }

    @Test
    fun `navigateToWelcome should clear all state`() {
        viewModel.navigateToEnroll()
        viewModel.updateFullName("Test")
        viewModel.captureImage()

        viewModel.navigateToWelcome()

        assertNull(viewModel.uiState.value.errorMessage)
        assertNull(viewModel.uiState.value.successMessage)
        assertNull(viewModel.uiState.value.verificationResult)
        assertNull(viewModel.uiState.value.capturedImage)
        assertEquals("", viewModel.enrollmentData.value.fullName)
    }

    @Test
    fun `navigateToEnroll should set screen to ENROLL`() {
        viewModel.navigateToEnroll()

        assertEquals(KioskScreen.ENROLL, viewModel.uiState.value.currentScreen)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `navigateToVerify should set screen to VERIFY`() {
        viewModel.navigateToVerify()

        assertEquals(KioskScreen.VERIFY, viewModel.uiState.value.currentScreen)
        assertNull(viewModel.uiState.value.verificationResult)
    }

    // ========================================
    // Enrollment Data Tests
    // ========================================

    @Test
    fun `updateFullName should update enrollment data`() {
        viewModel.updateFullName("John Doe")

        assertEquals("John Doe", viewModel.enrollmentData.value.fullName)
    }

    @Test
    fun `updateEmail should update enrollment data`() {
        viewModel.updateEmail("john@example.com")

        assertEquals("john@example.com", viewModel.enrollmentData.value.email)
    }

    @Test
    fun `updateIdNumber should update enrollment data`() {
        viewModel.updateIdNumber("ID123456")

        assertEquals("ID123456", viewModel.enrollmentData.value.idNumber)
    }

    @Test
    fun `updatePhoneNumber should update enrollment data`() {
        viewModel.updatePhoneNumber("+1234567890")

        assertEquals("+1234567890", viewModel.enrollmentData.value.phoneNumber)
    }

    @Test
    fun `updateAddress should update enrollment data`() {
        viewModel.updateAddress("123 Main St")

        assertEquals("123 Main St", viewModel.enrollmentData.value.address)
    }

    // ========================================
    // Camera Tests
    // ========================================

    @Test
    fun `openCamera should set showCamera state`() {
        viewModel.openCamera()

        assertTrue(viewModel.uiState.value.showCamera)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `closeCamera should clear showCamera state`() {
        viewModel.openCamera()
        viewModel.closeCamera()

        assertFalse(viewModel.uiState.value.showCamera)
    }

    @Test
    fun `captureImage should store captured image`() {
        viewModel.captureImage()

        assertNotNull(viewModel.uiState.value.capturedImage)
        assertFalse(viewModel.uiState.value.showCamera)
        assertNotNull(viewModel.uiState.value.successMessage)
    }

    @Test
    fun `setCapturedImage should update with provided bytes`() {
        val imageBytes = ByteArray(100) { it.toByte() }

        viewModel.setCapturedImage(imageBytes)

        assertEquals(imageBytes, viewModel.uiState.value.capturedImage)
        assertFalse(viewModel.uiState.value.showCamera)
    }

    // ========================================
    // Enrollment Submission Tests
    // ========================================

    @Test
    fun `submitEnrollment should fail when name is blank`() = runTest {
        viewModel.updateEmail("test@example.com")
        viewModel.updateIdNumber("ID123")
        viewModel.captureImage()

        viewModel.submitEnrollment()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
        assertTrue(viewModel.uiState.value.errorMessage!!.contains("name", ignoreCase = true))
    }

    @Test
    fun `submitEnrollment should fail when email is blank`() = runTest {
        viewModel.updateFullName("John Doe")
        viewModel.updateIdNumber("ID123")
        viewModel.captureImage()

        viewModel.submitEnrollment()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
        assertTrue(viewModel.uiState.value.errorMessage!!.contains("email", ignoreCase = true))
    }

    @Test
    fun `submitEnrollment should fail when ID number is blank`() = runTest {
        viewModel.updateFullName("John Doe")
        viewModel.updateEmail("test@example.com")
        viewModel.captureImage()

        viewModel.submitEnrollment()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
        assertTrue(viewModel.uiState.value.errorMessage!!.contains("id", ignoreCase = true))
    }

    @Test
    fun `submitEnrollment should fail when photo is not captured`() = runTest {
        viewModel.updateFullName("John Doe")
        viewModel.updateEmail("test@example.com")
        viewModel.updateIdNumber("ID123")

        viewModel.submitEnrollment()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
        assertTrue(viewModel.uiState.value.errorMessage!!.contains("photo", ignoreCase = true))
    }

    @Test
    fun `submitEnrollment should succeed with valid data`() = runTest {
        viewModel.updateFullName("John Doe")
        viewModel.updateEmail("john@example.com")
        viewModel.updateIdNumber("ID123456")
        viewModel.captureImage()

        viewModel.submitEnrollment()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.successMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `submitEnrollment should set loading state`() = runTest {
        viewModel.updateFullName("John Doe")
        viewModel.updateEmail("john@example.com")
        viewModel.updateIdNumber("ID123456")
        viewModel.captureImage()

        viewModel.submitEnrollment()

        // Check loading state before completion
        assertTrue(viewModel.uiState.value.isLoading)

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    // ========================================
    // Verification Tests
    // ========================================

    @Test
    fun `startVerification should open camera`() {
        viewModel.startVerification()

        assertTrue(viewModel.uiState.value.showCamera)
    }

    @Test
    fun `verifyWithCapturedImage should fail without image`() = runTest {
        viewModel.verifyWithCapturedImage()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
        assertTrue(viewModel.uiState.value.errorMessage!!.contains("photo", ignoreCase = true))
    }

    @Test
    fun `verifyWithCapturedImage should set verification result`() = runTest {
        viewModel.captureImage()
        viewModel.verifyWithCapturedImage()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.verificationResult)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `verifyWithCapturedImage should set loading state`() = runTest {
        viewModel.captureImage()
        viewModel.verifyWithCapturedImage()

        // Check loading state before completion
        assertTrue(viewModel.uiState.value.isLoading)

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    // ========================================
    // Message Control Tests
    // ========================================

    @Test
    fun `clearMessages should reset all messages`() {
        viewModel.clearMessages()

        assertNull(viewModel.uiState.value.errorMessage)
        assertNull(viewModel.uiState.value.successMessage)
    }

    @Test
    fun `clearError should only clear error message`() {
        viewModel.clearError()

        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `clearSuccess should only clear success message`() {
        viewModel.clearSuccess()

        assertNull(viewModel.uiState.value.successMessage)
    }
}
