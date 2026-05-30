package com.fivucsas.shared.presentation.state

import com.fivucsas.shared.data.remote.dto.AvailableMethodDto
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Immutable hand-off payload carried from Login → MfaFlow over the
 * navigation route, instead of read back off a shared ViewModel instance.
 *
 * History: before v5.2.2 the MfaFlow destination read `mfaSessionToken`
 * off a `koinInject<LoginViewModel>()`. Because `LoginViewModel` is a
 * Koin *factory*, that injection handed back a BRAND-NEW instance whose
 * token was `null`, so the flow immediately bounced the user back to the
 * Login screen — they could never complete MFA. Passing the data through
 * the route makes the hand-off explicit and keeps `LoginViewModel` a
 * factory (no global-singleton footgun, no auto-bounce on a returning
 * singleton).
 *
 * `@Serializable` so the whole payload (including the structured
 * `methods` list) can be JSON-encoded into a single URL-safe route
 * argument via [encode] and recovered with [decode].
 */
@Serializable
data class MfaHandoff(
    val sessionToken: String,
    val methods: List<AvailableMethodDto> = emptyList(),
    val step: Int = 1,
    val total: Int = 1
) {
    fun encode(): String = json.encodeToString(this)

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

        /** Recover a payload from its [encode]d form; `null` if malformed. */
        fun decode(raw: String?): MfaHandoff? {
            if (raw.isNullOrBlank()) return null
            return runCatching { json.decodeFromString<MfaHandoff>(raw) }.getOrNull()
        }
    }
}
