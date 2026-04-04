package com.fivucsas.shared.test.mocks

import com.fivucsas.shared.domain.model.*
import com.fivucsas.shared.domain.repository.*

/**
 * Fake repository implementations for testing ViewModels.
 * Each fake supports configurable success/failure and mock data.
 */

// ── EnrollmentRepository ────────────────────────────────────────────────────

class FakeEnrollmentRepository : EnrollmentRepository {
    var shouldSucceed = true
    var errorMessage = "Test error"
    var mockEnrollments = listOf(
        Enrollment(
            id = "enr-1",
            userId = "user-1",
            method = "FACE",
            status = EnrollmentStatus.ENROLLED,
            enrolledAt = "2025-01-01"
        ),
        Enrollment(
            id = "enr-2",
            userId = "user-1",
            method = "VOICE",
            status = EnrollmentStatus.PENDING,
            enrolledAt = "2025-01-02"
        )
    )

    override suspend fun getEnrollments(userId: String): Result<List<Enrollment>> {
        return if (shouldSucceed) Result.success(mockEnrollments)
        else Result.failure(RuntimeException(errorMessage))
    }
}

// ── VerificationRepository ──────────────────────────────────────────────────

class FakeVerificationRepository : VerificationRepository {
    var shouldSucceed = true
    var errorMessage = "Test error"
    var mockFlows = listOf(
        VerificationFlow(id = "flow-1", name = "Banking KYC", flowType = "KYC", status = "active"),
        VerificationFlow(id = "flow-2", name = "Education", flowType = "EDUCATION", status = "active")
    )
    var mockSessions = listOf(
        VerificationSession(id = "sess-1", userId = "user-1", flowId = "flow-1", flowName = "Banking KYC", status = "pending"),
        VerificationSession(id = "sess-2", userId = "user-2", flowId = "flow-2", flowName = "Education", status = "completed")
    )

    override suspend fun getFlows(): Result<List<VerificationFlow>> {
        return if (shouldSucceed) Result.success(mockFlows)
        else Result.failure(RuntimeException(errorMessage))
    }

    override suspend fun getSessions(status: String?): Result<List<VerificationSession>> {
        return if (shouldSucceed) {
            val filtered = if (status != null) mockSessions.filter { it.status == status } else mockSessions
            Result.success(filtered)
        } else Result.failure(RuntimeException(errorMessage))
    }

    override suspend fun getSession(sessionId: String): Result<VerificationSession> {
        return if (shouldSucceed) {
            mockSessions.find { it.id == sessionId }?.let { Result.success(it) }
                ?: Result.failure(NoSuchElementException("Session not found"))
        } else Result.failure(RuntimeException(errorMessage))
    }

    override suspend fun startSession(flowId: String, userId: String): Result<VerificationSession> {
        return if (shouldSucceed) {
            val session = VerificationSession(id = "sess-new", userId = userId, flowId = flowId, flowName = "Test Flow", status = "pending")
            Result.success(session)
        } else Result.failure(RuntimeException(errorMessage))
    }
}

// ── SessionRepository ───────────────────────────────────────────────────────

class FakeSessionRepository : SessionRepository {
    var shouldSucceed = true
    var errorMessage = "Test error"
    var revokedSessionId: String? = null
    var mockSessions = listOf(
        AuthSession(id = "sess-1", userId = "user-1", deviceInfo = "Chrome/Desktop", status = "ACTIVE"),
        AuthSession(id = "sess-2", userId = "user-1", deviceInfo = "Mobile/Android", status = "ACTIVE")
    )

    override suspend fun getSessions(): Result<List<AuthSession>> {
        return if (shouldSucceed) Result.success(mockSessions)
        else Result.failure(RuntimeException(errorMessage))
    }

    override suspend fun revokeSession(sessionId: String): Result<Unit> {
        revokedSessionId = sessionId
        return if (shouldSucceed) Result.success(Unit)
        else Result.failure(RuntimeException(errorMessage))
    }
}

// ── DeviceRepository ────────────────────────────────────────────────────────

class FakeDeviceRepository : DeviceRepository {
    var shouldSucceed = true
    var errorMessage = "Test error"
    var removedDeviceId: String? = null
    var mockDevices = listOf(
        Device(id = "dev-1", userId = "user-1", deviceName = "Pixel 7", platform = "Android"),
        Device(id = "dev-2", userId = "user-1", deviceName = "MacBook Pro", platform = "Desktop")
    )
    var mockCredentials = listOf(
        WebAuthnCredential(id = "cred-1", credentialId = "cred-id-1", publicKey = "pk-1")
    )

