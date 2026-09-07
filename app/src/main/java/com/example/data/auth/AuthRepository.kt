package com.example.data.auth

import android.content.Context
import com.example.domain.model.AuthProviderType
import com.example.domain.model.AuthUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepository(private val context: Context) {

    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    fun signInAsGuest(): Result<AuthUser> {
        val user = _currentUser.value ?: createLocalGuestUser()
        _currentUser.value = user
        return Result.success(user)
    }

    private fun createLocalGuestUser(): AuthUser {
        val randomHex = (1000..9999).random()
        return AuthUser(
            uid = "guest_$randomHex",
            email = null,
            displayName = "Guest Analyst #$randomHex",
            photoUrl = null,
            isAnonymous = true,
            provider = AuthProviderType.GUEST
        )
    }

    fun signOut() {
        _currentUser.value = null
    }

    companion object {
        @Volatile
        private var instance: AuthRepository? = null

        fun getInstance(context: Context): AuthRepository {
            return instance ?: synchronized(this) {
                instance ?: AuthRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
