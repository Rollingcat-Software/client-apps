# Day 7: Testing Infrastructure 🧪

**Date:** November 3, 2025  
**Status:** In Progress  
**Estimated Time:** 60-90 minutes  
**Difficulty:** MEDIUM  
**Impact:** HIGH - Production quality!

---

## 📊 What We'll Achieve

### Before Day 7 ⚠️
```kotlin
// No tests - hope it works!
class GetUsersUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): List<User> {
        return userRepository.getUsers()
    }
}
// How do we know it works? 🤷
```

### After Day 7 ✅
```kotlin
// Comprehensive tests - confidence!
@Test
fun `getUsers should return list of users`() = runTest {
    // Given
    val mockRepo = mockk<UserRepository>()
    coEvery { mockRepo.getUsers() } returns listOf(testUser1, testUser2)
    val useCase = GetUsersUseCase(mockRepo)
    
    // When
    val result = useCase()
    
    // Then
    assertEquals(2, result.size)
    coVerify { mockRepo.getUsers() }
}
// We KNOW it works! ✅
```

---

## 🎯 Goals

1. ✅ Add testing dependencies (MockK, Turbine, Coroutine Test)
2. ✅ Create test utilities and helpers
3. ✅ Write use case tests
4. ✅ Write repository tests
5. ✅ Write ViewModel tests
6. ✅ Add test coverage reporting
7. ✅ Verify all tests pass

---

## 📦 Step 1: Add Testing Dependencies (10 minutes)

### 1.1 Update `shared/build.gradle.kts`

Add testing dependencies to the common test source set:

```kotlin
kotlin {
    sourceSets {
        val commonMain by getting {
            // ... existing dependencies
        }
        
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                
                // Coroutines Test
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
                
                // Turbine (for Flow testing)
                implementation("app.cash.turbine:turbine:1.0.0")
                
                // MockK (for mocking)
                implementation("io.mockk:mockk:1.13.8")
            }
        }
        
        val androidUnitTest by getting {
            dependencies {
                implementation("io.mockk:mockk-android:1.13.8")
                implementation("androidx.test:core:1.5.0")
                implementation("org.robolectric:robolectric:4.11.1")
            }
        }
    }
}
```

---

## 🏗️ Step 2: Create Test Utilities (15 minutes)

### 2.1 Create Test Data Factory

**File:** `shared/src/commonTest/kotlin/com/fivucsas/shared/test/TestData.kt`

```kotlin
package com.fivucsas.shared.test

import com.fivucsas.shared.domain.model.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Test data factory
 * Provides consistent test data across all tests
 */
object TestData {
    
    // Test Users
    val testUser1 = User(
        id = "user-1",
        firstName = "John",
        lastName = "Doe",
        email = "john.doe@test.com",
        phoneNumber = "+1234567890",
        status = UserStatus.ACTIVE,
        enrolledAt = Clock.System.now(),
        lastVerifiedAt = null,
        biometricDataId = "bio-1"
    )
    
    val testUser2 = User(
        id = "user-2",
        firstName = "Jane",
        lastName = "Smith",
        email = "jane.smith@test.com",
        phoneNumber = "+0987654321",
        status = UserStatus.ACTIVE,
        enrolledAt = Clock.System.now(),
        lastVerifiedAt = null,
        biometricDataId = "bio-2"
    )
    
    val inactiveUser = User(
        id = "user-3",
        firstName = "Bob",
        lastName = "Inactive",
        email = "bob@test.com",
        phoneNumber = "+1111111111",
        status = UserStatus.INACTIVE,
        enrolledAt = Clock.System.now(),
        lastVerifiedAt = null,
        biometricDataId = null
    )
    
    // Test Statistics
    val testStatistics = Statistics(
        totalUsers = 100,
        activeUsers = 85,
        inactiveUsers = 15,
        verificationsToday = 250,
        verificationsPastWeek = 1500,
        successRate = 95.5,
        failedAttempts = 12
    )
    
    // Test Enrollment Data
    val testEnrollmentData = EnrollmentData(
        firstName = "Test",
        lastName = "User",
        email = "test@example.com",
        phoneNumber = "+1234567890",
        imageData = "base64EncodedImage"
    )
    
    // Test Verification Data
    val testVerificationData = VerificationData(
        imageData = "base64EncodedImage"
    )
    
    // Helper functions
    fun createTestUser(
        id: String = "test-id",
        firstName: String = "Test",
        lastName: String = "User",
        email: String = "test@example.com",
        status: UserStatus = UserStatus.ACTIVE
    ) = User(
        id = id,
        firstName = firstName,
        lastName = lastName,
        email = email,
        phoneNumber = "+1234567890",
        status = status,
        enrolledAt = Clock.System.now(),
        lastVerifiedAt = null,
        biometricDataId = null
    )
    
    fun createTestUsers(count: Int): List<User> {
        return (1..count).map { i ->
            createTestUser(
                id = "user-$i",
                firstName = "User",
                lastName = "$i",
                email = "user$i@test.com"
            )
        }
    }
}
```

