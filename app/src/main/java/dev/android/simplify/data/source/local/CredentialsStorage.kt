package dev.android.simplify.data.source.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class CredentialsStorage(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFERENCES_FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveCredentials(email: String, password: String) {
        sharedPreferences.edit().apply {
            putString(KEY_EMAIL, email)
            putString(KEY_PASSWORD, password)
            apply()
        }
    }

    fun getSavedEmail(): String? {
        return sharedPreferences.getString(KEY_EMAIL, null)
    }

    fun getSavedPassword(): String? {
        return sharedPreferences.getString(KEY_PASSWORD, null)
    }

    fun hasSavedCredentials(): Boolean {
        return sharedPreferences.contains(KEY_EMAIL) && sharedPreferences.contains(KEY_PASSWORD)
    }

    fun clearCredentials() {
        sharedPreferences.edit().apply {
            remove(KEY_EMAIL)
            remove(KEY_PASSWORD)
            apply()
        }
    }

    companion object {
        private const val PREFERENCES_FILE_NAME = "dev.android.simplify.credentials"
        private const val KEY_EMAIL = "email"
        private const val KEY_PASSWORD = "password"
    }
}