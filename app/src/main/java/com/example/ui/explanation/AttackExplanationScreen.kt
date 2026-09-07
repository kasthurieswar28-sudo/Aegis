package com.example.ui.explanation

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AttackStep
import com.example.domain.model.RiskClassification
import com.example.ui.components.CyberCard
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CriticalViolet
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanDim
import com.example.ui.theme.CyberSky
import com.example.ui.theme.ElectricAmber
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.SlateCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.ThreatRed
import com.example.ui.viewmodel.PhantomViewModel

@Composable
fun AttackExplanationScreen(
    viewModel: PhantomViewModel,
    onBack: () -> Unit
) {
    val result by viewModel.currentResult.collectAsState()

    if (result == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ObsidianBg),
            contentAlignment = Alignment.Center
        ) {
            Text("No active attack explanation available.", color = TextSecondary)
        }
        return
    }

    val current = result!!

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .testTag("attack_explanation_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Top Header
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
                        text = "ATTACK REASONING & CHAIN",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Psychological Vector Breakdown",
                        color = CyberSky,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // PHANTOM Insight Card
        item {
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SafeEmerald.copy(alpha = 0.12f))
                                    .border(1.dp, SafeEmerald.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Psychology,
                                    contentDescription = null,
                                    tint = SafeEmerald,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "AEGIS INSIGHT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SafeEmerald,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "\"${current.phantomInsight}\"",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = TextPrimary,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }

        // Section Title: Attack Chain
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Text(
                    text = "STEP-BY-STEP ATTACK CHAIN",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "How the attacker engineers your behavioral compliance:",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }

        // Attack Chain Steps
        itemsIndexed(current.attackChain) { index, step ->
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                AttackChainStepItem(
                    step = step,
                    isLast = index == current.attackChain.size - 1,
                    index = index + 1
                )
            }
        }

        // Counter-Measures & Safeguards
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(
                    text = "SAFEGUARDS & IMMEDIATE COUNTER-MEASURES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SafeEmerald,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                CyberCard(borderColor = SafeEmerald.copy(alpha = 0.35f)) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SafeguardRow(
                            stepNumber = "1",
                            action = "Never act inside the attacker's timeline",
                            description = "Artificial deadlines are designed to prevent independent thinking. Take a breath and pause."
                        )
                        SafeguardRow(
                            stepNumber = "2",
                            action = "Verify via independent official channels",
                            description = "Close the link/message. Open your official banking app or call the verified phone number printed on the back of your card."
                        )
                        SafeguardRow(
                            stepNumber = "3",
                            action = "Do not share OTP or scan QR to receive money",
                            description = "Scanning a QR code in payment apps is strictly for sending money out, never for receiving refunds."
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AttackChainStepItem(
    step: AttackStep,
    isLast: Boolean,
    index: Int
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(28.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isLast) ThreatRed else CyberCyanDim)
                    .border(1.dp, if (isLast) ThreatRed else CyberCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$index",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ObsidianBg
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(48.dp)
                        .background(BorderSubtle)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Card(
            modifier = Modifier
                .weight(1f)
                .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp)),
            colors = CardDefaults.cardColors(containerColor = SlateCard)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = step.stage,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberSky,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                if (step.quoteOrEvidence.isNotBlank()) {
                    Text(
                        text = step.quoteOrEvidence,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
                Text(
                    text = "Psychological Hook: ${step.psychologicalHook}",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
private fun SafeguardRow(
    stepNumber: String,
    action: String,
    description: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(SafeEmerald.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                color = SafeEmerald,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = action,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp
            )
        }
    }
}
