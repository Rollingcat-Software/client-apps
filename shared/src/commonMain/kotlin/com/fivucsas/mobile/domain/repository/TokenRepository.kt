package com.fivucsas.mobile.domain.repository

interface TokenRepository {
    fun getToken(): String?
    fun saveToken(token: String)
    fun clearToken()
    fun hasToken(): Boolean
}