    override suspend fun getDevices(userId: String): Result<List<Device>> {
        return if (shouldSucceed) Result.success(mockDevices)
        else Result.failure(RuntimeException(errorMessage))
    }

    override suspend fun removeDevice(deviceId: String): Result<Unit> {
        removedDeviceId = deviceId
        return if (shouldSucceed) Result.success(Unit)
        else Result.failure(RuntimeException(errorMessage))
    }

    override suspend fun getWebAuthnCredentials(userId: String): Result<List<WebAuthnCredential>> {
        return if (shouldSucceed) Result.success(mockCredentials)
        else Result.failure(RuntimeException(errorMessage))
    }
}

// ── OtpRepository ───────────────────────────────────────────────────────────

class FakeOtpRepository : OtpRepository {
    var shouldSucceed = true
    var errorMessage = "Test error"
    var mockResult = OtpResult(success = true, message = "OTP sent")

    override suspend fun sendEmailOtp(userId: String, email: String): Result<OtpResult> {
        return if (shouldSucceed) Result.success(mockResult)
        else Result.failure(RuntimeException(errorMessage))
    }

    override suspend fun verifyEmailOtp(userId: String, code: String): Result<OtpResult> {
        return if (shouldSucceed) Result.success(OtpResult(success = true, message = "Verified"))
        else Result.failure(RuntimeException(errorMessage))
    }

    override suspend fun sendSmsOtp(userId: String, phoneNumber: String): Result<OtpResult> {
        return if (shouldSucceed) Result.success(mockResult)
        else Result.failure(RuntimeException(errorMessage))
    }

    override suspend fun verifySmsOtp(userId: String, code: String): Result<OtpResult> {
        return if (shouldSucceed) Result.success(OtpResult(success = true, message = "Verified"))
        else Result.failure(RuntimeException(errorMessage))
    }
}

// ── TotpRepository ──────────────────────────────────────────────────────────

class FakeTotpRepository : TotpRepository {
    var shouldSucceed = true
    var errorMessage = "Test error"
    var mockEnabled = false

    override suspend fun setup(userId: String): Result<TotpSetupResult> {
        return if (shouldSucceed) Result.success(
            TotpSetupResult(success = true, secret = "JBSWY3DPEHPK3PXP", otpAuthUri = "otpauth://totp/FIVUCSAS?secret=JBSWY3DPEHPK3PXP", message = "Setup initiated")
        ) else Result.failure(RuntimeException(errorMessage))
    }

    override suspend fun verifySetup(userId: String, code: String): Result<TotpVerifyResult> {
        return if (shouldSucceed) Result.success(
            TotpVerifyResult(success = true, message = "TOTP verified and enabled")
        ) else Result.failure(RuntimeException(errorMessage))
    }

    override suspend fun getStatus(userId: String): Result<TotpStatusResult> {
        return if (shouldSucceed) Result.success(
            TotpStatusResult(enabled = mockEnabled, message = "")
        ) else Result.failure(RuntimeException(errorMessage))
    }
}

// ── RolesRepository ─────────────────────────────────────────────────────────

class FakeRolesRepository : RolesRepository {
    var shouldSucceed = true
    var errorMessage = "Test error"
    var mockRoles = listOf(
        Role(id = "role-1", name = "ADMIN", description = "Administrator", permissions = listOf(
            PermissionItem(id = "perm-1", name = "users:read", description = "Read users")
        )),
        Role(id = "role-2", name = "USER", description = "Regular user")
    )
    var mockPermissions = listOf(
        PermissionItem(id = "perm-1", name = "users:read", description = "Read users"),
        PermissionItem(id = "perm-2", name = "users:write", description = "Write users")
    )
    var deletedRoleId: String? = null
    var createdRole: Role? = null

    override suspend fun getRoles(): Result<List<Role>> {
        return if (shouldSucceed) Result.success(mockRoles)
        else Result.failure(RuntimeException(errorMessage))
    }

    override suspend fun createRole(name: String, description: String?): Result<Role> {
        val role = Role(id = "role-new", name = name, description = description ?: "")
        createdRole = role
        return if (shouldSucceed) Result.success(role)
        else Result.failure(RuntimeException(errorMessage))
    }

    override suspend fun updateRole(id: String, name: String?, description: String?): Result<Role> {
        return if (shouldSucceed) {
            val existing = mockRoles.find { it.id == id } ?: return Result.failure(NoSuchElementException())
            Result.success(existing.copy(name = name ?: existing.name, description = description ?: existing.description))
        } else Result.failure(RuntimeException(errorMessage))
    }

