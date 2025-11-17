package com.fivucsas.shared.data.local

interface TokenStore {
    fun get(): String?
    fun save(token: String)
    fun clear()
}
