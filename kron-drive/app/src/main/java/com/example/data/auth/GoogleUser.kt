package com.example.data.auth

data class GoogleUser(
    val email: String,
    val displayName: String,
    val photoUrl: String? = null,
    val googleId: String = email
)
