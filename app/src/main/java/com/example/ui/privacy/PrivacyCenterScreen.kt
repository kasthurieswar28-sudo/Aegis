package com.example.ui.privacy

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PermDeviceInformation
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberSky
import com.example.ui.theme.ElectricAmber
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.SafeEmeraldLight
import com.example.ui.theme.SlateCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun PrivacyCenterScreen(
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .testTag("privacy_center_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "PRIVACY & DATA POLICY",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Zero-Knowledge On-Device Security Architecture",
                        color = CyberSky,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Core Philosophy Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SafeEmerald.copy(alpha = 0.08f))
                    .border(1.dp, SafeEmerald.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = SafeEmeraldLight, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CORE PRIVACY PRINCIPLE",
                            color = SafeEmeraldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Aegis does not monitor your private communications in the background. It analyzes only the specific content or QR codes you explicitly submit. All baseline threat heuristic evaluations occur directly on your device.",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // Section: On-Device Processing
        item {
            PrivacySectionCard(
                title = "WHAT RUNS 100% ON-DEVICE",
                subtitle = "Local Hardware Sandbox",
                icon = Icons.Filled.Devices,
                iconTint = CyberCyan,
                points = listOf(
                    "ML Kit Text Recognition: Visual optical characters are decoded entirely using local device neural models without sending image pixels over the network.",
                    "Heuristic Signal Extractor: Regular expressions, token search, and coercion pattern algorithms execute strictly in-memory.",
                    "URL Structural Engine: Hostname parsing, Punycode inspection, and brand lookalike detection run offline.",
                    "Local Encrypted SQLite Storage: Scan history is stored strictly in your phone's internal app data sandbox."
                )
            )
        }

        // Section: Cloud AI & Redaction
        item {
            PrivacySectionCard(
                title = "WHAT IS SENT TO CLOUD AI",
                subtitle = "Optional Gemini Reasoning",
                icon = Icons.Filled.CloudQueue,
                iconTint = CyberSky,
                points = listOf(
                    "When cloud reasoning is enabled, only high-level threat indicators and sanitized text prompts are sent to the Gemini API.",
                    "Authentication code scrubbers redact sensitive numbers (e.g. 6-digit OTP codes or credit card numbers) before any prompt is assembled.",
                    "No personal device identifiers (IMEI, advertising ID, or phone number) are transmitted.",
                    "If offline or if no API key is set, the on-device rule engine automatically provides full analysis and fallback answers."
                )
            )
        }

        // Section: Data Retention & Instant Deletion
        item {
            PrivacySectionCard(
                title = "DATA RETENTION & CONTROL",
                subtitle = "Complete User Autonomy",
                icon = Icons.Filled.Delete,
                iconTint = ElectricAmber,
                points = listOf(
                    "You retain total control over your security log history.",
                    "Raw passwords, full bank account numbers, or OTP digits are never stored in history.",
                    "You can delete individual scans or tap 'Clear All History' in the History screen to instantly purge all stored records.",
                    "Uninstalling the application immediately removes all database files from the operating system."
                )
            )
        }

        // Section: System Permissions
        item {
            PrivacySectionCard(
                title = "PERMISSIONS EXPLAINED",
                subtitle = "Minimal Principle of Least Privilege",
                icon = Icons.Filled.PermDeviceInformation,
                iconTint = SafeEmeraldLight,
                points = listOf(
                    "CAMERA (android.permission.CAMERA): Required only while active on the Camera tab to read QR codes and text frames.",
                    "INTERNET (android.permission.INTERNET): Required to connect to Gemini API for deep cognitive scam reasoning.",
                    "VIBRATE (android.permission.VIBRATE): Used for brief haptic alerts when a Critical threat is identified.",
                    "NO MICROPHONE: Zero audio recording permissions. Voice Shield has been completely removed from the app codebase.",
                    "NO STORAGE PERMISSION: Uses modern Android Photo Picker which requires zero storage permissions."
                )
            )
        }
    }
}

@Composable
private fun PrivacySectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    points: List<String>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = SlateCard),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.12f))
                        .border(1.dp, iconTint.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = subtitle,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            points.forEach { point ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(iconTint)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = point,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}