    override suspend fun deleteRole(id: String): Result<Unit> {
        deletedRoleId = id
        return if (shouldSucceed) Result.success(Unit)
        else Result.failure(RuntimeException(errorMessage))
    }

    override suspend fun getPermissions(): Result<List<PermissionItem>> {
        return if (shouldSucceed) Result.success(mockPermissions)
        else Result.failure(RuntimeException(errorMessage))
    }

    override suspend fun updateRolePermissions(roleId: String, permissionNames: List<String>): Result<Unit> {
        return if (shouldSucceed) Result.success(Unit)
        else Result.failure(RuntimeException(errorMessage))
    }
}

// ── AuditLogRepository ──────────────────────────────────────────────────────

class FakeAuditLogRepository : AuditLogRepository {
    var shouldSucceed = true
    var errorMessage = "Test error"
    var mockLogs = (1..25).map { i ->
        AuditLog(id = "log-$i", userId = "user-1", action = "LOGIN", status = "SUCCESS", timestamp = "2025-01-0${i % 9 + 1}")
    }

    override suspend fun getAuditLogs(action: String?, userId: String?, page: Int, size: Int): Result<List<AuditLog>> {
        return if (shouldSucceed) {
            val filtered = mockLogs
                .let { logs -> if (action != null) logs.filter { it.action == action } else logs }
                .let { logs -> if (userId != null) logs.filter { it.userId == userId } else logs }
            val paged = filtered.drop(page * size).take(size)
            Result.success(paged)
        } else Result.failure(RuntimeException(errorMessage))
    }
}

// ── OAuth2ClientRepository ──────────────────────────────────────────────────

class FakeOAuth2ClientRepository : OAuth2ClientRepository {
    var shouldSucceed = true
    var errorMessage = "Test error"
    var deletedClientId: String? = null
    var mockClients = listOf(
        OAuth2Client(id = "app-1", appName = "Test App", clientId = "client-id-1", redirectUris = listOf("http://localhost:3000/callback"), scopes = listOf("openid", "profile"), status = "active", createdAt = "2025-01-01")
    )

    override suspend fun listClients(): Result<List<OAuth2Client>> {
        return if (shouldSucceed) Result.success(mockClients)
        else Result.failure(RuntimeException(errorMessage))
    }

    override suspend fun registerClient(appName: String, redirectUris: String, scopes: List<String>): Result<OAuth2Client> {
        val newClient = OAuth2Client(id = "app-new", appName = appName, clientId = "new-client-id", clientSecret = "new-secret", redirectUris = listOf(redirectUris), scopes = scopes, status = "active", createdAt = "2025-01-01")
        return if (shouldSucceed) Result.success(newClient)
        else Result.failure(RuntimeException(errorMessage))
    }

    override suspend fun deleteClient(id: String): Result<Unit> {
        deletedClientId = id
        return if (shouldSucceed) Result.success(Unit)
        else Result.failure(RuntimeException(errorMessage))
    }
}

// ── DashboardRepository ─────────────────────────────────────────────────────

class FakeDashboardRepository : DashboardRepository {
    var shouldSucceed = true
    var errorMessage = "Test error"
    var mockStatistics = Statistics(
        totalUsers = 100,
        activeUsers = 85,
        verificationsToday = 42,
        successRate = 95.5,
        failedAttempts = 3
    )

    override suspend fun getStatistics(): Result<Statistics> {
        return if (shouldSucceed) Result.success(mockStatistics)
        else Result.failure(RuntimeException(errorMessage))
    }
}

// ── FingerprintRepository ───────────────────────────────────────────────────

class FakeFingerprintRepository : FingerprintRepository {
    var shouldSucceed = true
    var errorMessage = "Fingerprint verification failed"
    var mockToken = "step-up-token-123"

    override suspend fun performStepUp(onStep: (FingerprintStep) -> Unit): Result<String> {
        onStep(FingerprintStep.RequestingChallenge)
        onStep(FingerprintStep.ScanningBiometric)
        onStep(FingerprintStep.VerifyingSignature)
        return if (shouldSucceed) Result.success(mockToken)
        else Result.failure(RuntimeException(errorMessage))
    }
}

// ── TenantSettingsRepository ────────────────────────────────────────────────

class FakeTenantSettingsRepository : TenantSettingsRepository {
    var shouldSucceed = true
    var errorMessage = "Test error"
    var mockSettings = TenantSettings(
        tenantName = "Test Tenant",
        livenessCheckEnabled = true,
        confidenceThreshold = 0.85f,
        maxEnrollmentAttempts = 3,
        sessionTimeoutMinutes = 30,
        autoLockEnabled = false,
        nfcExamEntryEnabled = false,
        inviteExpiryDays = 7
    )

