package com.example.domain.model

enum class AuthProviderType(val displayName: String) {
    EMAIL("Email / Password"),
    GOOGLE("Google Account"),
    GUEST("Guest Session")
}

data class AuthUser(
    val uid: String,
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val isAnonymous: Boolean = false,
    val provider: AuthProviderType = AuthProviderType.GUEST,
    val createdAt: Long = System.currentTimeMillis()
)
