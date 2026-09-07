package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
) {
    object Home : Screen("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Scanner : Screen("scanner", "Scan", Icons.Filled.QrCodeScanner, Icons.Outlined.QrCodeScanner)
    object History : Screen("history", "History", Icons.Filled.History, Icons.Outlined.History)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)

    // Non-tab destinations
    object Landing : Screen("landing", "Authentication", Icons.Filled.Shield, Icons.Outlined.Shield)
    object Result : Screen("result", "Risk Analysis", Icons.Filled.Shield, Icons.Outlined.Shield)
    object Explanation : Screen("explanation", "Attack Explanation", Icons.Filled.Shield, Icons.Outlined.Shield)
    object DemoMode : Screen("demo_mode", "Threat Scenarios", Icons.Filled.PlayCircle, Icons.Outlined.PlayCircle)
    object PrivacyCenter : Screen("privacy_center", "Privacy Center", Icons.Filled.Security, Icons.Outlined.Security)
    object UrlIntelligence : Screen("url_intelligence", "URL Intelligence", Icons.Filled.Language, Icons.Outlined.Language)
    object FakePayment : Screen("fakepayment", "Payment Integrity", Icons.Filled.Shield, Icons.Outlined.Shield)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Scanner,
    Screen.History,
    Screen.Settings
)
