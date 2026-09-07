package com.example

import com.example.domain.model.AuthProviderType
import com.example.domain.model.AuthUser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthTest {

    @Test
    fun `test guest user default attributes`() {
        val guest = AuthUser(
            uid = "guest_1234",
            email = null,
            displayName = "Guest Analyst #1234",
            isAnonymous = true,
            provider = AuthProviderType.GUEST
        )

        assertTrue(guest.isAnonymous)
        assertEquals(AuthProviderType.GUEST, guest.provider)
        assertEquals("Guest Session", guest.provider.displayName)
    }

    @Test
    fun `test email authenticated user`() {
        val user = AuthUser(
            uid = "email_user_999",
            email = "analyst@aegis.security",
            displayName = "Senior Analyst",
            isAnonymous = false,
            provider = AuthProviderType.EMAIL
        )

        assertFalse(user.isAnonymous)
        assertEquals("analyst@aegis.security", user.email)
        assertEquals(AuthProviderType.EMAIL, user.provider)
    }

    @Test
    fun `test google authenticated user`() {
        val googleUser = AuthUser(
            uid = "google_user_555",
            email = "agent@google.com",
            displayName = "Cyber Agent",
            isAnonymous = false,
            provider = AuthProviderType.GOOGLE
        )

        assertFalse(googleUser.isAnonymous)
        assertEquals(AuthProviderType.GOOGLE, googleUser.provider)
    }
}
