package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.QrLoginApproveRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Pre-demo 2026-06-03 — regression guard for the "approved on phone, browser
 * waits forever" bug (#1 + approve-login).
 *
 * QrLoginApiImpl.approveSession and ApproveLoginApiImpl.decide previously POSTed
 * WITHOUT reading the response, so a non-2xx (e.g. an expired bearer → 401/403)
 * was silently swallowed: the phone flipped to APPROVED while the server session
 * stayed PENDING and the originating (web) login never advanced. Both now throw
 * on a non-success status so the repository/ViewModel surface the real error.
 */
class QrApproveLoginStatusCheckTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun client(handler: io.ktor.client.engine.mock.MockRequestHandler): HttpClient {
        val engine = MockEngine(handler)
        return HttpClient(engine) {
            install(ContentNegotiation) { json(json) }
            defaultRequest { url("https://test.example.com/api/v1/") }
        }
    }

    // ---- QR approve ----

    @Test
    fun `qr approveSession succeeds on 200`() = runTest {
        val api = QrLoginApiImpl(client { _ -> respond("", HttpStatusCode.OK) })
        // Should not throw.
        api.approveSession("sess-1", QrLoginApproveRequestDto(approverPlatform = "MOBILE"))
    }

    @Test
    fun `qr approveSession throws on 401 (expired bearer)`() = runTest {
        val api = QrLoginApiImpl(client { _ ->
            respond(
                content = "{\"error\":\"UNAUTHORIZED\"}",
                status = HttpStatusCode.Unauthorized,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        })
        try {
            api.approveSession("sess-1", QrLoginApproveRequestDto(approverPlatform = "MOBILE"))
            fail("Expected approveSession to throw on 401 instead of swallowing it")
        } catch (e: Exception) {
            assertTrue("401" in (e.message ?: ""), "Status should surface in message: ${e.message}")
        }
    }

    @Test
    fun `qr approveSession throws on 403`() = runTest {
        val api = QrLoginApiImpl(client { _ -> respond("forbidden", HttpStatusCode.Forbidden) })
        try {
            api.approveSession("sess-1", QrLoginApproveRequestDto(approverPlatform = "MOBILE"))
            fail("Expected approveSession to throw on 403")
        } catch (_: Exception) {
            // expected
        }
    }

    // ---- approve-login decide ----

    @Test
    fun `approve-login decide succeeds on 200`() = runTest {
        val api = ApproveLoginApiImpl(client { _ -> respond("", HttpStatusCode.OK) })
        // Should not throw.
        api.decide("sess-2", "allow", "42")
    }

    @Test
    fun `approve-login decide throws on 401 (expired bearer)`() = runTest {
        val api = ApproveLoginApiImpl(client { _ -> respond("nope", HttpStatusCode.Unauthorized) })
        try {
            api.decide("sess-2", "allow", "42")
            fail("Expected decide to throw on 401 instead of swallowing it")
        } catch (e: Exception) {
            assertTrue("401" in (e.message ?: ""), "Status should surface in message: ${e.message}")
        }
    }

    @Test
    fun `approve-login decide throws on 403`() = runTest {
        val api = ApproveLoginApiImpl(client { _ -> respond("forbidden", HttpStatusCode.Forbidden) })
        try {
            api.decide("sess-2", "deny", null)
            fail("Expected decide to throw on 403")
        } catch (_: Exception) {
            // expected
        }
    }
}