    override suspend fun getSettings(): Result<TenantSettings> {
        return if (shouldSucceed) Result.success(mockSettings)
        else Result.failure(RuntimeException(errorMessage))
    }

    override suspend fun updateSettings(settings: TenantSettings): Result<TenantSettings> {
        mockSettings = settings
        return if (shouldSucceed) Result.success(settings)
        else Result.failure(RuntimeException(errorMessage))
    }
}

// ── InviteRepository ────────────────────────────────────────────────────────

class FakeInviteRepository : InviteRepository {
    var shouldSucceed = true
    var errorMessage = "Test error"
    var mockInvites = listOf(
        Invite(id = "inv-1", email = "user@test.com", role = "USER", tenantId = "t-1", tenantName = "Tenant A", status = InviteStatus.PENDING, createdAt = "2025-01-01", expiresAt = "2025-02-01"),
        Invite(id = "inv-2", email = "admin@test.com", role = "ADMIN", tenantId = "t-1", tenantName = "Tenant A", status = InviteStatus.ACCEPTED, createdAt = "2025-01-01", expiresAt = "2025-02-01")
    )
    var createdInvite: Invite? = null
    var revokedInviteId: String? = null

    override suspend fun getInvites(): Result<List<Invite>> {
        return if (shouldSucceed) Result.success(mockInvites)
        else Result.failure(RuntimeException(errorMessage))
    }

    override suspend fun createInvite(email: String, role: String, tenantId: String?): Result<Invite> {
        val invite = Invite(id = "inv-new", email = email, role = role, tenantId = tenantId, status = InviteStatus.PENDING, createdAt = "2025-01-01", expiresAt = "2025-02-01")
        createdInvite = invite
        return if (shouldSucceed) Result.success(invite)
        else Result.failure(RuntimeException(errorMessage))
    }

    override suspend fun revokeInvite(inviteId: String): Result<Invite> {
        revokedInviteId = inviteId
        val invite = mockInvites.find { it.id == inviteId } ?: Invite(id = inviteId, email = "test@test.com", role = "USER", status = InviteStatus.REVOKED, createdAt = "2025-01-01", expiresAt = "2025-02-01")
        return if (shouldSucceed) Result.success(invite.copy(status = InviteStatus.REVOKED))
        else Result.failure(RuntimeException(errorMessage))
    }

    override suspend fun resendInvite(inviteId: String): Result<Invite> {
        return if (shouldSucceed) {
            val invite = mockInvites.find { it.id == inviteId } ?: return Result.failure(NoSuchElementException())
            Result.success(invite)
        } else Result.failure(RuntimeException(errorMessage))
    }

    override suspend fun getReceivedInvites(): Result<List<ReceivedInvite>> {
        return Result.success(emptyList())
    }

    override suspend fun acceptInvite(inviteId: String): Result<ReceivedInvite> {
        return Result.success(ReceivedInvite(id = inviteId, tenantName = "Test", invitedBy = "admin", role = "USER", receivedAt = "2025-01-01", expiresAt = "2025-02-01", status = ReceivedInviteStatus.ACCEPTED))
    }

    override suspend fun declineInvite(inviteId: String): Result<ReceivedInvite> {
        return Result.success(ReceivedInvite(id = inviteId, tenantName = "Test", invitedBy = "admin", role = "USER", receivedAt = "2025-01-01", expiresAt = "2025-02-01", status = ReceivedInviteStatus.DECLINED))
    }
}

// ── QrLoginRepository ───────────────────────────────────────────────────────

class FakeQrLoginRepository : QrLoginRepository {
    var shouldSucceed = true
    var errorMessage = "Test error"
    var mockSession = QrLoginSession(
        sessionId = "qr-sess-1",
        qrContent = "fivucsas://qr-login?session=qr-sess-1",
        status = QrLoginSessionStatus.PENDING_SCAN
    )

    override suspend fun createSession(platform: String): Result<QrLoginSession> {
        return if (shouldSucceed) Result.success(mockSession)
        else Result.failure(RuntimeException(errorMessage))
    }

    override suspend fun getSession(sessionId: String): Result<QrLoginSession> {
        return if (shouldSucceed) Result.success(mockSession)
        else Result.failure(RuntimeException(errorMessage))
    }

    override suspend fun approveSession(sessionId: String, approverPlatform: String): Result<Unit> {
        return if (shouldSucceed) Result.success(Unit)
        else Result.failure(RuntimeException(errorMessage))
    }
}
