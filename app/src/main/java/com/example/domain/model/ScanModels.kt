package com.example.domain.model

enum class ScanType {
    QR_CODE,
    MESSAGE_TEXT,
    URL_DESTINATION,
    PAYMENT_SCREENSHOT;

    val displayName: String
        get() = when (this) {
            QR_CODE -> "QR Code"
            MESSAGE_TEXT -> "Message / Text"
            URL_DESTINATION -> "Website / URL"
            PAYMENT_SCREENSHOT -> "Payment Screenshot"
        }
}

enum class RiskClassification(val label: String, val levelText: String) {
    SAFE("SAFE", "LOW RISK"),
    SUSPICIOUS("SUSPICIOUS", "CAUTION REQUIRED"),
    HIGH_RISK("HIGH RISK", "STRONG THREAT"),
    CRITICAL("CRITICAL", "DO NOT PROCEED");

    companion object {
        fun fromScore(score: Int): RiskClassification = when {
            score >= 81 -> CRITICAL
            score >= 61 -> HIGH_RISK
            score >= 31 -> SUSPICIOUS
            else -> SAFE
        }
    }
}

enum class ThreatCategory(val displayName: String, val description: String) {
    FINANCIAL_SCAM("Financial Scam", "Attempts unauthorized fund transfers or payment diversion."),
    CREDENTIAL_THEFT("Credential Theft", "Harvests passwords, PINs, or banking login credentials."),
    OTP_SOCIAL_ENGINEERING("OTP Social Engineering", "Coerces or tricks victim into sharing one-time security codes."),
    PHISHING("Phishing", "Spoofs a legitimate portal to steal confidential user details."),
    IMPERSONATION("Impersonation", "Pretends to be a government body, bank, or trusted organization."),
    FAKE_SUPPORT("Fake Customer Support", "Impersonates helpdesk to push remote access software or payments."),
    FAKE_DELIVERY("Fake Delivery", "Claims parcel delivery failure to solicit fees or link clicks."),
    FAKE_INVESTMENT("Fake Investment", "Promises unrealistically high returns or guaranteed yields."),
    PAYMENT_FRAUD("Payment Fraud", "Manipulates UPI, QR, or transaction flows into paying the attacker."),
    SUSPICIOUS_QR("Suspicious QR", "Directs scanner to unverified payment gateways or obfuscated endpoints."),
    UNTRUSTED_DESTINATION("Untrusted Destination", "Directs user to high-risk TLDs, IP hosts, or typosquatted domains."),
    SOCIAL_ENGINEERING("Social Engineering", "Uses psychological triggers (urgency, panic, fear) to bypass scrutiny."),
    UNKNOWN("Unknown / Unclassified", "No distinct single threat pattern confirmed by heuristics.");

    companion object {
        fun fromName(name: String?): ThreatCategory {
            if (name == null) return UNKNOWN
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: UNKNOWN
        }
    }
}

data class RiskSignals(
    val financialRequest: Boolean = false,
    val otpRequest: Boolean = false,
    val passwordRequest: Boolean = false,
    val urgency: Boolean = false,
    val authorityImpersonation: Boolean = false,
    val threatLanguage: Boolean = false,
    val suspiciousDomain: Boolean = false,
    val suspiciousUrlStructure: Boolean = false,
    val rewardGreedTrap: Boolean = false,
    val suspiciousApkOrRemoteControl: Boolean = false,
    val independentVerificationEvidence: Boolean = false,
    val extractedUrls: List<String> = emptyList(),
    val extractedPhones: List<String> = emptyList(),
    val extractedAmounts: List<String> = emptyList(),
    val signalDetails: List<String> = emptyList()
)

data class AttackStep(
    val stage: String,
    val quoteOrEvidence: String,
    val psychologicalHook: String,
    val order: Int
)

data class ScamDNAStep(
    val order: Int,
    val title: String,
    val tactic: String,
    val psychologicalPurpose: String,
    val evidenceSnippet: String
)

data class ScamDNA(
    val manipulationStrategy: String,
    val sequence: List<ScamDNAStep> = emptyList()
)

