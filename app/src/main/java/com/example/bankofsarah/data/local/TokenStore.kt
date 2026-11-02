package com.example.bankofsarah.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "session")

class TokenStore(private val context: Context) {

    private val tokenKey = stringPreferencesKey("access_token")

    val token: Flow<String?> = context.dataStore.data.map { prefs -> prefs[tokenKey] }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs -> prefs[tokenKey] = token }
    }

    suspend fun clearToken() {
        context.dataStore.edit { prefs -> prefs.remove(tokenKey) }
    }
}