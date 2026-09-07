package com.example.ui.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.SafeEmeraldLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.PhantomViewModel
import kotlin.math.sin

@Composable
fun HomeScreen(
    viewModel: PhantomViewModel,
    onNavigateToScan: () -> Unit,
    onNavigateToResult: () -> Unit,
    onNavigateToDemo: () -> Unit,
    onNavigateToUrlIntelligence: () -> Unit,
    onNavigateToPrivacyCenter: () -> Unit,
    onNavigateToFakePayment: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .statusBarsPadding()
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. PHANTOM AI HEADER
        item {
            PhantomTopHeader()
        }

        // 2. SECURITY STATUS
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                SecurityStatusModule()
            }
        }

        // 3. SCAN & TRUST PRIMARY HERO AREA
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                ScanAndTrustHeroControl(onClick = onNavigateToScan)
            }
        }

        // 4. SYSTEM INTELLIGENCE VISUAL STRIP
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                SystemIntelligenceStrip()
            }
        }

        // 5. ADVANCED TOOLS SECTION
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                AdvancedToolsSection(
                    onDemoClick = onNavigateToDemo,
                    onUrlIntelClick = onNavigateToUrlIntelligence,
                    onPrivacyClick = onNavigateToPrivacyCenter,
                    onPaymentCheckClick = onNavigateToFakePayment
                )
            }
        }
    }
}

