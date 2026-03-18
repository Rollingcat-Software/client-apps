package com.fivucsas.shared.i18n

/**
 * i18n String Resource System
 *
 * Provides Turkish and English translations for all user-facing strings.
 * Uses a simple map-based approach compatible with Kotlin Multiplatform.
 */
object StringResources {

    enum class Language(val code: String, val displayName: String) {
        EN("en", "English"),
        TR("tr", "Turkce")
    }

    private var _currentLanguage: Language = Language.EN

    val currentLanguage: Language get() = _currentLanguage

    fun setLanguage(language: Language) {
        _currentLanguage = language
    }

    fun get(key: StringKey): String {
        val map = when (_currentLanguage) {
            Language.EN -> enStrings
            Language.TR -> trStrings
        }
        return map[key] ?: enStrings[key] ?: key.name
    }

    fun get(key: StringKey, vararg args: Any): String {
        var result = get(key)
        args.forEachIndexed { index, arg ->
            result = result.replace("{$index}", arg.toString())
        }
        return result
    }
}

enum class StringKey {
    // App
    APP_NAME,
    APP_SUBTITLE,

    // Auth
    LOGIN,
    REGISTER,
    LOGOUT,
    EMAIL,
    PASSWORD,
    CONFIRM_PASSWORD,
    FIRST_NAME,
    LAST_NAME,
    FORGOT_PASSWORD,
    DONT_HAVE_ACCOUNT,
    ALREADY_HAVE_ACCOUNT,
    GUEST_FACE_CHECK,
    DEV_ROLE,
    LOGIN_SUCCESS,
    REGISTER_SUCCESS,
    BIOMETRIC_AUTH,

    // Navigation
    NAV_DASHBOARD,
    NAV_USERS,
    NAV_ANALYTICS,
    NAV_SECURITY,
    NAV_SETTINGS,
    NAV_PROFILE,
    NAV_NOTIFICATIONS,
    NAV_HELP,
    NAV_ABOUT,
    NAV_AUTH_FLOWS,
    NAV_SESSIONS,
    NAV_DEVICES,
    NAV_ENROLLMENTS,

    // Dashboard
    DASHBOARD_TITLE,
    DASHBOARD_WELCOME,
    TOTAL_USERS,
    ACTIVE_USERS,
    ENROLLMENTS,
    VERIFICATIONS,

    // Users
    USERS_TITLE,
    USERS_SUBTITLE,
    ADD_USER,
    EDIT_USER,
    DELETE_USER,
    SEARCH_USERS,
    USER_NAME,
    USER_EMAIL,
    USER_STATUS,
    USER_ROLE,
    USER_ACTIONS,
    CONFIRM_DELETE_USER,
    USER_ADDED,
    USER_UPDATED,
    USER_DELETED,

    // Auth Flows
    AUTH_FLOWS_TITLE,
    AUTH_FLOWS_SUBTITLE,
    FLOW_NAME,
    FLOW_OPERATION_TYPE,
    FLOW_STEPS,
    FLOW_ACTIVE,
    NO_AUTH_FLOWS,

    // Sessions
    SESSIONS_TITLE,
    SESSIONS_SUBTITLE,
    SESSION_DEVICE,
    SESSION_IP,
    SESSION_LAST_ACTIVE,
    SESSION_CREATED,
    SESSION_STATUS,
    REVOKE_SESSION,
    CONFIRM_REVOKE_SESSION,
    SESSION_REVOKED,
    NO_ACTIVE_SESSIONS,

    // Devices
    DEVICES_TITLE,
    DEVICES_SUBTITLE,
    DEVICE_NAME,
    DEVICE_PLATFORM,
    DEVICE_REGISTERED,
    DEVICE_LAST_USED,
    REMOVE_DEVICE,
    CONFIRM_REMOVE_DEVICE,
    DEVICE_REMOVED,
    NO_DEVICES,
    WEBAUTHN_CREDENTIALS,

    // Enrollments
    ENROLLMENTS_TITLE,
    ENROLLMENTS_SUBTITLE,
    ENROLLMENT_METHOD,
    ENROLLMENT_STATUS,
    ENROLLMENT_DATE,
    START_ENROLLMENT,
    NO_ENROLLMENTS,
    ENROLLED,
    NOT_ENROLLED,

    // Settings
    SETTINGS_TITLE,
    SETTINGS_SUBTITLE,
    LANGUAGE,
    THEME,
    THEME_LIGHT,
    THEME_DARK,
    THEME_SYSTEM,
    NOTIFICATIONS_ENABLED,
    SAVE_SETTINGS,
    RESET_SETTINGS,
    SETTINGS_SAVED,

    // Common
    LOADING,
    ERROR,
    RETRY,
    CANCEL,
    CONFIRM,
    SAVE,
    DELETE,
    EDIT,
    BACK,
    CLOSE,
    SEARCH,
    REFRESH,
    NO_DATA,
    COMING_SOON,
    SUCCESS,
    FAILED,
    ACTIVE,
    INACTIVE,
    UNKNOWN,

    // Errors
    ERROR_NETWORK,
    ERROR_UNAUTHORIZED,
    ERROR_SERVER,
    ERROR_NOT_FOUND,
    ERROR_TIMEOUT,
    ERROR_UNKNOWN,
    ERROR_VALIDATION,
    ERROR_LOAD_FAILED,
    ERROR_EMPTY_FIELD,
    ERROR_INVALID_EMAIL,
    ERROR_PASSWORD_TOO_SHORT,
    ERROR_PASSWORDS_DONT_MATCH,

    // Security
    SECURITY_TITLE,
    SECURITY_SUBTITLE,
    ACTIVE_SESSIONS,
    REVOKE_ALL_SESSIONS,

    // Biometric
    FACE_RECOGNITION,
    FINGERPRINT,
    VOICE_RECOGNITION,
    NFC_DOCUMENT,
    TOTP,
    EMAIL_OTP,
    SMS_OTP,
    HARDWARE_KEY,

    // Tenant
    TENANT_MANAGEMENT,
    TENANT_NAME,
    TENANT_DOMAIN,
    CREATE_TENANT,

    // Profile
    PROFILE_TITLE,
    CHANGE_PASSWORD,
    CURRENT_PASSWORD,
    NEW_PASSWORD,

    // Voice
    VOICE_ENROLL,
    VOICE_VERIFY,
    VOICE_SEARCH,
    VOICE_ENROLL_INSTRUCTION,
    VOICE_VERIFY_INSTRUCTION,
    VOICE_SEARCH_INSTRUCTION,
    VOICE_PERMISSION_REQUIRED,
    VOICE_TAP_TO_RECORD,
    VOICE_RECORDING,
    VOICE_VERIFIED,
    VOICE_NOT_VERIFIED,
    VOICE_CONFIDENCE,
    VOICE_USER_FOUND,
    VOICE_USER_NOT_FOUND,

