package com.fivucsas.shared.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Response of `POST /auth/mfa/puzzle/session` (bio CREATE + identity additions). */
@Serializable
data class PuzzleSessionCreateResponse(
    @SerialName("session_id") val sessionId: String,
    val challenges: List<PuzzleIssuedChallenge> = emptyList(),
    @SerialName("trace_contract") val traceContract: String? = null,
    @SerialName("trace_face_indices") val traceFaceIndices: List<Int>? = null,
    /** Base64url (no padding) single-use nonce; present only when attested binding applies. */
    @SerialName("attestation_challenge") val attestationChallenge: String? = null,
    @SerialName("attestation_payload_version") val attestationPayloadVersion: Int? = null
)

@Serializable
data class PuzzleIssuedChallenge(
    val action: String,
    val params: JsonObject? = null
)

/** Per-challenge SUBMIT verdict (UX feedback; the gate is the MFA step). */
@Serializable
data class PuzzleChallengeVerdict(
    val verified: Boolean = false,
    val action: String? = null,
    @SerialName("reason_code") val reasonCode: String? = null
)

/**
 * The puzzle-session MFA proxy (identity-core-api AuthController) and the PUZZLE
 * step submission with a structured `data` object (the generic MfaStepRequest
 * carries only string values, which cannot hold traces / attestation bundles).
 */
class PuzzleSessionApi(private val client: HttpClient) {

    suspend fun create(sessionToken: String): PuzzleSessionCreateResponse {
        val response = client.post("auth/mfa/puzzle/session") {
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject { put("sessionToken", sessionToken) })
        }
        if (!response.status.isSuccess()) {
            throw Exception("${response.status.value} ${response.bodyAsText()}")
        }
        return response.body()
    }

    /** [submission] = `{action, metrics, start_timestamp_ms, end_timestamp_ms, confidence, trace}`. */
    suspend fun submitChallenge(sessionToken: String, puzzleSessionId: String, submission: JsonObject):
        PuzzleChallengeVerdict {
        val body = JsonObject(submission + ("sessionToken" to JsonPrimitive(sessionToken)))
        val response = client.post("auth/mfa/puzzle/session/$puzzleSessionId/challenge") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        if (!response.status.isSuccess()) {
            throw Exception("${response.status.value} ${response.bodyAsText()}")
        }
        return response.body()
    }

    /** `POST /auth/mfa/step` with method PUZZLE and the structured [data]; returns the raw JSON. */
    suspend fun verifyStep(sessionToken: String, data: JsonObject): JsonObject {
        val response = client.post("auth/mfa/step") {
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject {
                put("sessionToken", sessionToken)
                put("method", "PUZZLE")
                put("data", data)
            })
        }
        if (!response.status.isSuccess()) {
            throw Exception("${response.status.value} ${response.bodyAsText()}")
        }
        return response.body()
    }
}
