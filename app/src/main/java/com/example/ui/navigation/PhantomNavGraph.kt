package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.ui.auth.LandingAuthScreen
import com.example.ui.demo.DemoScreen
import com.example.ui.explanation.AttackExplanationScreen
import com.example.ui.fakepayment.FakePaymentScreen
import com.example.ui.history.HistoryScreen
import com.example.ui.home.HomeScreen
import com.example.ui.privacy.PrivacyCenterScreen
import com.example.ui.result.ResultScreen
import com.example.ui.scanner.ScannerScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.NavBg
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SafeEmeraldLight
import com.example.ui.theme.SlateCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.url.UrlIntelligenceScreen
import com.example.ui.viewmodel.PhantomViewModel

@Composable
fun PhantomApp(
    navController: NavHostController,
    viewModel: PhantomViewModel
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Show bottom bar on primary tabs
    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Scanner.route,
        Screen.History.route,
        Screen.Settings.route
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg),
        containerColor = ObsidianBg,
        bottomBar = {
            if (showBottomBar) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(Color(0xF0121212))
                            .border(
                                width = 1.dp,
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0x1AFFFFFF),
                                        Color(0x3334D399),
                                        Color(0x1AFFFFFF)
                                    )
                                ),
                                shape = RoundedCornerShape(28.dp)
                            )
                            .testTag("bottom_nav_bar")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            bottomNavItems.forEach { screen ->
                                val selected = currentRoute == screen.route
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(22.dp))
                                        .background(
                                            if (selected) Color(0x2234D399) else Color.Transparent
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (selected) Color(0x3834D399) else Color.Transparent,
                                            shape = RoundedCornerShape(22.dp)
                                        )
                                        .clickable {
                                            if (currentRoute != screen.route) {
                                                navController.navigate(screen.route) {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        }
                                        .padding(
                                            horizontal = if (selected) 16.dp else 12.dp,
                                            vertical = 8.dp
                                        )
                                        .testTag("nav_item_${screen.route}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (selected) screen.activeIcon else screen.inactiveIcon,
                                            contentDescription = screen.title,
                                            tint = if (selected) SafeEmeraldLight else TextTertiary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        if (selected) {
                                            Text(
                                                text = screen.title.uppercase(),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary,
                                                letterSpacing = 0.8.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Landing.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Landing.route) {
                LandingAuthScreen(
                    viewModel = viewModel,
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Landing.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToScan = { navController.navigate(Screen.Scanner.route) },
                    onNavigateToResult = { navController.navigate(Screen.Result.route) },
                    onNavigateToDemo = { navController.navigate(Screen.DemoMode.route) },
                    onNavigateToUrlIntelligence = { navController.navigate(Screen.UrlIntelligence.route) },
                    onNavigateToPrivacyCenter = { navController.navigate(Screen.PrivacyCenter.route) },
                    onNavigateToFakePayment = { navController.navigate(Screen.FakePayment.route) }
                )
            }

            composable(Screen.Scanner.route) {
                ScannerScreen(
                    viewModel = viewModel,
                    onNavigateToResult = { navController.navigate(Screen.Result.route) }
                )
            }

            composable(Screen.Result.route) {
                ResultScreen(
                    viewModel = viewModel,
                    onNavigateToExplanation = { navController.navigate(Screen.Explanation.route) },
                    onScanAnother = {
                        viewModel.clearLiveDetection()
                        navController.navigate(Screen.Scanner.route) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Explanation.route) {
                AttackExplanationScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    viewModel = viewModel,
                    onNavigateToResult = { navController.navigate(Screen.Result.route) }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel,
                    onSignOut = {
                        navController.navigate(Screen.Landing.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.DemoMode.route) {
                DemoScreen(
                    viewModel = viewModel,
                    onNavigateToResult = { navController.navigate(Screen.Result.route) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.UrlIntelligence.route) {
                UrlIntelligenceScreen(
                    viewModel = viewModel,
                    onNavigateToResult = { navController.navigate(Screen.Result.route) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.PrivacyCenter.route) {
                PrivacyCenterScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.FakePayment.route) {
                FakePaymentScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToResult = { navController.navigate(Screen.Result.route) }
                )
            }
        }
    }
}