    // OTP
    OTP_SEND,
    OTP_VERIFY,
    OTP_RESEND,
    OTP_CODE,
    OTP_ENTER_CODE,
    OTP_VERIFIED_SUCCESS,

    // Email OTP
    EMAIL_OTP_TITLE,
    EMAIL_OTP_DESCRIPTION,

    // SMS OTP
    SMS_OTP_TITLE,
    SMS_OTP_DESCRIPTION,
    SMS_PHONE_NUMBER,
    SMS_TWILIO_INFO,

    // TOTP
    TOTP_TITLE,
    TOTP_DESCRIPTION,
    TOTP_SETUP,
    TOTP_SCAN_QR,
    TOTP_MANUAL_KEY,
    TOTP_ENTER_CODE,
    TOTP_ALREADY_ENABLED,
    TOTP_SETUP_COMPLETE,

    // Analytics
    ANALYTICS_TITLE,
    ANALYTICS_SUBTITLE,
    ANALYTICS_VERIFICATIONS_TODAY,
    ANALYTICS_FAILED_ATTEMPTS,
    ANALYTICS_SUCCESS_RATE,
    ANALYTICS_PENDING,

    // Liveness
    LIVENESS_TITLE,
    LIVENESS_SUBTITLE,
    LIVENESS_BLINK,
    LIVENESS_SMILE,
    LIVENESS_TURN_LEFT,
    LIVENESS_TURN_RIGHT,
    LIVENESS_NOD,
    LIVENESS_OPEN_MOUTH,
    LIVENESS_CLIENT_SCORE,
    LIVENESS_SERVER_SCORE,
    LIVENESS_VERIFIED,
    LIVENESS_FAILED,

    // Card Detection
    CARD_DETECTION_TITLE,
    CARD_DETECTION_SUBTITLE,
    CARD_DETECTION_CAPTURE,
    CARD_DETECTION_RESULT,
    CARD_TYPE,
    CARD_CONFIDENCE,

    // Hardware Token
    HARDWARE_TOKEN_TITLE,
    HARDWARE_TOKEN_SUBTITLE,
    HARDWARE_TOKEN_REGISTER,
    HARDWARE_TOKEN_VERIFY,
    HARDWARE_TOKEN_CREDENTIAL_ID,
    HARDWARE_TOKEN_TRANSPORTS,

    // Analytics Enhanced (P3)
    ANALYTICS_OVERVIEW,
    ANALYTICS_ENROLLMENTS_BY_METHOD,
    ANALYTICS_RECENT_ACTIVITY,
    ANALYTICS_LOGINS_TODAY,
    ANALYTICS_REGISTRATIONS_TODAY,
    ANALYTICS_ENROLLMENTS_TODAY,

    // Biometric Backup (P3)
    BIOMETRIC_BACKUP_TITLE,
    BIOMETRIC_BACKUP_STATUS_TITLE,
    BIOMETRIC_BACKUP_EXPORT,
    BIOMETRIC_BACKUP_DELETE_ALL,
    BIOMETRIC_BACKUP_DELETE_TITLE,
    BIOMETRIC_BACKUP_DELETE_CONFIRM,
    BIOMETRIC_BACKUP_GDPR_TITLE,
    BIOMETRIC_BACKUP_GDPR_DESC,

    // Desktop NFC (P3)
    DESKTOP_NFC_TITLE,
    DESKTOP_NFC_SUBTITLE,
    DESKTOP_NFC_USB_REQUIRED,
    DESKTOP_NFC_CHECKING,
    DESKTOP_NFC_READER_FOUND,
    DESKTOP_NFC_NO_READER,
    DESKTOP_NFC_NOT_AVAILABLE,

    // Offline Mode (P2)
    OFFLINE_MODE_BANNER,
    OFFLINE_LAST_SYNCED,

    // Fingerprint Step-Up
    FINGERPRINT_SECURITY_CHECK,
    FINGERPRINT_SECURITY_CHECK_DESC,
    FINGERPRINT_READY,
    FINGERPRINT_REGISTERING_DEVICE,
    FINGERPRINT_REQUESTING_CHALLENGE,
    FINGERPRINT_SCAN_NOW,
    FINGERPRINT_VERIFYING,
    FINGERPRINT_VERIFIED,
    FINGERPRINT_VERIFY_BUTTON,
    FINGERPRINT_RETRY_BUTTON,
    FINGERPRINT_SKIP,
    PASSWORD_AUTH_METHOD,
}

