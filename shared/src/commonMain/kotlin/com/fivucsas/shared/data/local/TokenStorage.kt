package com.fivucsas.shared.data.local

interface TokenStorage {
    fun saveToken(token: String)
    fun getToken(): String?
    fun clearToken()
    fun saveRefreshToken(token: String) {}
    fun getRefreshToken(): String? = null
    fun clearRefreshToken() {}
    fun saveRole(role: String)
    fun getRole(): String?
    fun clearRole()
    fun saveUserName(name: String) {}
    fun getUserName(): String? = null
    fun clearUserName() {}
    fun saveUserEmail(email: String) {}
    fun getUserEmail(): String? = null
    fun clearUserEmail() {}
    fun saveUserId(id: String) {}
    fun getUserId(): String? = null
    fun clearUserId() {}
    fun saveTenantId(tenantId: String) {}
    fun getTenantId(): String? = null
    fun clearTenantId() {}
}
