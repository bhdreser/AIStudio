package com.example.data.local

data class UserProfile(
    val userId: String,
    val displayName: String,
    val email: String,
    val photoUrl: String? = null,
    val isLoggedIn: Boolean = true,
    val lastSyncTime: String = ""
)