private val enStrings = mapOf(
    // App
    StringKey.APP_NAME to "FIVUCSAS",
    StringKey.APP_SUBTITLE to "Biometric Authentication",

    // Auth
    StringKey.LOGIN to "Login",
    StringKey.REGISTER to "Register",
    StringKey.LOGOUT to "Logout",
    StringKey.EMAIL to "Email",
    StringKey.PASSWORD to "Password",
    StringKey.CONFIRM_PASSWORD to "Confirm Password",
    StringKey.FIRST_NAME to "First Name",
    StringKey.LAST_NAME to "Last Name",
    StringKey.FORGOT_PASSWORD to "Forgot password?",
    StringKey.DONT_HAVE_ACCOUNT to "Don't have an account? Register",
    StringKey.ALREADY_HAVE_ACCOUNT to "Already have an account? Login",
    StringKey.GUEST_FACE_CHECK to "Continue as Guest (Face Check)",
    StringKey.DEV_ROLE to "Dev Role",
    StringKey.LOGIN_SUCCESS to "Login successful",
    StringKey.REGISTER_SUCCESS to "Registration successful",
    StringKey.BIOMETRIC_AUTH to "Biometric Authentication",

    // Navigation
    StringKey.NAV_DASHBOARD to "Dashboard",
    StringKey.NAV_USERS to "Users",
    StringKey.NAV_ANALYTICS to "Analytics",
    StringKey.NAV_SECURITY to "Security",
    StringKey.NAV_SETTINGS to "Settings",
    StringKey.NAV_PROFILE to "Profile",
    StringKey.NAV_NOTIFICATIONS to "Notifications",
    StringKey.NAV_HELP to "Help",
    StringKey.NAV_ABOUT to "About",
    StringKey.NAV_AUTH_FLOWS to "Auth Flows",
    StringKey.NAV_SESSIONS to "Sessions",
    StringKey.NAV_DEVICES to "Devices",
    StringKey.NAV_ENROLLMENTS to "Enrollments",

    // Dashboard
    StringKey.DASHBOARD_TITLE to "Dashboard",
    StringKey.DASHBOARD_WELCOME to "Welcome back",
    StringKey.TOTAL_USERS to "Total Users",
    StringKey.ACTIVE_USERS to "Active Users",
    StringKey.ENROLLMENTS to "Enrollments",
    StringKey.VERIFICATIONS to "Verifications",

    // Users
    StringKey.USERS_TITLE to "User Management",
    StringKey.USERS_SUBTITLE to "Manage system users and permissions",
    StringKey.ADD_USER to "Add User",
    StringKey.EDIT_USER to "Edit User",
    StringKey.DELETE_USER to "Delete User",
    StringKey.SEARCH_USERS to "Search users...",
    StringKey.USER_NAME to "Name",
    StringKey.USER_EMAIL to "Email",
    StringKey.USER_STATUS to "Status",
    StringKey.USER_ROLE to "Role",
    StringKey.USER_ACTIONS to "Actions",
    StringKey.CONFIRM_DELETE_USER to "Are you sure you want to delete this user?",
    StringKey.USER_ADDED to "User added: {0}",
    StringKey.USER_UPDATED to "User updated: {0}",
    StringKey.USER_DELETED to "User deleted: {0}",

    // Auth Flows
    StringKey.AUTH_FLOWS_TITLE to "Authentication Flows",
    StringKey.AUTH_FLOWS_SUBTITLE to "View configured authentication flows for this tenant",
    StringKey.FLOW_NAME to "Flow Name",
    StringKey.FLOW_OPERATION_TYPE to "Operation Type",
    StringKey.FLOW_STEPS to "Steps",
    StringKey.FLOW_ACTIVE to "Active",
    StringKey.NO_AUTH_FLOWS to "No authentication flows configured",

    // Sessions
    StringKey.SESSIONS_TITLE to "Active Sessions",
    StringKey.SESSIONS_SUBTITLE to "View and manage active authentication sessions",
    StringKey.SESSION_DEVICE to "Device",
    StringKey.SESSION_IP to "IP Address",
    StringKey.SESSION_LAST_ACTIVE to "Last Active",
    StringKey.SESSION_CREATED to "Created",
    StringKey.SESSION_STATUS to "Status",
    StringKey.REVOKE_SESSION to "Revoke",
    StringKey.CONFIRM_REVOKE_SESSION to "Are you sure you want to revoke this session?",
    StringKey.SESSION_REVOKED to "Session revoked successfully",
    StringKey.NO_ACTIVE_SESSIONS to "No active sessions",

    // Devices
    StringKey.DEVICES_TITLE to "Device Management",
    StringKey.DEVICES_SUBTITLE to "Manage registered devices",
    StringKey.DEVICE_NAME to "Device Name",
    StringKey.DEVICE_PLATFORM to "Platform",
    StringKey.DEVICE_REGISTERED to "Registered",
    StringKey.DEVICE_LAST_USED to "Last Used",
    StringKey.REMOVE_DEVICE to "Remove",
    StringKey.CONFIRM_REMOVE_DEVICE to "Are you sure you want to remove this device?",
    StringKey.DEVICE_REMOVED to "Device removed successfully",
    StringKey.NO_DEVICES to "No devices registered",
    StringKey.WEBAUTHN_CREDENTIALS to "WebAuthn Credentials",

    // Enrollments
    StringKey.ENROLLMENTS_TITLE to "Enrollment Management",
    StringKey.ENROLLMENTS_SUBTITLE to "View and manage biometric enrollments",
    StringKey.ENROLLMENT_METHOD to "Method",
    StringKey.ENROLLMENT_STATUS to "Status",
    StringKey.ENROLLMENT_DATE to "Enrolled Date",
    StringKey.START_ENROLLMENT to "Start Enrollment",
    StringKey.NO_ENROLLMENTS to "No enrollments found",
    StringKey.ENROLLED to "Enrolled",
    StringKey.NOT_ENROLLED to "Not Enrolled",

    // Settings
    StringKey.SETTINGS_TITLE to "Settings",
    StringKey.SETTINGS_SUBTITLE to "Configure system settings and preferences",
    StringKey.LANGUAGE to "Language",
    StringKey.THEME to "Theme",
    StringKey.THEME_LIGHT to "Light",
    StringKey.THEME_DARK to "Dark",
    StringKey.THEME_SYSTEM to "System",
    StringKey.NOTIFICATIONS_ENABLED to "Enable Notifications",
    StringKey.SAVE_SETTINGS to "Save Settings",
    StringKey.RESET_SETTINGS to "Reset",
    StringKey.SETTINGS_SAVED to "Settings saved",

    // Common
    StringKey.LOADING to "Loading...",
    StringKey.ERROR to "Error",
    StringKey.RETRY to "Retry",
    StringKey.CANCEL to "Cancel",
    StringKey.CONFIRM to "Confirm",
    StringKey.SAVE to "Save",
    StringKey.DELETE to "Delete",
    StringKey.EDIT to "Edit",
    StringKey.BACK to "Back",
    StringKey.CLOSE to "Close",
    StringKey.SEARCH to "Search",
    StringKey.REFRESH to "Refresh",
    StringKey.NO_DATA to "No data available",
    StringKey.COMING_SOON to "Coming Soon",
    StringKey.SUCCESS to "Success",
    StringKey.FAILED to "Failed",
    StringKey.ACTIVE to "Active",
    StringKey.INACTIVE to "Inactive",
    StringKey.UNKNOWN to "Unknown",

    // Errors
    StringKey.ERROR_NETWORK to "Network error. Please check your connection.",
    StringKey.ERROR_UNAUTHORIZED to "Session expired. Please login again.",
    StringKey.ERROR_SERVER to "Server error. Please try again later.",
    StringKey.ERROR_NOT_FOUND to "Resource not found.",
    StringKey.ERROR_TIMEOUT to "Request timed out. Please try again.",
    StringKey.ERROR_UNKNOWN to "An unexpected error occurred.",
    StringKey.ERROR_VALIDATION to "Please check your input.",
    StringKey.ERROR_LOAD_FAILED to "Failed to load data.",
    StringKey.ERROR_EMPTY_FIELD to "This field is required.",
    StringKey.ERROR_INVALID_EMAIL to "Please enter a valid email address.",
    StringKey.ERROR_PASSWORD_TOO_SHORT to "Password must be at least 8 characters.",
    StringKey.ERROR_PASSWORDS_DONT_MATCH to "Passwords do not match.",

    // Security
    StringKey.SECURITY_TITLE to "Security",
    StringKey.SECURITY_SUBTITLE to "Monitor system security and sessions",
    StringKey.ACTIVE_SESSIONS to "Active Sessions",
    StringKey.REVOKE_ALL_SESSIONS to "Revoke All Sessions",

    // Biometric
    StringKey.FACE_RECOGNITION to "Face Recognition",
    StringKey.FINGERPRINT to "Fingerprint",
    StringKey.VOICE_RECOGNITION to "Voice Recognition",
    StringKey.NFC_DOCUMENT to "NFC Document",
    StringKey.TOTP to "TOTP",
    StringKey.EMAIL_OTP to "Email OTP",
    StringKey.SMS_OTP to "SMS OTP",
    StringKey.HARDWARE_KEY to "Hardware Key",

    // Tenant
    StringKey.TENANT_MANAGEMENT to "Tenant Management",
    StringKey.TENANT_NAME to "Tenant Name",
    StringKey.TENANT_DOMAIN to "Domain",
    StringKey.CREATE_TENANT to "Create Tenant",

    // Profile
    StringKey.PROFILE_TITLE to "Profile",
    StringKey.CHANGE_PASSWORD to "Change Password",
    StringKey.CURRENT_PASSWORD to "Current Password",
    StringKey.NEW_PASSWORD to "New Password",

    // Voice
    StringKey.VOICE_ENROLL to "Enroll",
    StringKey.VOICE_VERIFY to "Verify",
    StringKey.VOICE_SEARCH to "Search",
    StringKey.VOICE_ENROLL_INSTRUCTION to "Speak clearly for 3-5 seconds to enroll your voice.",
    StringKey.VOICE_VERIFY_INSTRUCTION to "Speak to verify your identity against your enrolled voice.",
    StringKey.VOICE_SEARCH_INSTRUCTION to "Speak to search for a matching user in the system.",
    StringKey.VOICE_PERMISSION_REQUIRED to "Microphone permission is required",
    StringKey.VOICE_TAP_TO_RECORD to "Tap the microphone to start recording",
    StringKey.VOICE_RECORDING to "Recording",
    StringKey.VOICE_VERIFIED to "Voice Verified",
    StringKey.VOICE_NOT_VERIFIED to "Voice Not Verified",
    StringKey.VOICE_CONFIDENCE to "Confidence",
    StringKey.VOICE_USER_FOUND to "User Found",
    StringKey.VOICE_USER_NOT_FOUND to "No Matching User Found",

    // OTP
    StringKey.OTP_SEND to "Send OTP",
    StringKey.OTP_VERIFY to "Verify OTP",
    StringKey.OTP_RESEND to "Resend OTP",
    StringKey.OTP_CODE to "OTP Code",
    StringKey.OTP_ENTER_CODE to "Enter the 6-digit code",
    StringKey.OTP_VERIFIED_SUCCESS to "OTP verified successfully!",

    // Email OTP
    StringKey.EMAIL_OTP_TITLE to "Email OTP Verification",
    StringKey.EMAIL_OTP_DESCRIPTION to "Enter your email to receive a one-time verification code.",

    // SMS OTP
    StringKey.SMS_OTP_TITLE to "SMS OTP Verification",
    StringKey.SMS_OTP_DESCRIPTION to "Enter your phone number to receive a one-time code via SMS.",
    StringKey.SMS_PHONE_NUMBER to "Phone Number",
    StringKey.SMS_TWILIO_INFO to "Note: SMS service (Twilio) is not yet activated. This feature is pending activation.",

    // TOTP
    StringKey.TOTP_TITLE to "TOTP Setup",
    StringKey.TOTP_DESCRIPTION to "Set up Time-based One-Time Password for two-factor authentication.",
    StringKey.TOTP_SETUP to "Setup TOTP",
    StringKey.TOTP_SCAN_QR to "Scan this QR code with your authenticator app (e.g., Google Authenticator)",
    StringKey.TOTP_MANUAL_KEY to "Manual entry key:",
    StringKey.TOTP_ENTER_CODE to "Enter the code from your authenticator app",
    StringKey.TOTP_ALREADY_ENABLED to "TOTP is already enabled",
    StringKey.TOTP_SETUP_COMPLETE to "TOTP setup complete! Two-factor authentication is now enabled.",

    // Analytics
    StringKey.ANALYTICS_TITLE to "Analytics",
    StringKey.ANALYTICS_SUBTITLE to "System statistics and metrics overview",
    StringKey.ANALYTICS_VERIFICATIONS_TODAY to "Verifications Today",
    StringKey.ANALYTICS_FAILED_ATTEMPTS to "Failed Attempts",
    StringKey.ANALYTICS_SUCCESS_RATE to "Success Rate",
    StringKey.ANALYTICS_PENDING to "Pending Verifications",

    // Liveness
    StringKey.LIVENESS_TITLE to "Face Liveness Puzzle",
    StringKey.LIVENESS_SUBTITLE to "Complete facial actions to prove liveness",
    StringKey.LIVENESS_BLINK to "Blink your eyes",
    StringKey.LIVENESS_SMILE to "Smile",
    StringKey.LIVENESS_TURN_LEFT to "Turn your head left",
    StringKey.LIVENESS_TURN_RIGHT to "Turn your head right",
    StringKey.LIVENESS_NOD to "Nod your head up",
    StringKey.LIVENESS_OPEN_MOUTH to "Open your mouth",
    StringKey.LIVENESS_CLIENT_SCORE to "Client Score",
    StringKey.LIVENESS_SERVER_SCORE to "Server Score",
    StringKey.LIVENESS_VERIFIED to "Liveness Verified",
    StringKey.LIVENESS_FAILED to "Liveness Check Failed",

    // Card Detection
    StringKey.CARD_DETECTION_TITLE to "Card Detection",
    StringKey.CARD_DETECTION_SUBTITLE to "Detect ID cards using AI",
    StringKey.CARD_DETECTION_CAPTURE to "Capture Card",
    StringKey.CARD_DETECTION_RESULT to "Detection Result",
    StringKey.CARD_TYPE to "Card Type",
    StringKey.CARD_CONFIDENCE to "Confidence",

    // Hardware Token
    StringKey.HARDWARE_TOKEN_TITLE to "Hardware Security Key",
    StringKey.HARDWARE_TOKEN_SUBTITLE to "WebAuthn cross-platform authentication",
    StringKey.HARDWARE_TOKEN_REGISTER to "Register Token",
    StringKey.HARDWARE_TOKEN_VERIFY to "Verify Token",
    StringKey.HARDWARE_TOKEN_CREDENTIAL_ID to "Credential ID",
    StringKey.HARDWARE_TOKEN_TRANSPORTS to "Transports",

    // Analytics Enhanced (P3)
    StringKey.ANALYTICS_OVERVIEW to "Overview",
    StringKey.ANALYTICS_ENROLLMENTS_BY_METHOD to "Enrollments by Method",
    StringKey.ANALYTICS_RECENT_ACTIVITY to "Recent Activity",
    StringKey.ANALYTICS_LOGINS_TODAY to "Logins Today",
    StringKey.ANALYTICS_REGISTRATIONS_TODAY to "Registrations",
    StringKey.ANALYTICS_ENROLLMENTS_TODAY to "Enrollments",

    // Biometric Backup (P3)
    StringKey.BIOMETRIC_BACKUP_TITLE to "Biometric Data & Privacy",
    StringKey.BIOMETRIC_BACKUP_STATUS_TITLE to "Enrolled Biometrics",
    StringKey.BIOMETRIC_BACKUP_EXPORT to "Export My Data",
    StringKey.BIOMETRIC_BACKUP_DELETE_ALL to "Delete All My Biometric Data",
    StringKey.BIOMETRIC_BACKUP_DELETE_TITLE to "Delete Biometric Data",
    StringKey.BIOMETRIC_BACKUP_DELETE_CONFIRM to "This will permanently delete all your biometric enrollments (face, voice, fingerprint). This action cannot be undone. Are you sure?",
    StringKey.BIOMETRIC_BACKUP_GDPR_TITLE to "Your Data Rights (GDPR/KVKK)",
    StringKey.BIOMETRIC_BACKUP_GDPR_DESC to "You have the right to view, export, and delete your biometric data at any time. Deleting your data will unenroll you from all biometric authentication methods.",

    // Desktop NFC (P3)
    StringKey.DESKTOP_NFC_TITLE to "Desktop NFC",
    StringKey.DESKTOP_NFC_SUBTITLE to "NFC reader status for desktop",
    StringKey.DESKTOP_NFC_USB_REQUIRED to "NFC on desktop requires a USB smart card reader.",
    StringKey.DESKTOP_NFC_CHECKING to "Checking for NFC readers...",
    StringKey.DESKTOP_NFC_READER_FOUND to "NFC reader detected: {0}",
    StringKey.DESKTOP_NFC_NO_READER to "No NFC reader found. Connect a USB smart card reader to use NFC features.",
    StringKey.DESKTOP_NFC_NOT_AVAILABLE to "Smart card I/O library is not available on this system.",

    // Offline Mode (P2)
    StringKey.OFFLINE_MODE_BANNER to "You are offline. Showing cached data.",
    StringKey.OFFLINE_LAST_SYNCED to "Last synced: {0}",

    // Fingerprint Step-Up
    StringKey.FINGERPRINT_SECURITY_CHECK to "Security check required",
    StringKey.FINGERPRINT_SECURITY_CHECK_DESC to "Before opening Home, verify with fingerprint to get a short-lived step-up token.",
    StringKey.FINGERPRINT_READY to "Ready",
    StringKey.FINGERPRINT_REGISTERING_DEVICE to "Registering this device",
    StringKey.FINGERPRINT_REQUESTING_CHALLENGE to "Requesting secure challenge",
    StringKey.FINGERPRINT_SCAN_NOW to "Scan your fingerprint now",
    StringKey.FINGERPRINT_VERIFYING to "Verifying signature",
    StringKey.FINGERPRINT_VERIFIED to "Verification successful",
    StringKey.FINGERPRINT_VERIFY_BUTTON to "Verify fingerprint",
    StringKey.FINGERPRINT_RETRY_BUTTON to "Retry",
    StringKey.FINGERPRINT_SKIP to "Skip for now",
    StringKey.PASSWORD_AUTH_METHOD to "Password",
)

