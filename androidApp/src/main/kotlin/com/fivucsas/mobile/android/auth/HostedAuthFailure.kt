package com.fivucsas.mobile.android.auth

import com.fivucsas.shared.i18n.StringKey
import net.openid.appauth.AuthorizationException
import java.io.IOException

/**
 * Why a hosted (Custom Tab) sign-in ended without tokens. Each cause has its own
 * message so a failed attempt tells the user, and the log, what actually happened.
 */
enum class HostedAuthFailure(val messageKey: StringKey) {
    /** The tab was closed, or the hosted page returned `access_denied` (user cancelled). */
    CANCELLED(StringKey.LOGIN_HOSTED_CANCELLED),

    /** The hosted page redirected back with another OAuth error, or the state did not match. */
    AUTHORIZATION_REJECTED(StringKey.LOGIN_HOSTED_REJECTED),

    /** The code came back but the token endpoint refused it (for example `invalid_grant`). */
    TOKEN_EXCHANGE_FAILED(StringKey.LOGIN_HOSTED_EXCHANGE_FAILED),

    NETWORK(StringKey.ERROR_NETWORK),

    UNKNOWN(StringKey.LOGIN_ERROR_GENERIC);

    companion object {
        fun classify(error: Throwable): HostedAuthFailure {
            if (error is IOException) return NETWORK
            if (error !is AuthorizationException) return UNKNOWN
            return when {
                error == AuthorizationException.GeneralErrors.USER_CANCELED_AUTH_FLOW -> CANCELLED
                error == AuthorizationException.AuthorizationRequestErrors.ACCESS_DENIED -> CANCELLED
                error == AuthorizationException.GeneralErrors.NETWORK_ERROR -> NETWORK
                error == AuthorizationException.AuthorizationRequestErrors.STATE_MISMATCH -> AUTHORIZATION_REJECTED
                error.type == AuthorizationException.TYPE_OAUTH_TOKEN_ERROR -> TOKEN_EXCHANGE_FAILED
                error.type == AuthorizationException.TYPE_OAUTH_AUTHORIZATION_ERROR -> AUTHORIZATION_REJECTED
                else -> UNKNOWN
            }
        }
    }
}
