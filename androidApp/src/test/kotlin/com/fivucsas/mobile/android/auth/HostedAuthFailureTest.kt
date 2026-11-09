package com.fivucsas.mobile.android.auth

import com.fivucsas.shared.i18n.StringKey
import net.openid.appauth.AuthorizationException
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class HostedAuthFailureTest {

    @Test
    fun classify_WhenUserClosesTheCustomTab_ShouldBeCancelledNotAnError() {
        // AppAuth returns RESULT_CANCELED with this exception in the intent when the
        // tab is closed; the screen used to show LOGIN_ERROR_GENERIC for it.
        val failure = HostedAuthFailure.classify(AuthorizationException.GeneralErrors.USER_CANCELED_AUTH_FLOW)

        assertEquals(HostedAuthFailure.CANCELLED, failure)
        assertEquals(StringKey.LOGIN_HOSTED_CANCELLED, failure.messageKey)
    }

    @Test
    fun classify_WhenHostedPageReturnsAccessDenied_ShouldBeCancelled() {
        val failure = HostedAuthFailure.classify(AuthorizationException.AuthorizationRequestErrors.ACCESS_DENIED)

        assertEquals(HostedAuthFailure.CANCELLED, failure)
    }

    @Test
    fun classify_WhenHostedPageReturnsAnotherAuthorizationError_ShouldBeRejected() {
        val failure = HostedAuthFailure.classify(AuthorizationException.AuthorizationRequestErrors.SERVER_ERROR)

        assertEquals(HostedAuthFailure.AUTHORIZATION_REJECTED, failure)
        assertEquals(StringKey.LOGIN_HOSTED_REJECTED, failure.messageKey)
    }

    @Test
    fun classify_WhenTokenEndpointRejectsTheCode_ShouldBeExchangeFailed() {
        val failure = HostedAuthFailure.classify(AuthorizationException.TokenRequestErrors.INVALID_GRANT)

        assertEquals(HostedAuthFailure.TOKEN_EXCHANGE_FAILED, failure)
        assertEquals(StringKey.LOGIN_HOSTED_EXCHANGE_FAILED, failure.messageKey)
    }

    @Test
    fun classify_WhenStateDoesNotMatch_ShouldBeRejected() {
        val failure = HostedAuthFailure.classify(AuthorizationException.AuthorizationRequestErrors.STATE_MISMATCH)

        assertEquals(HostedAuthFailure.AUTHORIZATION_REJECTED, failure)
    }

    @Test
    fun classify_WhenNetworkFails_ShouldBeNetwork() {
        assertEquals(HostedAuthFailure.NETWORK, HostedAuthFailure.classify(AuthorizationException.GeneralErrors.NETWORK_ERROR))
        assertEquals(HostedAuthFailure.NETWORK, HostedAuthFailure.classify(IOException("reset")))
        assertEquals(StringKey.ERROR_NETWORK, HostedAuthFailure.NETWORK.messageKey)
    }

    @Test
    fun classify_WhenErrorIsUnknown_ShouldBeGeneric() {
        val failure = HostedAuthFailure.classify(IllegalStateException("Token response missing access_token"))

        assertEquals(HostedAuthFailure.UNKNOWN, failure)
        assertEquals(StringKey.LOGIN_ERROR_GENERIC, failure.messageKey)
    }
}