data class EvidenceItem(
    val signalName: String,
    val scoreContribution: Int,
    val evidenceSnippet: String,
    val explanation: String
)

data class TrustBreakdown(
    val identityTrustScore: Int,      // 0..100 (100 = high trust, 0 = strong impersonation)
    val messageTrustScore: Int,       // 0..100
    val destinationTrustScore: Int,   // 0..100
    val financialRiskScore: Int,      // 0..100 (100 = critical risk)
    val urgencyScore: Int,            // 0..100 (100 = acute artificial pressure)
    val credentialRiskScore: Int,     // 0..100 (100 = credential harvesting)
    val manipulationRiskScore: Int,   // 0..100 (100 = deceptive psychological manipulation)
    val summary: String
)

data class VerificationGuideline(
    val title: String,
    val recommendedAction: String,
    val safeChannel: String,
    val strictWarning: String
)

data class WhatCouldHappenScenario(
    val title: String,
    val chainSteps: List<String>,
    val disclaimer: String = "Educational simulation of a potential attack trajectory based on observed signals. Not an assertion of absolute certainty."
)

data class RiskTimelineStep(
    val stage: String,
    val label: String,
    val isDetected: Boolean,
    val description: String
)

enum class SafeActionType {
    COPY_FOR_CYBERCRIME,
    COPY_SANITIZED_URL,
    RECOMMEND_BLOCK,
    RECOMMEND_BANK_HELPLINE,
    SAFE_DISMISS
}

data class SafeActionShortcut(
    val id: String,
    val title: String,
    val description: String,
    val actionType: SafeActionType,
    val payload: String = ""
)

data class URLIntelligence(
    val originalUrl: String,
    val hostname: String,
    val tld: String,
    val isSuspiciousTld: Boolean,
    val hasLookalikeDomain: Boolean,
    val lookalikeBrand: String? = null,
    val hasSuspiciousSubdomain: Boolean,
    val subdomains: List<String> = emptyList(),
    val hasCharacterSubstitutions: Boolean,
    val isIpAddress: Boolean,
    val isPunycode: Boolean,
    val excessiveComplexity: Boolean,
    val isPaymentUri: Boolean,
    val payeeName: String? = null,
    val structuralFlags: List<String> = emptyList(),
    val unavailableDataNotes: List<String> = listOf(
        "Domain Registration Age: Unavailable (Requires remote WHOIS registry)",
        "Global DNS Threat Feed: Offline heuristic analysis only",
        "SSL Certificate Authority: Unavailable without active TLS handshake"
    )
)

data class AskPhantomQnA(
    val question: String,
    val answer: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ScanResult(
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val scanType: ScanType,
    val rawContent: String,
    val destinationUrl: String? = null,
    val riskScore: Int,
    val classification: RiskClassification,
    val threatCategory: ThreatCategory = ThreatCategory.UNKNOWN,
    val intent: String,
    val manipulationTechniques: List<String>,
    val reasons: List<String>,
    val attackChain: List<AttackStep> = emptyList(),
    val scamDNA: ScamDNA = ScamDNA("No manipulation pattern detected"),
    val evidenceItems: List<EvidenceItem> = emptyList(),
    val trustBreakdown: TrustBreakdown = TrustBreakdown(100, 100, 100, 0, 0, 0, 0, "No significant risk flags observed"),
    val verificationGuidelines: List<VerificationGuideline> = emptyList(),
    val whatCouldHappen: WhatCouldHappenScenario = WhatCouldHappenScenario("No immediate exploitation vector identified", emptyList()),
    val riskTimeline: List<RiskTimelineStep> = emptyList(),
    val safeActions: List<SafeActionShortcut> = emptyList(),
    val urlIntelligence: URLIntelligence? = null,
    val phantomInsight: String,
    val recommendedAction: String,
    val isAIEnhanced: Boolean = false,
    val signals: RiskSignals = RiskSignals(),
    val qnaHistory: List<AskPhantomQnA> = emptyList()
)