### 2.2 Create Test Extensions

**File:** `shared/src/commonTest/kotlin/com/fivucsas/shared/test/TestExtensions.kt`

```kotlin
package com.fivucsas.shared.test

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*

/**
 * Test extensions for coroutines
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun runTest(block: suspend TestScope.() -> Unit) {
    return kotlinx.coroutines.test.runTest {
        block()
    }
}

/**
 * Replace main dispatcher with test dispatcher
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule : TestWatcher() {
    private val testDispatcher = StandardTestDispatcher()
    
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }
    
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
```

---

## 🧪 Step 3: Write Use Case Tests (20 minutes)

### 3.1 Test GetUsersUseCase

**File:** `shared/src/commonTest/kotlin/com/fivucsas/shared/domain/usecase/admin/GetUsersUseCaseTest.kt`

```kotlin
package com.fivucsas.shared.domain.usecase.admin

import com.fivucsas.shared.domain.repository.UserRepository
import com.fivucsas.shared.test.TestData
import com.fivucsas.shared.test.runTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetUsersUseCaseTest {
    
    private val mockRepository = mockk<UserRepository>()
    private val useCase = GetUsersUseCase(mockRepository)
    
    @Test
    fun `invoke should return list of users from repository`() = runTest {
        // Given
        val expectedUsers = listOf(TestData.testUser1, TestData.testUser2)
        coEvery { mockRepository.getUsers() } returns expectedUsers
        
        // When
        val result = useCase()
        
        // Then
        assertEquals(expectedUsers, result)
        assertEquals(2, result.size)
        coVerify(exactly = 1) { mockRepository.getUsers() }
    }
    
    @Test
    fun `invoke should return empty list when no users exist`() = runTest {
        // Given
        coEvery { mockRepository.getUsers() } returns emptyList()
        
        // When
        val result = useCase()
        
        // Then
        assertTrue(result.isEmpty())
        coVerify(exactly = 1) { mockRepository.getUsers() }
    }
    
    @Test
    fun `invoke should propagate repository errors`() = runTest {
        // Given
        val exception = RuntimeException("Database error")
        coEvery { mockRepository.getUsers() } throws exception
        
        // When/Then
        try {
            useCase()
            throw AssertionError("Expected exception was not thrown")
        } catch (e: RuntimeException) {
            assertEquals("Database error", e.message)
        }
    }
}
```

### 3.2 Test SearchUsersUseCase

**File:** `shared/src/commonTest/kotlin/com/fivucsas/shared/domain/usecase/admin/SearchUsersUseCaseTest.kt`

