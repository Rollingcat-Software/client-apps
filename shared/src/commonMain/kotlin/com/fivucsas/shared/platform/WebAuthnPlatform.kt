package com.fivucsas.shared.platform

/**
 * Provides the platform-specific WebAuthn authenticator implementation.
 */
expect fun provideWebAuthnAuthenticator(): WebAuthnAuthenticator

/**
 * Whether WebAuthn/FIDO2 flow is available on this platform.
 */
expect fun isWebAuthnAvailable(): Boolean
