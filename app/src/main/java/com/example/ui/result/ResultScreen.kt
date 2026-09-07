package com.example.ui.result

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.EvidenceItem
import com.example.domain.model.RiskClassification
import com.example.domain.model.SafeActionShortcut
import com.example.domain.model.SafeActionType
import com.example.domain.model.ScanType
import com.example.domain.model.ScamDNA
import com.example.domain.model.ThreatCategory
import com.example.domain.model.TrustBreakdown
import com.example.ui.components.AnimatedRiskMeter
import com.example.ui.components.CyberCard
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CriticalViolet
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
import com.example.ui.theme.ThreatRed
import com.example.ui.viewmodel.PhantomViewModel

@Composable
fun ResultScreen(
    viewModel: PhantomViewModel,
    onNavigateToExplanation: () -> Unit,
    onScanAnother: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val result by viewModel.currentResult.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()
    val isAskingAi by viewModel.isAskingAi.collectAsState()

    var userQuestion by remember { mutableStateOf("") }
    var showEvidenceMode by remember { mutableStateOf(true) }

    if (result == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ObsidianBg),
            contentAlignment = Alignment.Center
        ) {
            Text("No active scan analysis available.", color = TextSecondary)
        }
        return
    }

    val current = result!!

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .testTag("result_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Top Bar
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
                        text = "DECEPTION ASSESSMENT",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    val displayCategory = if (current.classification == RiskClassification.SAFE) {
                        if (current.destinationUrl?.contains("instagram", ignoreCase = true) == true ||
                            current.urlIntelligence?.hostname?.contains("instagram", ignoreCase = true) == true) {
                            "Verified Instagram Profile"
                        } else if (current.urlIntelligence?.hostname?.contains("App", ignoreCase = true) == true ||
                            current.urlIntelligence?.hostname?.contains("Wi-Fi", ignoreCase = true) == true ||
                            current.urlIntelligence?.hostname?.contains("Contact", ignoreCase = true) == true) {
                            current.urlIntelligence.hostname
                        } else if (current.scanType == ScanType.QR_CODE) {
                            "Verified Safe QR Code"
                        } else {
                            "Verified Benign Content"
                        }
                    } else {
                        current.threatCategory.displayName
                    }

                    Text(
                        text = "${current.scanType.displayName} • $displayCategory",
                        color = CyberSky,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                if (current.isAIEnhanced) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SafeEmerald.copy(alpha = 0.12f))
                            .border(1.dp, SafeEmerald.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(SafeEmerald)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AI REASONED",
                            color = SafeEmerald,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        // Circular Animated Gauge Hero Card
        item {
            val displayCategory = if (current.classification == RiskClassification.SAFE) {
                if (current.destinationUrl?.contains("instagram", ignoreCase = true) == true ||
                    current.urlIntelligence?.hostname?.contains("instagram", ignoreCase = true) == true) {
                    "VERIFIED INSTAGRAM PROFILE"
                } else if (current.scanType == ScanType.QR_CODE) {
                    "VERIFIED SAFE QR CODE"
                } else {
                    "VERIFIED BENIGN CONTENT"
                }
            } else {
                current.threatCategory.displayName.uppercase()
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedRiskMeter(
                    score = current.riskScore,
                    classification = current.classification
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = displayCategory,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (current.classification) {
                        RiskClassification.CRITICAL -> ThreatRed
                        RiskClassification.HIGH_RISK -> ElectricAmber
                        RiskClassification.SUSPICIOUS -> ElectricAmber
                        RiskClassification.SAFE -> SafeEmeraldLight
                    },
                    letterSpacing = 1.sp
                )
                Text(
                    text = current.intent,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }

        // 1. EVIDENCE MODE SECTION (Signal contribution breakdown)
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "EVIDENCE MODE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = if (showEvidenceMode) "HIDE" else "SHOW DETAILS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        modifier = Modifier.clickable { showEvidenceMode = !showEvidenceMode }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (showEvidenceMode) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("evidence_mode_card"),
                        colors = CardDefaults.cardColors(containerColor = SlateCard),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Score: ${current.riskScore}/100 • Risk estimate based on verified heuristic indicators rather than proof of fraud.",
                                fontSize = 11.sp,
                                color = TextMuted,
                                lineHeight = 15.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            if (current.evidenceItems.isEmpty()) {
                                Text(
                                    text = "No anomalous risk signals detected in content.",
                                    fontSize = 12.sp,
                                    color = SafeEmeraldLight
                                )
                            } else {
                                current.evidenceItems.forEach { item ->
                                    EvidenceItemRow(item)
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. SCAM DNA SECTION (Visual attack sequence explanation)
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AccountTree, contentDescription = null, tint = SafeEmeraldLight, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SCAM DNA • MANIPULATION STRATEGY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SafeEmeraldLight,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("scam_dna_card"),
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SafeEmerald.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = current.scamDNA.manipulationStrategy,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            lineHeight = 17.sp
                        )

                        if (current.scamDNA.sequence.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            current.scamDNA.sequence.forEachIndexed { index, step ->
                                ScamDNAStepRow(step = step, isLast = index == current.scamDNA.sequence.size - 1)
                            }
                        }
                    }
                }
            }
        }

        // 3. TRUST BREAKDOWN (7 Multi-Dimensional Trust Indicators)
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Shield, contentDescription = null, tint = CyberSky, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TRUST BREAKDOWN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberSky,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("trust_breakdown_card"),
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = current.trustBreakdown.summary,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        TrustDimensionBar("Identity Trust", current.trustBreakdown.identityTrustScore, isRisk = false)
                        TrustDimensionBar("Message Integrity", current.trustBreakdown.messageTrustScore, isRisk = false)
                        TrustDimensionBar("Destination Trust", current.trustBreakdown.destinationTrustScore, isRisk = false)
                        TrustDimensionBar("Financial Extraction Risk", current.trustBreakdown.financialRiskScore, isRisk = true)
                        TrustDimensionBar("Artificial Urgency Level", current.trustBreakdown.urgencyScore, isRisk = true)
                        TrustDimensionBar("Credential Harvesting Risk", current.trustBreakdown.credentialRiskScore, isRisk = true)
                        TrustDimensionBar("Psychological Manipulation", current.trustBreakdown.manipulationRiskScore, isRisk = true)
                    }
                }
            }
        }

        // 4. VERIFY BEFORE YOU ACT SECTION
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.HelpOutline, contentDescription = null, tint = ElectricAmber, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "VERIFY BEFORE YOU ACT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricAmber,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                current.verificationGuidelines.forEach { guideline ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = SlateCard),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElectricAmber.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = guideline.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = guideline.recommendedAction,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "Safe Channel: ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SafeEmeraldLight)
                                Text(text = guideline.safeChannel, fontSize = 11.sp, color = TextPrimary)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = guideline.strictWarning,
                                fontSize = 10.sp,
                                color = ThreatRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // 5. WHAT COULD HAPPEN? (Educational Attack Chain)
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = CriticalViolet, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "WHAT COULD HAPPEN?",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CriticalViolet,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("what_could_happen_card"),
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CriticalViolet.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = current.whatCouldHappen.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        current.whatCouldHappen.chainSteps.forEach { step ->
                            Text(
                                text = step,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "⚠ ${current.whatCouldHappen.disclaimer}",
                            fontSize = 10.sp,
                            color = TextMuted,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }

        // 6. RISK TIMELINE (Likely manipulation sequence)
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Timeline, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "RISK TIMELINE • MANIPULATION SEQUENCE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Estimated progression based on detected cognitive hooks. Aegis does not assert knowledge of the attacker's actual timeline.",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        current.riskTimeline.forEach { step ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (step.isDetected) ThreatRed else BorderSubtle)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = step.stage, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        if (step.isDetected) {
                                            Text(text = "DETECTED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ThreatRed)
                                        }
                                    }
                                    Text(text = step.description, fontSize = 11.sp, color = TextSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. SAFE ACTION SHORTCUTS
        if (current.safeActions.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = "SAFE ACTION SHORTCUTS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SafeEmeraldLight,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    current.safeActions.forEach { action ->
                        SafeActionCard(
                            action = action,
                            onPerform = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                when (action.actionType) {
                                    SafeActionType.COPY_FOR_CYBERCRIME -> {
                                        val clip = ClipData.newPlainText("Cybercrime Report", action.payload)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Incident summary copied to clipboard", Toast.LENGTH_SHORT).show()
                                    }
                                    SafeActionType.COPY_SANITIZED_URL -> {
                                        val clip = ClipData.newPlainText("Sanitized URL", action.payload)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Sanitized destination copied safely", Toast.LENGTH_SHORT).show()
                                    }
                                    SafeActionType.RECOMMEND_BLOCK -> {
                                        Toast.makeText(context, "Open your phone's SMS app and select 'Block & Report Spam'", Toast.LENGTH_LONG).show()
                                    }
                                    SafeActionType.SAFE_DISMISS -> {
                                        onBack()
                                    }
                                    else -> {}
                                }
                            }
                        )
                    }
                }
            }
        }

        // 8. ASK AEGIS (Interactive Q&A)
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.QuestionAnswer, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ASK AEGIS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ask_aegis_card"),
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Ask questions regarding this specific analysis. Answers are strictly constrained to verified evidence without assumptions.",
                            fontSize = 11.sp,
                            color = TextMuted,
                            lineHeight = 15.sp
                        )

                        // Suggested query chips
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val chips = listOf(
                                "Why is this suspicious?",
                                "What is the attacker trying to do?",
                                "What should I do next?",
                                "Is it safe to click or pay?"
                            )
                            items(chips) { chip ->
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        userQuestion = chip
                                        viewModel.askPhantom(chip)
                                    },
                                    label = { Text(chip, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = ObsidianBg,
                                        labelColor = TextPrimary
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = false,
                                        borderColor = BorderSubtle
                                    )
                                )
                            }
                        }

                        // Input field
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = userQuestion,
                                onValueChange = { userQuestion = it },
                                placeholder = { Text("Ask a question about this threat...", color = TextMuted, fontSize = 12.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("ask_phantom_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberCyan,
                                    unfocusedBorderColor = BorderSubtle,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (userQuestion.isNotBlank()) {
                                        viewModel.askPhantom(userQuestion)
                                    }
                                },
                                enabled = !isAskingAi && userQuestion.isNotBlank()
                            ) {
                                if (isAskingAi) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = CyberCyan, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Filled.Send, contentDescription = "Send", tint = CyberCyan)
                                }
                            }
                        }

                        // QnA History
                        if (current.qnaHistory.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            current.qnaHistory.forEach { qna ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ObsidianBg)
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Text(text = "Q: ${qna.question}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyberSky)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = qna.answer, fontSize = 12.sp, color = TextPrimary, lineHeight = 16.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onNavigateToExplanation,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("explain_attack_button")
                ) {
                    Icon(Icons.Filled.Psychology, contentDescription = null, tint = ObsidianBg)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "VIEW FULL ATTACK REASONING",
                        color = ObsidianBg,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onScanAnother,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberSky),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Icon(Icons.Filled.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Scan Another", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { viewModel.saveCurrentResult() },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isSaved) SafeEmerald else TextSecondary
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSaved) SafeEmerald.copy(alpha = 0.5f) else BorderSubtle
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isSaved) "Saved" else "Save Result", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun EvidenceItemRow(item: EvidenceItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(if (item.scoreContribution > 0) ThreatRed else SafeEmeraldLight)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.signalName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${if (item.scoreContribution > 0) "+" else ""}${item.scoreContribution} pts",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (item.scoreContribution > 0) ThreatRed else SafeEmeraldLight
                )
            }
            Text(
                text = item.explanation,
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun ScamDNAStepRow(step: com.example.domain.model.ScamDNAStep, isLast: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(SafeEmerald.copy(alpha = 0.15f))
                    .border(1.dp, SafeEmerald, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "${step.order}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SafeEmeraldLight)
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(26.dp)
                        .background(SafeEmerald.copy(alpha = 0.3f))
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = step.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = step.psychologicalPurpose, fontSize = 11.sp, color = TextSecondary, lineHeight = 15.sp)
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
private fun TrustDimensionBar(label: String, value: Int, isRisk: Boolean) {
    val barColor = if (isRisk) {
        when {
            value >= 70 -> ThreatRed
            value >= 40 -> ElectricAmber
            else -> SafeEmeraldLight
        }
    } else {
        when {
            value >= 70 -> SafeEmeraldLight
            value >= 40 -> ElectricAmber
            else -> ThreatRed
        }
    }

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 11.sp, color = TextSecondary)
            Text(text = "$value/100", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = barColor)
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { value / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = barColor,
            trackColor = ObsidianBg
        )
    }
}

@Composable
private fun SafeActionCard(
    action: SafeActionShortcut,
    onPerform: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clickable(onClick = onPerform),
        colors = CardDefaults.cardColors(containerColor = SlateCard),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (action.actionType) {
                    SafeActionType.COPY_FOR_CYBERCRIME, SafeActionType.COPY_SANITIZED_URL -> Icons.Filled.ContentCopy
                    else -> Icons.Filled.Shield
                },
                contentDescription = null,
                tint = SafeEmeraldLight,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = action.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(text = action.description, fontSize = 10.sp, color = TextMuted)
            }
        }
    }
}