```kotlin
package com.fivucsas.shared.domain.usecase.admin

import com.fivucsas.shared.domain.repository.UserRepository
import com.fivucsas.shared.test.TestData
import com.fivucsas.shared.test.runTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchUsersUseCaseTest {
    
    private val mockRepository = mockk<UserRepository>()
    private val useCase = SearchUsersUseCase(mockRepository)
    
    @Test
    fun `invoke should return matching users`() = runTest {
        // Given
        val query = "John"
        val expectedUsers = listOf(TestData.testUser1)
        coEvery { mockRepository.searchUsers(query) } returns expectedUsers
        
        // When
        val result = useCase(query)
        
        // Then
        assertEquals(1, result.size)
        assertEquals("John", result.first().firstName)
        coVerify(exactly = 1) { mockRepository.searchUsers(query) }
    }
    
    @Test
    fun `invoke should return empty list for no matches`() = runTest {
        // Given
        val query = "NonExistent"
        coEvery { mockRepository.searchUsers(query) } returns emptyList()
        
        // When
        val result = useCase(query)
        
        // Then
        assertTrue(result.isEmpty())
    }
    
    @Test
    fun `invoke should handle empty query`() = runTest {
        // Given
        val query = ""
        coEvery { mockRepository.searchUsers(query) } returns emptyList()
        
        // When
        val result = useCase(query)
        
        // Then
        assertTrue(result.isEmpty())
    }
}
```

---

## 🔄 Step 4: Write Repository Tests (15 minutes)

### 4.1 Test UserRepositoryImpl

**File:** `shared/src/commonTest/kotlin/com/fivucsas/shared/data/repository/UserRepositoryImplTest.kt`

```kotlin
package com.fivucsas.shared.data.repository

import com.fivucsas.shared.data.remote.api.IdentityApi
import com.fivucsas.shared.data.remote.dto.UserDto
import com.fivucsas.shared.domain.model.UserStatus
import com.fivucsas.shared.test.TestData
import com.fivucsas.shared.test.runTest
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class UserRepositoryImplTest {
    
    private val mockApi = mockk<IdentityApi>()
    private val repository = UserRepositoryImpl(mockApi)
    
    @Test
    fun `getUsers should return mock data when API disabled`() = runTest {
        // When
        val result = repository.getUsers()
        
        // Then
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
    }
    
    @Test
    fun `getUserById should return user when found`() = runTest {
        // When
        val result = repository.getUserById("user-1")
        
        // Then
        assertNotNull(result)
        assertEquals("user-1", result?.id)
    }
    
    @Test
    fun `getUserById should return null when not found`() = runTest {
        // When
        val result = repository.getUserById("non-existent")
        
        // Then
        assertEquals(null, result)
    }
    
    @Test
    fun `searchUsers should filter by query`() = runTest {
        // When
        val result = repository.searchUsers("John")
        
        // Then
        assertTrue(result.all { 
            it.firstName.contains("John", ignoreCase = true) ||
            it.lastName.contains("John", ignoreCase = true) ||
            it.email.contains("John", ignoreCase = true)
        })
    }
}
```

---

## 🎨 Step 5: Write ViewModel Tests (15 minutes)

### 5.1 Test AdminViewModel

**File:** `shared/src/commonTest/kotlin/com/fivucsas/shared/presentation/viewmodel/AdminViewModelTest.kt`

