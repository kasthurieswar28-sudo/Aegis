package com.example.ui.demo

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.RiskClassification
import com.example.domain.model.ScanType
import com.example.domain.model.ThreatCategory
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
import com.example.ui.theme.ThreatRed
import com.example.ui.viewmodel.PhantomViewModel

data class DemoScenario(
    val id: String,
    val title: String,
    val category: ThreatCategory,
    val expectedClassification: RiskClassification,
    val scanType: ScanType,
    val icon: ImageVector,
    val description: String,
    val syntheticContent: String,
    val destinationUrl: String? = null
)

val SYNTHETIC_SCENARIOS = listOf(
    DemoScenario(
        id = "bank_qr_kyc",
        title = "Bank KYC QR Attack",
        category = ThreatCategory.PAYMENT_FRAUD,
        expectedClassification = RiskClassification.CRITICAL,
        scanType = ScanType.QR_CODE,
        icon = Icons.Filled.QrCode,
        description = "Deceptive QR code claiming State Bank KYC suspension with ₹50 reactivation fee.",
        syntheticContent = "upi://pay?pa=reactivate.kyc.sbi@fakebank&pn=State%20Bank%20KYC&am=50.00&cu=INR&tn=Immediate%20KYC%20Reactivation",
        destinationUrl = "upi://pay?pa=reactivate.kyc.sbi@fakebank&pn=State%20Bank%20KYC&am=50.00&cu=INR"
    ),
    DemoScenario(
        id = "delivery_payment",
        title = "Fake Courier Delivery Fee",
        category = ThreatCategory.FINANCIAL_SCAM,
        expectedClassification = RiskClassification.HIGH_RISK,
        scanType = ScanType.MESSAGE_TEXT,
        icon = Icons.Filled.Payment,
        description = "Urgent parcel hold notice demanding ₹49 redelivery fee via untrusted link.",
        syntheticContent = "India Post: Your package #IN984102 cannot be delivered due to incomplete address. Pay ₹49 pending redelivery fee immediately at https://indiapost-parcel-redelivery.top/pay or item will be returned.",
        destinationUrl = "https://indiapost-parcel-redelivery.top/pay"
    ),
    DemoScenario(
        id = "otp_social_eng",
        title = "Telecom SIM Block OTP Coercion",
        category = ThreatCategory.OTP_SOCIAL_ENGINEERING,
        expectedClassification = RiskClassification.CRITICAL,
        scanType = ScanType.MESSAGE_TEXT,
        icon = Icons.Filled.PhoneAndroid,
        description = "Aggressive threat claiming SIM deactivation today unless 6-digit OTP is verified.",
        syntheticContent = "URGENT: Telecom Regulatory Authority. Your SIM card will be permanently disconnected within 2 hours due to unverified Aadhaar. Share the 6-digit verification code OTP received on your mobile immediately to prevent cutoff."
    ),
    DemoScenario(
        id = "fake_customer_support",
        title = "Fake Support Remote Desk",
        category = ThreatCategory.FAKE_SUPPORT,
        expectedClassification = RiskClassification.CRITICAL,
        scanType = ScanType.MESSAGE_TEXT,
        icon = Icons.Filled.SupportAgent,
        description = "Pretends to offer payment refund by pushing AnyDesk remote desktop APK.",
        syntheticContent = "Paytm Customer Support: Your refund of ₹4,999 is on hold. Please install AnyDesk or QuickSupport APK immediately from http://192.168.1.45/paytm-helper.apk and provide your 9-digit session code to clear the transaction."
    ),
    DemoScenario(
        id = "crypto_investment",
        title = "Guaranteed Returns Crypto Scheme",
        category = ThreatCategory.FAKE_INVESTMENT,
        expectedClassification = RiskClassification.HIGH_RISK,
        scanType = ScanType.MESSAGE_TEXT,
        icon = Icons.Filled.TrendingUp,
        description = "Unsolicited high-yield investment scheme promising 300% daily returns.",
        syntheticContent = "VIP Invitation: Official Binance Cloud Mining Pool. Invest ₹10,000 today and receive guaranteed ₹30,000 daily returns credited to your wallet. Limited slots available! Claim bonus at https://binance-earn-crypto.xyz/invest"
    ),
    DemoScenario(
        id = "credential_phishing",
        title = "Lookalike Banking Phishing Portal",
        category = ThreatCategory.PHISHING,
        expectedClassification = RiskClassification.CRITICAL,
        scanType = ScanType.URL_DESTINATION,
        icon = Icons.Filled.Language,
        description = "Homoglyph domain imitating HDFC Bank login with high-risk .top extension.",
        syntheticContent = "https://hdfcbank-netbanking-secure-auth.top/login.php?session=urgent",
        destinationUrl = "https://hdfcbank-netbanking-secure-auth.top/login.php?session=urgent"
    ),
    DemoScenario(
        id = "safe_notification",
        title = "Legitimate Clinic Appointment",
        category = ThreatCategory.UNKNOWN,
        expectedClassification = RiskClassification.SAFE,
        scanType = ScanType.MESSAGE_TEXT,
        icon = Icons.Filled.CheckCircle,
        description = "Normal transactional confirmation with zero financial hooks or threats.",
        syntheticContent = "City Health Clinic: Your dental check-up with Dr. Sharma is scheduled for tomorrow at 4:30 PM. Please arrive 10 minutes early. To reschedule, call clinic reception directly at 011-23456789."
    ),
    DemoScenario(
        id = "clean_website",
        title = "Normal Institutional Portal",
        category = ThreatCategory.UNKNOWN,
        expectedClassification = RiskClassification.SAFE,
        scanType = ScanType.URL_DESTINATION,
        icon = Icons.Filled.Shield,
        description = "Standard verified institutional website with clean domain structure.",
        syntheticContent = "https://www.who.int/news-room/fact-sheets",
        destinationUrl = "https://www.who.int/news-room/fact-sheets"
    )
)

