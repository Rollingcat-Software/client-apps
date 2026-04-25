package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.MfaSwitchMethodRequest
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * AuthApiImpl tests for the three PR #25 endpoints:
 *  - DELETE /auth/mfa/session/{sessionToken}
 *  - POST   /auth/mfa/switch-method  (200 + 409 paths)
 *
 * Verifies the request method, path, and body without spinning up a real
 * server. Uses Ktor's MockEngine.
 */
class AuthApiImplMfaPr25Test {

    private val json = Json { ignoreUnknownKeys = true }

    private fun api(handler: io.ktor.client.engine.mock.MockRequestHandler): AuthApiImpl {
        val engine = MockEngine(handler)
        val client = HttpClient(engine) {
            install(ContentNegotiation) { json(json) }
            defaultRequest {
                url("https://test.example.com/api/v1/")
            }
        }
        return AuthApiImpl(client)
    }

    @Test
    fun `cancelMfaSession sends DELETE with sessionToken in path`() = runTest {
        var capturedMethod: HttpMethod? = null
        var capturedUrl: String? = null

        val sut = api { req ->
            capturedMethod = req.method
            capturedUrl = req.url.toString()
            respond(content = "", status = HttpStatusCode.NoContent)
        }

        sut.cancelMfaSession("session-abc-123")

        assertEquals(HttpMethod.Delete, capturedMethod)
        assertNotNull(capturedUrl)
        assertTrue(
            capturedUrl!!.endsWith("auth/mfa/session/session-abc-123"),
            "Expected path to end with auth/mfa/session/session-abc-123 but was $capturedUrl"
        )
    }

    @Test
    fun `cancelMfaSession swallows 404 NotFound`() = runTest {
        val sut = api { _ ->
            respond(content = "{\"error\":\"NOT_FOUND\"}", status = HttpStatusCode.NotFound)
        }
        // Should not throw — already-expired sessions are a soft success.
        sut.cancelMfaSession("expired-token")
    }

    @Test
    fun `cancelMfaSession throws on 500`() = runTest {
        val sut = api { _ ->
            respond(content = "boom", status = HttpStatusCode.InternalServerError)
        }
        try {
            sut.cancelMfaSession("any")
            fail("Expected exception on 500")
        } catch (_: Exception) {
            // expected
        }
    }

    @Test
    fun `switchMfaMethod posts JSON body with sessionToken and method`() = runTest {
        var capturedMethod: HttpMethod? = null
        var capturedUrl: String? = null
        var capturedBody: String? = null

        val sut = api { req ->
            capturedMethod = req.method
            capturedUrl = req.url.toString()
            capturedBody = req.body.toByteArray().decodeToString()
            respond(
                content = """
                    {
                      "status": "METHOD_SWITCHED",
                      "currentStep": 2,
                      "totalSteps": 3,
                      "expectedMethod": "EMAIL_OTP",
                      "availableMethods": [],
                      "alternativeMethods": [],
                      "completedMethods": ["TOTP"]
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val resp = sut.switchMfaMethod(
            MfaSwitchMethodRequest(sessionToken = "tok-1", method = "EMAIL_OTP")
        )

        assertEquals(HttpMethod.Post, capturedMethod)
        assertTrue(
            capturedUrl!!.endsWith("auth/mfa/switch-method"),
            "Expected url to end with auth/mfa/switch-method but was $capturedUrl"
        )
        assertNotNull(capturedBody)
        assertTrue("\"sessionToken\":\"tok-1\"" in capturedBody!!)
        assertTrue("\"method\":\"EMAIL_OTP\"" in capturedBody!!)

        assertEquals("METHOD_SWITCHED", resp.status)
        assertEquals(2, resp.currentStep)
        assertEquals("EMAIL_OTP", resp.expectedMethod)
        assertEquals(listOf("TOTP"), resp.completedMethods)
        assertNull(resp.errorCode)
    }

    @Test
    fun `switchMfaMethod returns 409 envelope without throwing`() = runTest {
        val sut = api { _ ->
            respond(
                content = """
                    {
                      "status": "ERROR",
                      "errorCode": "METHOD_ALREADY_USED",
                      "message": "You already used this method"
                    }
                """.trimIndent(),
                status = HttpStatusCode.Conflict,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val resp = sut.switchMfaMethod(
            MfaSwitchMethodRequest(sessionToken = "tok", method = "TOTP")
        )

        assertEquals("METHOD_ALREADY_USED", resp.errorCode)
        assertEquals("You already used this method", resp.message)
    }

    @Test
    fun `switchMfaMethod throws on 500`() = runTest {
        val sut = api { _ ->
            respond(content = "kaboom", status = HttpStatusCode.InternalServerError)
        }
        try {
            sut.switchMfaMethod(MfaSwitchMethodRequest("tok", "TOTP"))
            fail("Expected exception on 500")
        } catch (_: Exception) {
            // expected
        }
    }
}

