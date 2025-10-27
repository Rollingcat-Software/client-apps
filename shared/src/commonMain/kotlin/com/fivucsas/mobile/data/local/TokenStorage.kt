package com.fivucsas.mobile.data.local

interface TokenStorage {
    fun saveToken(token: String)
    fun getToken(): String?
    fun clearToken()
}