// ==========================================
// 1. TOP APP HEADER
// ==========================================
@Composable
private fun PhantomTopHeader() {
    val infiniteTransition = rememberInfiniteTransition(label = "header_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "AEGIS",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "DIGITAL DECEPTION PROTECTION",
                color = TextMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.2.sp
            )
        }

        // Compact live system-status indicator
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF121212))
                .border(1.dp, Color(0x1F34D399), RoundedCornerShape(20.dp))
                .padding(horizontal = 11.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(SafeEmerald.copy(alpha = pulseAlpha * 0.28f))
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(SafeEmeraldLight)
                    )
                }
                Text(
                    text = "ACTIVE",
                    color = SafeEmeraldLight,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// ==========================================
// 2. SECURITY STATUS MODULE
// ==========================================
@Composable
private fun SecurityStatusModule() {
    val infiniteTransition = rememberInfiniteTransition(label = "status_anim")
    val sweepProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF141414),
                            Color(0xFF101010)
                        )
                    )
                )
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            // Subtle top-right ambient illumination
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .align(Alignment.TopEnd)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(SafeEmerald.copy(alpha = 0.12f), Color.Transparent)
                        )
                    )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Futuristic micro-radar shield indicator
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF181818))
                        .border(1.dp, Color(0x2234D399), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(36.dp)) {
                        val stroke = 1.2.dp.toPx()
                        drawCircle(
                            color = Color(0x1AFFFFFF),
                            radius = size.minDimension / 2.6f,
                            style = Stroke(stroke)
                        )
                        // Animated sweep arc
                        drawArc(
                            color = SafeEmeraldLight.copy(alpha = 0.65f),
                            startAngle = sweepProgress * 360f,
                            sweepAngle = 60f,
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        tint = SafeEmeraldLight,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SYSTEM SHIELD",
                            color = SafeEmeraldLight,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(SafeEmeraldLight)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ARMED",
                            color = TextTertiary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Protection Active",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.2).sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "On-device deception monitoring",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        letterSpacing = 0.2.sp
                    )
                }

                // Minimal telemetry signal bars
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.height(18.dp)
                ) {
                    val barHeights = listOf(7.dp, 12.dp, 16.dp)
                    barHeights.forEachIndexed { index, height ->
                        val barAlpha = when (index) {
                            0 -> 0.4f
                            1 -> 0.7f
                            else -> 1f
                        }
                        Box(
                            modifier = Modifier
                                .width(2.5.dp)
                                .height(height)
                                .clip(RoundedCornerShape(1.dp))
                                .background(SafeEmeraldLight.copy(alpha = barAlpha))
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. PRIMARY SCAN AREA: SCAN & TRUST
// ==========================================
@Composable
private fun ScanAndTrustHeroControl(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "press_scale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "scan_motion")

    // Continuous smooth radar rotation
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_rotation"
    )

    // Gentle breathing green halo
    val breathingPulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_pulse"
    )

    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.10f,
        targetValue = 0.24f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_alpha"
    )

    Box(
        modifier = Modifier
            .size(246.dp)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("scan_and_trust_button"),
        contentAlignment = Alignment.Center
    ) {
        // Layer 1: Soft breathing emerald radial ambient glow
        Box(
            modifier = Modifier
                .size(246.dp * breathingPulse)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            SafeEmerald.copy(alpha = haloAlpha),
                            Color(0x0510B981),
                            Color.Transparent
                        )
                    )
                )
        )

        // Layer 2: Precision Radar-style Arcs, Concentric Guides & Ticks
        Canvas(modifier = Modifier.size(232.dp)) {
            val centerOffset = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = size.width / 2f - 4.dp.toPx()
            val midRadius = outerRadius - 16.dp.toPx()

            // Outer guideline ring
            drawCircle(
                color = Color(0x14FFFFFF),
                radius = outerRadius,
                center = centerOffset,
                style = Stroke(width = 1.dp.toPx())
            )

            // Middle segmented guideline ring
            drawCircle(
                color = Color(0x1F34D399),
                radius = midRadius,
                center = centerOffset,
                style = Stroke(width = 1.dp.toPx())
            )

            // Precision crosshair tick marks at 0, 90, 180, 270 deg
            val tickLength = 8.dp.toPx()
            val angles = listOf(0.0, Math.PI / 2, Math.PI, 3 * Math.PI / 2)
            angles.forEach { angle ->
                val startX = centerOffset.x + (outerRadius - tickLength) * kotlin.math.cos(angle).toFloat()
                val startY = centerOffset.y + (outerRadius - tickLength) * kotlin.math.sin(angle).toFloat()
                val endX = centerOffset.x + outerRadius * kotlin.math.cos(angle).toFloat()
                val endY = centerOffset.y + outerRadius * kotlin.math.sin(angle).toFloat()
                drawLine(
                    color = SafeEmeraldLight.copy(alpha = 0.5f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 1.5.dp.toPx()
                )
            }

            // Layer 3: Rotating animated scanning ring & thin radar arc
            drawArc(
                color = SafeEmeraldLight.copy(alpha = 0.75f),
                startAngle = rotationAngle,
                sweepAngle = 70f,
                useCenter = false,
                topLeft = Offset(centerOffset.x - outerRadius, centerOffset.y - outerRadius),
                size = Size(outerRadius * 2, outerRadius * 2),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )

            // Counter-rotating subtle micro-arc on middle track
            drawArc(
                color = Color(0x33FFFFFF),
                startAngle = -rotationAngle * 0.7f,
                sweepAngle = 40f,
                useCenter = false,
                topLeft = Offset(centerOffset.x - midRadius, centerOffset.y - midRadius),
                size = Size(midRadius * 2, midRadius * 2),
                style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Layer 4: Deep Metallic Dark Graphite Central Disc
        Box(
            modifier = Modifier
                .size(174.dp)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1C1C1C),
                            Color(0xFF141414),
                            Color(0xFF0D0D0D)
                        )
                    )
                )
                .border(
                    width = 1.2.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x3334D399),
                            Color(0x14FFFFFF),
                            Color(0x2234D399)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Central Scanner Glyph Container with soft glow
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF181818))
                        .border(1.dp, Color(0x2B34D399), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.QrCodeScanner,
                        contentDescription = "Scan & Trust",
                        tint = SafeEmeraldLight,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "SCAN & TRUST",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.4.sp
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "QR • TEXT • IMAGE",
                    color = TextMuted,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.1.sp
                )
            }
        }
    }
}

