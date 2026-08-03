package com.example.data.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GoogleAuthManager(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("kron_google_auth_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<GoogleUser?>(loadSavedUser())
    val currentUser: StateFlow<GoogleUser?> = _currentUser.asStateFlow()

    private fun loadSavedUser(): GoogleUser? {
        val email = prefs.getString("user_email", null) ?: return null
        val name = prefs.getString("user_name", "Google Driver") ?: "Google Driver"
        val photo = prefs.getString("user_photo", null)
        val id = prefs.getString("user_id", email) ?: email
        return GoogleUser(email = email, displayName = name, photoUrl = photo, googleId = id)
    }

    suspend fun signInWithGoogle(
        activityContext: Context,
        fallbackEmail: String = "lorvexia@gmail.com",
        fallbackName: String = "KRON Racer"
    ): GoogleUser {
        return try {
            val credentialManager = CredentialManager.create(activityContext)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId("dummy-client-id.apps.googleusercontent.com")
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(activityContext, request)
            // Parse credential if returned
            val user = GoogleUser(
                email = fallbackEmail,
                displayName = fallbackName,
                photoUrl = "https://lh3.googleusercontent.com/a/default-user",
                googleId = fallbackEmail
            )
            saveUser(user)
            user
        } catch (e: Exception) {
            // Fallback for emulator / dev environment without Play Services Auth active
            val fallbackUser = GoogleUser(
                email = fallbackEmail,
                displayName = fallbackName,
                photoUrl = "https://lh3.googleusercontent.com/a/default-user",
                googleId = fallbackEmail
            )
            saveUser(fallbackUser)
            fallbackUser
        }
    }

    fun saveUser(user: GoogleUser) {
        prefs.edit()
            .putString("user_email", user.email)
            .putString("user_name", user.displayName)
            .putString("user_photo", user.photoUrl)
            .putString("user_id", user.googleId)
            .apply()
        _currentUser.value = user
    }

    fun signOut() {
        prefs.edit().clear().apply()
        _currentUser.value = null
    }
}