private val trStrings = mapOf(
    // App
    StringKey.APP_NAME to "FIVUCSAS",
    StringKey.APP_SUBTITLE to "Biyometrik Kimlik Dogrulama",

    // Auth
    StringKey.LOGIN to "Giris Yap",
    StringKey.REGISTER to "Kayit Ol",
    StringKey.LOGOUT to "Cikis Yap",
    StringKey.EMAIL to "E-posta",
    StringKey.PASSWORD to "Sifre",
    StringKey.CONFIRM_PASSWORD to "Sifre Tekrar",
    StringKey.FIRST_NAME to "Ad",
    StringKey.LAST_NAME to "Soyad",
    StringKey.FORGOT_PASSWORD to "Sifremi unuttum?",
    StringKey.DONT_HAVE_ACCOUNT to "Hesabiniz yok mu? Kayit olun",
    StringKey.ALREADY_HAVE_ACCOUNT to "Zaten hesabiniz var mi? Giris yapin",
    StringKey.GUEST_FACE_CHECK to "Misafir Olarak Devam Et (Yuz Kontrolu)",
    StringKey.DEV_ROLE to "Gelistirici Rolu",
    StringKey.LOGIN_SUCCESS to "Giris basarili",
    StringKey.REGISTER_SUCCESS to "Kayit basarili",
    StringKey.BIOMETRIC_AUTH to "Biyometrik Kimlik Dogrulama",

    // Navigation
    StringKey.NAV_DASHBOARD to "Ana Sayfa",
    StringKey.NAV_USERS to "Kullanicilar",
    StringKey.NAV_ANALYTICS to "Analitik",
    StringKey.NAV_SECURITY to "Guvenlik",
    StringKey.NAV_SETTINGS to "Ayarlar",
    StringKey.NAV_PROFILE to "Profil",
    StringKey.NAV_NOTIFICATIONS to "Bildirimler",
    StringKey.NAV_HELP to "Yardim",
    StringKey.NAV_ABOUT to "Hakkinda",
    StringKey.NAV_AUTH_FLOWS to "Kimlik Dogrulama Akislari",
    StringKey.NAV_SESSIONS to "Oturumlar",
    StringKey.NAV_DEVICES to "Cihazlar",
    StringKey.NAV_ENROLLMENTS to "Kayitlar",

    // Dashboard
    StringKey.DASHBOARD_TITLE to "Ana Sayfa",
    StringKey.DASHBOARD_WELCOME to "Tekrar hos geldiniz",
    StringKey.TOTAL_USERS to "Toplam Kullanici",
    StringKey.ACTIVE_USERS to "Aktif Kullanici",
    StringKey.ENROLLMENTS to "Kayitlar",
    StringKey.VERIFICATIONS to "Dogrulamalar",

    // Users
    StringKey.USERS_TITLE to "Kullanici Yonetimi",
    StringKey.USERS_SUBTITLE to "Sistem kullanicilarini ve izinleri yonetin",
    StringKey.ADD_USER to "Kullanici Ekle",
    StringKey.EDIT_USER to "Kullaniciyi Duzenle",
    StringKey.DELETE_USER to "Kullaniciyi Sil",
    StringKey.SEARCH_USERS to "Kullanici ara...",
    StringKey.USER_NAME to "Isim",
    StringKey.USER_EMAIL to "E-posta",
    StringKey.USER_STATUS to "Durum",
    StringKey.USER_ROLE to "Rol",
    StringKey.USER_ACTIONS to "Islemler",
    StringKey.CONFIRM_DELETE_USER to "Bu kullaniciyi silmek istediginizden emin misiniz?",
    StringKey.USER_ADDED to "Kullanici eklendi: {0}",
    StringKey.USER_UPDATED to "Kullanici guncellendi: {0}",
    StringKey.USER_DELETED to "Kullanici silindi: {0}",

    // Auth Flows
    StringKey.AUTH_FLOWS_TITLE to "Kimlik Dogrulama Akislari",
    StringKey.AUTH_FLOWS_SUBTITLE to "Bu kiracinin yapilandirilmis kimlik dogrulama akislarini goruntuleyin",
    StringKey.FLOW_NAME to "Akis Adi",
    StringKey.FLOW_OPERATION_TYPE to "Islem Turu",
    StringKey.FLOW_STEPS to "Adimlar",
    StringKey.FLOW_ACTIVE to "Aktif",
    StringKey.NO_AUTH_FLOWS to "Yapilandirilmis kimlik dogrulama akisi yok",

    // Sessions
    StringKey.SESSIONS_TITLE to "Aktif Oturumlar",
    StringKey.SESSIONS_SUBTITLE to "Aktif kimlik dogrulama oturumlarini goruntuleyin ve yonetin",
    StringKey.SESSION_DEVICE to "Cihaz",
    StringKey.SESSION_IP to "IP Adresi",
    StringKey.SESSION_LAST_ACTIVE to "Son Etkinlik",
    StringKey.SESSION_CREATED to "Olusturulma",
    StringKey.SESSION_STATUS to "Durum",
    StringKey.REVOKE_SESSION to "Iptal Et",
    StringKey.CONFIRM_REVOKE_SESSION to "Bu oturumu iptal etmek istediginizden emin misiniz?",
    StringKey.SESSION_REVOKED to "Oturum basariyla iptal edildi",
    StringKey.NO_ACTIVE_SESSIONS to "Aktif oturum yok",

    // Devices
    StringKey.DEVICES_TITLE to "Cihaz Yonetimi",
    StringKey.DEVICES_SUBTITLE to "Kayitli cihazlari yonetin",
    StringKey.DEVICE_NAME to "Cihaz Adi",
    StringKey.DEVICE_PLATFORM to "Platform",
    StringKey.DEVICE_REGISTERED to "Kayit Tarihi",
    StringKey.DEVICE_LAST_USED to "Son Kullanim",
    StringKey.REMOVE_DEVICE to "Kaldir",
    StringKey.CONFIRM_REMOVE_DEVICE to "Bu cihazi kaldirmak istediginizden emin misiniz?",
    StringKey.DEVICE_REMOVED to "Cihaz basariyla kaldirildi",
    StringKey.NO_DEVICES to "Kayitli cihaz yok",
    StringKey.WEBAUTHN_CREDENTIALS to "WebAuthn Kimlik Bilgileri",

    // Enrollments
    StringKey.ENROLLMENTS_TITLE to "Kayit Yonetimi",
    StringKey.ENROLLMENTS_SUBTITLE to "Biyometrik kayitlari goruntuleyin ve yonetin",
    StringKey.ENROLLMENT_METHOD to "Yontem",
    StringKey.ENROLLMENT_STATUS to "Durum",
    StringKey.ENROLLMENT_DATE to "Kayit Tarihi",
    StringKey.START_ENROLLMENT to "Kayit Baslat",
    StringKey.NO_ENROLLMENTS to "Kayit bulunamadi",
    StringKey.ENROLLED to "Kayitli",
    StringKey.NOT_ENROLLED to "Kayitli Degil",

    // Settings
    StringKey.SETTINGS_TITLE to "Ayarlar",
    StringKey.SETTINGS_SUBTITLE to "Sistem ayarlarini ve tercihlerini yapilandirin",
    StringKey.LANGUAGE to "Dil",
    StringKey.THEME to "Tema",
    StringKey.THEME_LIGHT to "Acik",
    StringKey.THEME_DARK to "Koyu",
    StringKey.THEME_SYSTEM to "Sistem",
    StringKey.NOTIFICATIONS_ENABLED to "Bildirimleri Etkinlestir",
    StringKey.SAVE_SETTINGS to "Ayarlari Kaydet",
    StringKey.RESET_SETTINGS to "Sifirla",
    StringKey.SETTINGS_SAVED to "Ayarlar kaydedildi",

    // Common
    StringKey.LOADING to "Yukleniyor...",
    StringKey.ERROR to "Hata",
    StringKey.RETRY to "Tekrar Dene",
    StringKey.CANCEL to "Iptal",
    StringKey.CONFIRM to "Onayla",
    StringKey.SAVE to "Kaydet",
    StringKey.DELETE to "Sil",
    StringKey.EDIT to "Duzenle",
    StringKey.BACK to "Geri",
    StringKey.CLOSE to "Kapat",
    StringKey.SEARCH to "Ara",
    StringKey.REFRESH to "Yenile",
    StringKey.NO_DATA to "Veri bulunamadi",
    StringKey.COMING_SOON to "Yakinda",
    StringKey.SUCCESS to "Basarili",
    StringKey.FAILED to "Basarisiz",
    StringKey.ACTIVE to "Aktif",
    StringKey.INACTIVE to "Pasif",
    StringKey.UNKNOWN to "Bilinmiyor",

    // Errors
    StringKey.ERROR_NETWORK to "Ag hatasi. Lutfen baglantinizi kontrol edin.",
    StringKey.ERROR_UNAUTHORIZED to "Oturum suresi doldu. Lutfen tekrar giris yapin.",
    StringKey.ERROR_SERVER to "Sunucu hatasi. Lutfen daha sonra tekrar deneyin.",
    StringKey.ERROR_NOT_FOUND to "Kaynak bulunamadi.",
    StringKey.ERROR_TIMEOUT to "Istek zaman asimina ugradi. Lutfen tekrar deneyin.",
    StringKey.ERROR_UNKNOWN to "Beklenmeyen bir hata olustu.",
    StringKey.ERROR_VALIDATION to "Lutfen girdiginizi kontrol edin.",
    StringKey.ERROR_LOAD_FAILED to "Veri yuklenemedi.",
    StringKey.ERROR_EMPTY_FIELD to "Bu alan zorunludur.",
    StringKey.ERROR_INVALID_EMAIL to "Lutfen gecerli bir e-posta adresi girin.",
    StringKey.ERROR_PASSWORD_TOO_SHORT to "Sifre en az 8 karakter olmalidir.",
    StringKey.ERROR_PASSWORDS_DONT_MATCH to "Sifreler eslesmiyor.",

    // Security
    StringKey.SECURITY_TITLE to "Guvenlik",
    StringKey.SECURITY_SUBTITLE to "Sistem guvenligini ve oturumlari izleyin",
    StringKey.ACTIVE_SESSIONS to "Aktif Oturumlar",
    StringKey.REVOKE_ALL_SESSIONS to "Tum Oturumlari Iptal Et",

    // Biometric
    StringKey.FACE_RECOGNITION to "Yuz Tanima",
    StringKey.FINGERPRINT to "Parmak Izi",
    StringKey.VOICE_RECOGNITION to "Ses Tanima",
    StringKey.NFC_DOCUMENT to "NFC Belge",
    StringKey.TOTP to "TOTP",
    StringKey.EMAIL_OTP to "E-posta OTP",
    StringKey.SMS_OTP to "SMS OTP",
    StringKey.HARDWARE_KEY to "Donanim Anahtari",

    // Tenant
    StringKey.TENANT_MANAGEMENT to "Kiraci Yonetimi",
    StringKey.TENANT_NAME to "Kiraci Adi",
    StringKey.TENANT_DOMAIN to "Alan Adi",
    StringKey.CREATE_TENANT to "Kiraci Olustur",

    // Profile
    StringKey.PROFILE_TITLE to "Profil",
    StringKey.CHANGE_PASSWORD to "Sifre Degistir",
    StringKey.CURRENT_PASSWORD to "Mevcut Sifre",
    StringKey.NEW_PASSWORD to "Yeni Sifre",

    // Voice
    StringKey.VOICE_ENROLL to "Kayit",
    StringKey.VOICE_VERIFY to "Dogrula",
    StringKey.VOICE_SEARCH to "Ara",
    StringKey.VOICE_ENROLL_INSTRUCTION to "Sesinizi kaydetmek icin 3-5 saniye net konusun.",
    StringKey.VOICE_VERIFY_INSTRUCTION to "Kimliginizi dogrulamak icin konusun.",
    StringKey.VOICE_SEARCH_INSTRUCTION to "Sistemde eslesen bir kullanici aramak icin konusun.",
    StringKey.VOICE_PERMISSION_REQUIRED to "Mikrofon izni gerekli",
    StringKey.VOICE_TAP_TO_RECORD to "Kayit baslatmak icin mikrofona dokunun",
    StringKey.VOICE_RECORDING to "Kayit yapiliyor",
    StringKey.VOICE_VERIFIED to "Ses Dogrulandi",
    StringKey.VOICE_NOT_VERIFIED to "Ses Dogrulanamadi",
    StringKey.VOICE_CONFIDENCE to "Guven",
    StringKey.VOICE_USER_FOUND to "Kullanici Bulundu",
    StringKey.VOICE_USER_NOT_FOUND to "Eslesen Kullanici Bulunamadi",

    // OTP
    StringKey.OTP_SEND to "OTP Gonder",
    StringKey.OTP_VERIFY to "OTP Dogrula",
    StringKey.OTP_RESEND to "OTP Tekrar Gonder",
    StringKey.OTP_CODE to "OTP Kodu",
    StringKey.OTP_ENTER_CODE to "6 haneli kodu girin",
    StringKey.OTP_VERIFIED_SUCCESS to "OTP basariyla dogrulandi!",

    // Email OTP
    StringKey.EMAIL_OTP_TITLE to "E-posta OTP Dogrulama",
    StringKey.EMAIL_OTP_DESCRIPTION to "Tek kullanimlik dogrulama kodu almak icin e-postanizi girin.",

    // SMS OTP
    StringKey.SMS_OTP_TITLE to "SMS OTP Dogrulama",
    StringKey.SMS_OTP_DESCRIPTION to "SMS ile tek kullanimlik kod almak icin telefon numaranizi girin.",
    StringKey.SMS_PHONE_NUMBER to "Telefon Numarasi",
    StringKey.SMS_TWILIO_INFO to "Not: SMS hizmeti (Twilio) henuz aktif edilmedi. Bu ozellik aktivasyon bekliyor.",

    // TOTP
    StringKey.TOTP_TITLE to "TOTP Kurulumu",
    StringKey.TOTP_DESCRIPTION to "Iki faktorlu kimlik dogrulama icin Zamana Dayali Tek Kullanimlik Sifre kurun.",
    StringKey.TOTP_SETUP to "TOTP Kur",
    StringKey.TOTP_SCAN_QR to "Bu QR kodu dogrulama uygulamanizla tarayin (ornegin Google Authenticator)",
    StringKey.TOTP_MANUAL_KEY to "Manuel giris anahtari:",
    StringKey.TOTP_ENTER_CODE to "Dogrulama uygulamanizdan gelen kodu girin",
    StringKey.TOTP_ALREADY_ENABLED to "TOTP zaten etkin",
    StringKey.TOTP_SETUP_COMPLETE to "TOTP kurulumu tamamlandi! Iki faktorlu kimlik dogrulama artik etkin.",

    // Analytics
    StringKey.ANALYTICS_TITLE to "Analitik",
    StringKey.ANALYTICS_SUBTITLE to "Sistem istatistikleri ve metrikler genel gorunumu",
    StringKey.ANALYTICS_VERIFICATIONS_TODAY to "Bugunun Dogrulamalari",
    StringKey.ANALYTICS_FAILED_ATTEMPTS to "Basarisiz Denemeler",
    StringKey.ANALYTICS_SUCCESS_RATE to "Basari Orani",
    StringKey.ANALYTICS_PENDING to "Bekleyen Dogrulamalar",

    // Liveness
    StringKey.LIVENESS_TITLE to "Yuz Canlilik Bulmacasi",
    StringKey.LIVENESS_SUBTITLE to "Canlilik kanitlamak icin yuz hareketlerini tamamlayin",
    StringKey.LIVENESS_BLINK to "Gozlerinizi kirpin",
    StringKey.LIVENESS_SMILE to "Gulumseyin",
    StringKey.LIVENESS_TURN_LEFT to "Basinizi sola cevirin",
    StringKey.LIVENESS_TURN_RIGHT to "Basinizi saga cevirin",
    StringKey.LIVENESS_NOD to "Basinizi yukari kaldin",
    StringKey.LIVENESS_OPEN_MOUTH to "Agzinizi acin",
    StringKey.LIVENESS_CLIENT_SCORE to "Istemci Puani",
    StringKey.LIVENESS_SERVER_SCORE to "Sunucu Puani",
    StringKey.LIVENESS_VERIFIED to "Canlilik Dogrulandi",
    StringKey.LIVENESS_FAILED to "Canlilik Kontrolu Basarisiz",

    // Card Detection
    StringKey.CARD_DETECTION_TITLE to "Kart Algilama",
    StringKey.CARD_DETECTION_SUBTITLE to "Yapay zeka ile kimlik karti algilama",
    StringKey.CARD_DETECTION_CAPTURE to "Kart Yakala",
    StringKey.CARD_DETECTION_RESULT to "Algilama Sonucu",
    StringKey.CARD_TYPE to "Kart Turu",
    StringKey.CARD_CONFIDENCE to "Guven",

    // Hardware Token
    StringKey.HARDWARE_TOKEN_TITLE to "Donanim Guvenlik Anahtari",
    StringKey.HARDWARE_TOKEN_SUBTITLE to "WebAuthn coklu platform kimlik dogrulama",
    StringKey.HARDWARE_TOKEN_REGISTER to "Token Kaydet",
    StringKey.HARDWARE_TOKEN_VERIFY to "Token Dogrula",
    StringKey.HARDWARE_TOKEN_CREDENTIAL_ID to "Kimlik Bilgisi ID",
    StringKey.HARDWARE_TOKEN_TRANSPORTS to "Tasimacilar",

    // Analytics Enhanced (P3)
    StringKey.ANALYTICS_OVERVIEW to "Genel Bakis",
    StringKey.ANALYTICS_ENROLLMENTS_BY_METHOD to "Yonteme Gore Kayitlar",
    StringKey.ANALYTICS_RECENT_ACTIVITY to "Son Etkinlikler",
    StringKey.ANALYTICS_LOGINS_TODAY to "Bugunun Girisleri",
    StringKey.ANALYTICS_REGISTRATIONS_TODAY to "Kayitlar",
    StringKey.ANALYTICS_ENROLLMENTS_TODAY to "Kayitlar",

    // Biometric Backup (P3)
    StringKey.BIOMETRIC_BACKUP_TITLE to "Biyometrik Veri ve Gizlilik",
    StringKey.BIOMETRIC_BACKUP_STATUS_TITLE to "Kayitli Biyometrikler",
    StringKey.BIOMETRIC_BACKUP_EXPORT to "Verilerimi Disa Aktar",
    StringKey.BIOMETRIC_BACKUP_DELETE_ALL to "Tum Biyometrik Verilerimi Sil",
    StringKey.BIOMETRIC_BACKUP_DELETE_TITLE to "Biyometrik Verileri Sil",
    StringKey.BIOMETRIC_BACKUP_DELETE_CONFIRM to "Bu islem tum biyometrik kayitlarinizi (yuz, ses, parmak izi) kalici olarak silecektir. Bu islem geri alinamaz. Emin misiniz?",
    StringKey.BIOMETRIC_BACKUP_GDPR_TITLE to "Veri Haklariniz (GDPR/KVKK)",
    StringKey.BIOMETRIC_BACKUP_GDPR_DESC to "Biyometrik verilerinizi istediginiz zaman goruntuleme, disa aktarma ve silme hakkiniz vardir. Verilerinizi silmek, tum biyometrik kimlik dogrulama yontemlerinden kaydınızı kaldıracaktır.",

    // Desktop NFC (P3)
    StringKey.DESKTOP_NFC_TITLE to "Masaustu NFC",
    StringKey.DESKTOP_NFC_SUBTITLE to "Masaustu icin NFC okuyucu durumu",
    StringKey.DESKTOP_NFC_USB_REQUIRED to "Masaustunde NFC icin USB akilli kart okuyucu gereklidir.",
    StringKey.DESKTOP_NFC_CHECKING to "NFC okuyuculari kontrol ediliyor...",
    StringKey.DESKTOP_NFC_READER_FOUND to "NFC okuyucu algilandi: {0}",
    StringKey.DESKTOP_NFC_NO_READER to "NFC okuyucu bulunamadi. NFC ozelliklerini kullanmak icin bir USB akilli kart okuyucu baglayin.",
    StringKey.DESKTOP_NFC_NOT_AVAILABLE to "Akilli kart G/C kutuphanesi bu sistemde mevcut degil.",

    // Offline Mode (P2)
    StringKey.OFFLINE_MODE_BANNER to "Cevrimdisi moddasiniz. Onbellekteki veriler gosteriliyor.",
    StringKey.OFFLINE_LAST_SYNCED to "Son senkronizasyon: {0}",

    // Fingerprint Step-Up
    StringKey.FINGERPRINT_SECURITY_CHECK to "Guvenlik kontrolu gerekli",
    StringKey.FINGERPRINT_SECURITY_CHECK_DESC to "Ana sayfayi acmadan once, kisa sureli adim-yukselme tokeni almak icin parmak izinizi dogrulayin.",
    StringKey.FINGERPRINT_READY to "Hazir",
    StringKey.FINGERPRINT_REGISTERING_DEVICE to "Cihaz kaydediliyor",
    StringKey.FINGERPRINT_REQUESTING_CHALLENGE to "Guvenli sorgu isteniyor",
    StringKey.FINGERPRINT_SCAN_NOW to "Simdi parmak izinizi tarayin",
    StringKey.FINGERPRINT_VERIFYING to "Imza dogrulaniyor",
    StringKey.FINGERPRINT_VERIFIED to "Dogrulama basarili",
    StringKey.FINGERPRINT_VERIFY_BUTTON to "Parmak izini dogrula",
    StringKey.FINGERPRINT_RETRY_BUTTON to "Tekrar dene",
    StringKey.FINGERPRINT_SKIP to "Simdilik atla",
    StringKey.PASSWORD_AUTH_METHOD to "Sifre",
)

/**
 * Convenience function to get a localized string
 */
fun s(key: StringKey): String = StringResources.get(key)

/**
 * Convenience function to get a localized string with arguments
 */
fun s(key: StringKey, vararg args: Any): String = StringResources.get(key, *args)
