package com.fivucsas.authenticator.totp

import kotlinx.serialization.Serializable

@Serializable
data class TotpAccount(
    val id: String,
    val issuer: String,
    val accountName: String,
    val secretBase32: String,
    val algorithm: String = TotpAlgorithm.SHA1.canonical,
    val digits: Int = 6,
    val period: Int = 30,
    val createdAt: Long
) {
    fun asAlgorithm(): TotpAlgorithm = TotpAlgorithm.fromString(algorithm)
    fun secretBytes(): ByteArray = Base32.decode(secretBase32)

    companion object {
        fun fromConfig(config: OtpauthConfig, id: String, createdAt: Long): TotpAccount =
            TotpAccount(
                id = id,
                issuer = config.issuer,
                accountName = config.accountName,
                secretBase32 = Base32.encode(config.secretBytes),
                algorithm = config.algorithm.canonical,
                digits = config.digits,
                period = config.period,
                createdAt = createdAt
            )
    }
}
