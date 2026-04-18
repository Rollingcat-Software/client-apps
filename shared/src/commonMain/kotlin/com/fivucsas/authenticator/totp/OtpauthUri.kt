package com.fivucsas.authenticator.totp

data class OtpauthConfig(
    val issuer: String,
    val accountName: String,
    val secretBytes: ByteArray,
    val algorithm: TotpAlgorithm,
    val digits: Int,
    val period: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OtpauthConfig) return false
        return issuer == other.issuer &&
            accountName == other.accountName &&
            secretBytes.contentEquals(other.secretBytes) &&
            algorithm == other.algorithm &&
            digits == other.digits &&
            period == other.period
    }

    override fun hashCode(): Int {
        var result = issuer.hashCode()
        result = 31 * result + accountName.hashCode()
        result = 31 * result + secretBytes.contentHashCode()
        result = 31 * result + algorithm.hashCode()
        result = 31 * result + digits
        result = 31 * result + period
        return result
    }
}

object OtpauthUri {

    fun parse(uri: String): OtpauthConfig {
        val trimmed = uri.trim()
        require(trimmed.startsWith("otpauth://")) {
            "Unsupported scheme; expected otpauth://"
        }
        val afterScheme = trimmed.removePrefix("otpauth://")
        val typeSplit = afterScheme.indexOf('/')
        require(typeSplit > 0) { "Missing otpauth type" }
        val type = afterScheme.substring(0, typeSplit).lowercase()
        require(type == "totp") { "Only totp is supported; got $type" }

        val rest = afterScheme.substring(typeSplit + 1)
        val querySplit = rest.indexOf('?')
        require(querySplit >= 0) { "Missing query parameters" }
        val rawLabel = rest.substring(0, querySplit)
        val query = rest.substring(querySplit + 1)

        val label = urlDecode(rawLabel)
        val (labelIssuer, accountName) = splitLabel(label)
        val params = parseQuery(query)

        val secretEncoded = params["secret"]
            ?: throw IllegalArgumentException("Missing required parameter: secret")
        val secretBytes = Base32.decode(secretEncoded)
        require(secretBytes.isNotEmpty()) { "Decoded secret is empty" }

        val algorithm = TotpAlgorithm.fromString(params["algorithm"])
        val digits = params["digits"]?.toIntOrNull() ?: 6
        require(digits == 6 || digits == 8) { "digits must be 6 or 8, got $digits" }
        val period = params["period"]?.toIntOrNull() ?: 30
        require(period > 0) { "period must be positive, got $period" }

        val issuer = params["issuer"]?.takeIf { it.isNotBlank() } ?: labelIssuer
        val finalAccount = accountName.ifBlank { label }

        return OtpauthConfig(
            issuer = issuer,
            accountName = finalAccount,
            secretBytes = secretBytes,
            algorithm = algorithm,
            digits = digits,
            period = period
        )
    }

    private fun splitLabel(label: String): Pair<String, String> {
        val idx = label.indexOf(':')
        if (idx < 0) return "" to label.trim()
        val issuer = label.substring(0, idx).trim()
        val account = label.substring(idx + 1).trim()
        return issuer to account
    }

    private fun parseQuery(query: String): Map<String, String> {
        if (query.isBlank()) return emptyMap()
        val map = LinkedHashMap<String, String>()
        for (pair in query.split('&')) {
            if (pair.isBlank()) continue
            val eq = pair.indexOf('=')
            if (eq < 0) {
                map[urlDecode(pair)] = ""
            } else {
                map[urlDecode(pair.substring(0, eq))] = urlDecode(pair.substring(eq + 1))
            }
        }
        return map
    }

    private fun urlDecode(value: String): String {
        if (value.isEmpty()) return value
        val sb = StringBuilder()
        val bytes = ArrayList<Byte>()
        var i = 0
        fun flushBytes() {
            if (bytes.isNotEmpty()) {
                sb.append(bytes.toByteArray().decodeToString())
                bytes.clear()
            }
        }
        while (i < value.length) {
            val c = value[i]
            when {
                c == '%' && i + 2 < value.length -> {
                    val hex = value.substring(i + 1, i + 3)
                    val b = hex.toInt(16).toByte()
                    bytes.add(b)
                    i += 3
                }
                c == '+' -> {
                    flushBytes()
                    sb.append(' ')
                    i++
                }
                else -> {
                    flushBytes()
                    sb.append(c)
                    i++
                }
            }
        }
        flushBytes()
        return sb.toString()
    }
}