@Composable
fun DemoScreen(
    viewModel: PhantomViewModel,
    onNavigateToResult: () -> Unit,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .testTag("demo_screen"),
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
                        text = "SYNTHETIC THREAT SIMULATOR",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Deterministic Cybersecurity Scenarios",
                        color = CyberSky,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Info Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SafeEmerald.copy(alpha = 0.08f))
                    .border(1.dp, SafeEmerald.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Filled.ElectricBolt,
                        contentDescription = null,
                        tint = SafeEmeraldLight,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "These controlled, synthetic scenarios test Aegis's detection engine without exposing real credentials. Tapping any scenario feeds it into the live analysis pipeline to display real Scam DNA, Evidence Mode, and Trust Breakdown.",
                        fontSize = 12.sp,
                        color = TextPrimary,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // Scenarios List
        items(SYNTHETIC_SCENARIOS) { scenario ->
            ScenarioCard(
                scenario = scenario,
                onClick = {
                    viewModel.runSyntheticScenario(
                        title = scenario.title,
                        content = scenario.syntheticContent,
                        scanType = scenario.scanType,
                        destinationUrl = scenario.destinationUrl
                    )
                    onNavigateToResult()
                }
            )
        }
    }
}

@Composable
private fun ScenarioCard(
    scenario: DemoScenario,
    onClick: () -> Unit
) {
    val statusColor = when (scenario.expectedClassification) {
        RiskClassification.CRITICAL -> ThreatRed
        RiskClassification.HIGH_RISK -> ElectricAmber
        RiskClassification.SUSPICIOUS -> ElectricAmber
        RiskClassification.SAFE -> SafeEmeraldLight
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick)
            .testTag("scenario_${scenario.id}"),
        colors = CardDefaults.cardColors(containerColor = SlateCard),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.12f))
                        .border(1.dp, statusColor.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = scenario.icon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = scenario.title,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = scenario.category.displayName,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = scenario.expectedClassification.label,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = scenario.description,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ObsidianBg.copy(alpha = 0.6f))
                    .padding(8.dp)
            ) {
                Text(
                    text = scenario.syntheticContent.take(80) + if (scenario.syntheticContent.length > 80) "..." else "",
                    color = CyberSky.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 2
                )
            }
        }
    }
}