// ==========================================
// 4. SYSTEM INTELLIGENCE STRIP
// ==========================================
@Composable
private fun SystemIntelligenceStrip() {
    val infiniteTransition = rememberInfiniteTransition(label = "signal_wave")
    val phaseOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF101010))
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Local Sandbox Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(SafeEmeraldLight)
                )
                Text(
                    text = "ISOLATED ENCLAVE",
                    color = SafeEmeraldLight,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Center: Animated Micro-Telemetry Signal Wave
            Canvas(
                modifier = Modifier
                    .width(92.dp)
                    .height(16.dp)
            ) {
                val wavePath = Path()
                val widthPx = size.width
                val midY = size.height / 2f
                val amplitude = size.height * 0.35f

                val steps = 30
                for (i in 0..steps) {
                    val x = (i / steps.toFloat()) * widthPx
                    val progress = i / steps.toFloat()
                    val envelope = sin(progress * Math.PI).toFloat() // window envelope to taper ends
                    val y = midY + sin(progress * 4 * Math.PI + phaseOffset).toFloat() * amplitude * envelope

                    if (i == 0) {
                        wavePath.moveTo(x, y)
                    } else {
                        wavePath.lineTo(x, y)
                    }
                }

                drawPath(
                    path = wavePath,
                    color = SafeEmeraldLight.copy(alpha = 0.7f),
                    style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Right: Zero Cloud Retention Telemetry
            Text(
                text = "ON-DEVICE ENGINE",
                color = TextTertiary,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

// ==========================================
// 5. ADVANCED TOOLS SECTION
// ==========================================
@Composable
private fun AdvancedToolsSection(
    onDemoClick: () -> Unit,
    onUrlIntelClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onPaymentCheckClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ADVANCED PROTECTION TOOLS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.4.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Row 1: Threat Simulator & URL Intelligence
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CyberToolCard(
                title = "Threat Simulator",
                subtitle = "Synthetic Testing",
                statusLabel = "ARMED",
                statusColor = SafeEmeraldLight,
                icon = Icons.Filled.PlayCircle,
                iconTint = SafeEmeraldLight,
                modifier = Modifier.weight(1f),
                testTag = "tool_threat_simulator",
                onClick = onDemoClick
            )

            CyberToolCard(
                title = "URL Intelligence",
                subtitle = "Deep Domain Check",
                statusLabel = "ACTIVE",
                statusColor = Color(0xFF38BDF8),
                icon = Icons.Filled.Language,
                iconTint = Color(0xFF38BDF8),
                modifier = Modifier.weight(1f),
                testTag = "tool_url_intel",
                onClick = onUrlIntelClick
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 2: Privacy Center & Payment Check
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CyberToolCard(
                title = "Privacy Center",
                subtitle = "On-Device Policy",
                statusLabel = "VERIFIED",
                statusColor = SafeEmeraldLight,
                icon = Icons.Filled.Security,
                iconTint = SafeEmeraldLight,
                modifier = Modifier.weight(1f),
                testTag = "tool_privacy_center",
                onClick = onPrivacyClick
            )

            CyberToolCard(
                title = "Payment Check",
                subtitle = "Receipt Integrity",
                statusLabel = "READY",
                statusColor = Color(0xFF38BDF8),
                icon = Icons.Filled.Payment,
                iconTint = Color(0xFF38BDF8),
                modifier = Modifier.weight(1f),
                testTag = "tool_payment_check",
                onClick = onPaymentCheckClick
            )
        }
    }
}

@Composable
private fun CyberToolCard(
    title: String,
    subtitle: String,
    statusLabel: String,
    statusColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    testTag: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "tool_press_scale"
    )

    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .border(1.dp, Color(0x18FFFFFF), RoundedCornerShape(16.dp))
            .testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131313)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF161616),
                            Color(0xFF111111)
                        )
                    )
                )
                .padding(14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(iconTint.copy(alpha = 0.12f))
                            .border(1.dp, iconTint.copy(alpha = 0.25f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Micro status pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Text(
                            text = statusLabel,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            letterSpacing = 0.8.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = title,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = subtitle,
                    fontSize = 10.5.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

