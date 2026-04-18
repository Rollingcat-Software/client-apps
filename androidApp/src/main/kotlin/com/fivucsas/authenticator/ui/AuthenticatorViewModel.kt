package com.fivucsas.authenticator.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fivucsas.authenticator.storage.TotpVault
import com.fivucsas.authenticator.totp.Base32
import com.fivucsas.authenticator.totp.OtpauthUri
import com.fivucsas.authenticator.totp.TotpAccount
import com.fivucsas.authenticator.totp.TotpAlgorithm
import com.fivucsas.authenticator.totp.TotpGenerator
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AccountCode(
    val account: TotpAccount,
    val code: String,
    val remainingSeconds: Int
)

data class AuthenticatorUiState(
    val accounts: List<AccountCode> = emptyList(),
    val nowEpoch: Long = 0L
)

class AuthenticatorViewModel(private val vault: TotpVault) : ViewModel() {

    private val _state = MutableStateFlow(AuthenticatorUiState())
    val state: StateFlow<AuthenticatorUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                refreshNow()
                delay(1_000L)
            }
        }
    }

    fun refreshNow() {
        val now = System.currentTimeMillis() / 1000L
        val all = vault.getAll()
        _state.update {
            it.copy(
                accounts = all.map { account -> account.toCode(now) },
                nowEpoch = now
            )
        }
    }

    fun addManual(
        issuer: String,
        accountName: String,
        secretBase32: String,
        algorithm: TotpAlgorithm,
        digits: Int,
        period: Int
    ): Result<Unit> {
        val cleanedSecret = secretBase32.trim().replace(" ", "").uppercase()
        val decoded = runCatching { Base32.decode(cleanedSecret) }.getOrElse {
            return Result.failure(IllegalArgumentException("Invalid secret"))
        }
        if (decoded.isEmpty()) {
            return Result.failure(IllegalArgumentException("Invalid secret"))
        }
        val account = TotpAccount(
            id = UUID.randomUUID().toString(),
            issuer = issuer.trim(),
            accountName = accountName.trim(),
            secretBase32 = cleanedSecret,
            algorithm = algorithm.canonical,
            digits = digits,
            period = period,
            createdAt = System.currentTimeMillis()
        )
        vault.add(account)
        refreshNow()
        return Result.success(Unit)
    }

    fun addFromUri(uri: String): Result<Unit> {
        val config = runCatching { OtpauthUri.parse(uri) }.getOrElse {
            return Result.failure(it)
        }
        val account = TotpAccount.fromConfig(
            config = config,
            id = UUID.randomUUID().toString(),
            createdAt = System.currentTimeMillis()
        )
        vault.add(account)
        refreshNow()
        return Result.success(Unit)
    }

    fun delete(id: String) {
        vault.delete(id)
        refreshNow()
    }

    fun rename(id: String, issuer: String, accountName: String) {
        vault.rename(id, issuer.trim(), accountName.trim())
        refreshNow()
    }

    private fun TotpAccount.toCode(now: Long): AccountCode = AccountCode(
        account = this,
        code = TotpGenerator.generate(
            secret = secretBytes(),
            epochSeconds = now,
            algorithm = asAlgorithm(),
            digits = digits,
            period = period
        ),
        remainingSeconds = TotpGenerator.remainingSeconds(now, period)
    )
}
