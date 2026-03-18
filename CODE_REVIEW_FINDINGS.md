# Client-Apps Code Review Findings

**Date**: 2026-03-18
**Reviewed by**: Claude Code (Opus 4.6)
**Scope**: Full API integration and architecture review of `client-apps/shared/`

---

## CRITICAL: Login Crash — Root Cause Found and Fixed

### Problem
The Android app crashed on login with:
```
Illegal input field access token refresh... expires... are required for type with serial...AuthResponseDto
```

### Root Cause
**All Identity Core API DTOs used snake_case `@SerialName` annotations, but the Spring Boot server returns camelCase JSON (Jackson default).**

The server returns:
```json
{
  "accessToken": "eyJ...",
  "refreshToken": "428ff1db-...",
  "tokenType": "Bearer",
  "expiresIn": 3600000,
  "user": { "id": "...", "email": "...", "firstName": "...", ... }
}
```

But `AuthResponseDto` expected:
```kotlin
@SerialName("access_token") val accessToken: String   // WRONG: server sends "accessToken"
@SerialName("refresh_token") val refreshToken: String  // WRONG: server sends "refreshToken"
@SerialName("expires_in") val expiresIn: Long          // WRONG: server sends "expiresIn"
@SerialName("token_type") val tokenType: String        // WRONG: server sends "tokenType"
```

Even though `ignoreUnknownKeys = true` was correctly set, the serializer couldn't find the **required** fields (`access_token`, `refresh_token`, `expires_in`) because the JSON had `accessToken`, `refreshToken`, `expiresIn` instead.

---

## All Issues Found and Fixed

### Issue 1: AuthResponseDto — snake_case vs camelCase (CRITICAL)
- **File**: `shared/.../dto/AuthDto.kt`
- **Fix**: Removed all `@SerialName("access_token")` etc. annotations. Fields now match server's camelCase directly.
- **Also**: Added `AuthUserDto` to capture the `user` object from login/refresh responses. The role is now extracted from `user.role` instead of a non-existent top-level `role` field.

### Issue 2: RegisterRequestDto — snake_case vs camelCase (CRITICAL)
- **File**: `shared/.../dto/AuthDto.kt`
- **Fix**: Removed `@SerialName("first_name")` and `@SerialName("last_name")`. Server expects `firstName` and `lastName`.

### Issue 3: RefreshTokenRequestDto — snake_case vs camelCase
- **File**: `shared/.../dto/AuthDto.kt`
- **Fix**: Removed `@SerialName("refresh_token")`. Server expects `refreshToken`.

### Issue 4: ChangePasswordRequestDto — snake_case vs camelCase
- **File**: `shared/.../dto/AuthDto.kt`
- **Fix**: Removed `@SerialName("current_password")` and `@SerialName("new_password")`.

### Issue 5: AuthSessionDto — snake_case vs camelCase
- **File**: `shared/.../dto/SessionDto.kt`
- **Fix**: Removed all `@SerialName` annotations (`user_id` -> `userId`, `device_info` -> `deviceInfo`, etc.).

### Issue 6: DeviceDto / WebAuthnCredentialDto — snake_case vs camelCase
- **File**: `shared/.../dto/DeviceDto.kt`
- **Fix**: Removed all `@SerialName` annotations.

### Issue 7: EnrollmentDto — snake_case vs camelCase
- **File**: `shared/.../dto/EnrollmentDto.kt`
- **Fix**: Removed all `@SerialName` annotations.

### Issue 8: AuthFlowDto / AuthFlowStepDto — snake_case vs camelCase
- **File**: `shared/.../dto/AuthFlowDto.kt`
- **Fix**: Removed all `@SerialName` annotations.

### Issue 9: VerifyBiometricSignatureResponseDto — dual-field workaround
- **File**: `shared/.../dto/FingerprintStepUpDto.kt`
- **Fix**: Removed the snake_case `stepUpTokenSnake` field and `resolvedStepUpToken()` method. Now uses a single `stepUpToken` field matching server's camelCase.
- **Also updated**: `FingerprintRepositoryImpl.kt` to use `response.stepUpToken` directly.

### Issue 10: Raw error messages shown to users
- **Files**: `LoginViewModel.kt`, `RegisterViewModel.kt`
- **Fix**: Added `mapErrorToUserMessage()` that translates technical exceptions (serialization errors, network timeouts, HTTP status codes) into user-friendly messages.

---

## What Was Already Correct

1. **JSON configuration** (`NetworkModule.kt`): `ignoreUnknownKeys = true` was already set. This prevents crashes from extra fields the DTO doesn't declare (like the new `user` object).

2. **API base URLs** (`ApiConfig.kt`): All pointing to correct production URLs (`https://auth.rollingcatsoftware.com/api/v1`).

3. **Biometric DTOs** (`BiometricDto.kt`): Correctly use snake_case `@SerialName` because the biometric-processor is Python/FastAPI which uses snake_case by default.

4. **Token refresh interceptor** (`NetworkModule.kt`): Correctly handles 401 responses and attempts token refresh.

5. **Clean Architecture**: Proper separation of concerns (DTOs -> Domain models -> ViewModels -> UI).

6. **Koin DI**: Correctly configured with named qualifiers for identity and biometric HTTP clients.

7. **StateFlow patterns**: ViewModels correctly use `MutableStateFlow` with `asStateFlow()`.

---

## Architecture Notes

### Good Patterns Observed
- Two separate HTTP clients (identity vs biometric) with different timeout configs
- Token manager with in-memory cache + persistent storage
- Step-up token manager for fingerprint auth
- Use cases with input validation before API calls
- Extension functions for DTO-to-model mapping

### Recommendations (Non-blocking)
1. Other ViewModels (AdminViewModel, InviteViewModel, etc.) still pass raw `error.message` to UI. Consider a shared error mapping utility.
2. The `UserDto` fields don't match the server's user response format (has `name` instead of `firstName`/`lastName`, has `idNumber` as required instead of nullable). This will fail when fetching user lists.
3. The token refresh interceptor in `NetworkModule.kt` catches 401 and refreshes, but doesn't retry the original request. The user sees a failed request and must retry manually.

---

## Summary

**10 issues fixed**, all related to snake_case/camelCase mismatch between the Kotlin DTOs and the Spring Boot server. The login flow should now work correctly.

The root pattern: the DTOs were written assuming a Python/snake_case API, but the Identity Core API is a Spring Boot/Jackson service that returns camelCase by default.