```kotlin
package com.fivucsas.shared.presentation.viewmodel

import app.cash.turbine.test
import com.fivucsas.shared.domain.model.Statistics
import com.fivucsas.shared.domain.usecase.admin.*
import com.fivucsas.shared.presentation.state.AdminTab
import com.fivucsas.shared.test.TestData
import com.fivucsas.shared.test.runTest
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class AdminViewModelTest {
    
    private val mockGetUsersUseCase = mockk<GetUsersUseCase>()
    private val mockSearchUsersUseCase = mockk<SearchUsersUseCase>()
    private val mockGetStatisticsUseCase = mockk<GetStatisticsUseCase>()
    private val mockUpdateUserUseCase = mockk<UpdateUserUseCase>()
    private val mockDeleteUserUseCase = mockk<DeleteUserUseCase>()
    
    private fun createViewModel() = AdminViewModel(
        getUsersUseCase = mockGetUsersUseCase,
        searchUsersUseCase = mockSearchUsersUseCase,
        getStatisticsUseCase = mockGetStatisticsUseCase,
        updateUserUseCase = mockUpdateUserUseCase,
        deleteUserUseCase = mockDeleteUserUseCase
    )
    
    @Test
    fun `initial state should be correct`() = runTest {
        // Given/When
        val viewModel = createViewModel()
        
        // Then
        viewModel.uiState.test(timeout = 1.seconds) {
            val state = awaitItem()
            assertEquals(AdminTab.USERS, state.selectedTab)
            assertTrue(state.users.isEmpty())
            assertEquals("", state.searchQuery)
            expectNoEvents()
        }
    }
    
    @Test
    fun `loadUsers should update state with users`() = runTest {
        // Given
        val expectedUsers = TestData.createTestUsers(5)
        coEvery { mockGetUsersUseCase() } returns expectedUsers
        val viewModel = createViewModel()
        
        // When
        viewModel.loadUsers()
        
        // Then
        viewModel.uiState.test(timeout = 1.seconds) {
            val state = awaitItem()
            assertEquals(5, state.users.size)
            expectNoEvents()
        }
    }
    
    @Test
    fun `selectTab should update selected tab`() = runTest {
        // Given
        val viewModel = createViewModel()
        
        // When
        viewModel.selectTab(AdminTab.ANALYTICS)
        
        // Then
        viewModel.uiState.test(timeout = 1.seconds) {
            val state = awaitItem()
            assertEquals(AdminTab.ANALYTICS, state.selectedTab)
            expectNoEvents()
        }
    }
    
    @Test
    fun `updateSearchQuery should update query in state`() = runTest {
        // Given
        val viewModel = createViewModel()
        val query = "John"
        
        // When
        viewModel.updateSearchQuery(query)
        
        // Then
        viewModel.uiState.test(timeout = 1.seconds) {
            val state = awaitItem()
            assertEquals(query, state.searchQuery)
            expectNoEvents()
        }
    }
}
```

---

## 📊 Step 6: Run Tests (10 minutes)

### 6.1 Run All Tests

```bash
cd mobile-app

# Run all tests
.\gradlew.bat test

# Run only common tests
.\gradlew.bat :shared:testDebugUnitTest

# Run with coverage
.\gradlew.bat :shared:testDebugUnitTestCoverage
```

### 6.2 View Test Reports

Tests results will be in:
```
shared/build/reports/tests/testDebugUnitTest/index.html
```

---

## 🎯 Success Criteria

- [ ] Testing dependencies added
- [ ] Test utilities created
- [ ] Use case tests written and passing
- [ ] Repository tests written and passing
- [ ] ViewModel tests written and passing
- [ ] All tests pass
- [ ] Test coverage > 70%
- [ ] No flaky tests

---

## 📊 Progress Tracking

```
Day 1: Shared Module Structure    ✅ (10%)
Day 2: Data Layer                  ✅ (20%)
Day 3: Use Cases & Validation      ✅ (30%)
Day 4: ViewModels to Shared        ✅ (50%)
Day 5: Dependency Injection        ✅ (60%)
Day 6: API Integration             ✅ (70%)
Day 7: Testing Infrastructure      ⏳ (80%) ⭐ IN PROGRESS!
----------------------------------------------
Day 8: Error Handling              ⬜ (90%)
Day 9: Performance & Polish        ⬜ (95%)
Day 10: Final Integration          ⬜ (100%)
```

---

## 🐛 Troubleshooting

### Issue 1: "Could not find MockK"
**Solution:** Ensure version catalog or direct dependency is correct

### Issue 2: "Turbine timeout"
**Solution:** Increase timeout or use `expectNoEvents()`

### Issue 3: "Coroutine test fails"
**Solution:** Use `runTest` wrapper and `StandardTestDispatcher`

---

**Ready to implement? Let's write those tests! 🧪**
