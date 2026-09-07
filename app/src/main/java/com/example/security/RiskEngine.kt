package com.example.security

import com.example.domain.model.EvidenceItem
import com.example.domain.model.RiskClassification
import com.example.domain.model.RiskSignals
import com.example.domain.model.RiskTimelineStep
import com.example.domain.model.SafeActionShortcut
import com.example.domain.model.SafeActionType
import com.example.domain.model.ScamDNA
import com.example.domain.model.ScamDNAStep
import com.example.domain.model.ThreatCategory
import com.example.domain.model.TrustBreakdown
import com.example.domain.model.VerificationGuideline
import com.example.domain.model.WhatCouldHappenScenario
import kotlin.math.max
import kotlin.math.min

data class RiskEvaluation(
    val score: Int,
    val classification: RiskClassification,
    val threatCategory: ThreatCategory,
    val evidenceItems: List<EvidenceItem>,
    val trustBreakdown: TrustBreakdown,
    val scamDNA: ScamDNA,
    val whatCouldHappen: WhatCouldHappenScenario,
    val verificationGuidelines: List<VerificationGuideline>,
    val riskTimeline: List<RiskTimelineStep>,
    val safeActions: List<SafeActionShortcut>,
    val primaryFlags: List<String>,
    val recommendation: String
)

object RiskEngine {

    fun evaluate(
        signals: RiskSignals,
        urlAnalysis: URLAnalysisResult? = null,
        sensitivityFactor: Float = 1.0f
    ): RiskEvaluation {
        val evidenceList = mutableListOf<EvidenceItem>()
        val primaryFlags = mutableListOf<String>()
        var baseScore = 0

        // Deduplication Guard: Each distinct psychological & technical category evaluated once
        // 1. Suspicious Domain / Destination Endpoint
        val isDomainSuspicious = signals.suspiciousDomain || urlAnalysis?.isSuspiciousDomain == true
        if (isDomainSuspicious) {
            val domainSnippet = urlAnalysis?.urlIntelligence?.hostname ?: "Unverified external destination"
            val contribution = 20
            baseScore += contribution
            evidenceList.add(
                EvidenceItem(
                    signalName = "Suspicious Destination Domain",
                    scoreContribution = contribution,
                    evidenceSnippet = domainSnippet,
                    explanation = "The destination domain uses a high-risk registry extension, an unverified brand lookalike pattern, or raw IP addressing."
                )
            )
            primaryFlags.add("Unverified or high-risk destination domain")
        }

        // 2. Obfuscated URL Structure / Redirects / Shorteners
        val isUrlStructureSuspicious = signals.suspiciousUrlStructure || urlAnalysis?.isSuspiciousStructure == true
        if (isUrlStructureSuspicious && !isDomainSuspicious) {
            val contribution = 15
            baseScore += contribution
            evidenceList.add(
                EvidenceItem(
                    signalName = "Obfuscated URL Structure",
                    scoreContribution = contribution,
                    evidenceSnippet = urlAnalysis?.urlIntelligence?.originalUrl?.take(40) ?: "Complex nested path",
                    explanation = "Unusual path parameters, non-standard ports, or shorteners obscuring the destination."
                )
            )
            primaryFlags.add("Masked or abnormal URL structure")
        }

        // 3. Credential or OTP Request
        val hasOtpOrPassword = signals.otpRequest || signals.passwordRequest
        if (hasOtpOrPassword) {
            val contribution = 25
            baseScore += contribution
            val snippet = if (signals.otpRequest && signals.passwordRequest) "Requests both OTP and Password / PIN"
            else if (signals.otpRequest) "Demands One-Time Password (OTP)"
            else "Requests confidential login password or PIN"
            evidenceList.add(
                EvidenceItem(
                    signalName = "Authentication Credential Request",
                    scoreContribution = contribution,
                    evidenceSnippet = snippet,
                    explanation = "Legitimate institutions never request security codes, OTPs, or passwords over unverified channels."
                )
            )
            primaryFlags.add("Requests sensitive security code or password")
        }

        // 4. Financial Request or Payment QR / UPI Intent
        val hasFinancial = signals.financialRequest || urlAnalysis?.isPaymentUri == true
        if (hasFinancial) {
            val contribution = 20
            baseScore += contribution
            val snippet = if (urlAnalysis?.isPaymentUri == true) "UPI payment intent URI (${urlAnalysis.payeeName ?: "Unknown Merchant"})"
            else if (signals.extractedAmounts.isNotEmpty()) "Demands monetary amount: ${signals.extractedAmounts.first()}"
            else "Direct money transfer or payment trigger"
            evidenceList.add(
                EvidenceItem(
                    signalName = "Financial Movement Trigger",
                    scoreContribution = contribution,
                    evidenceSnippet = snippet,
                    explanation = "Attempts to divert funds, collect fees, or trigger wallet authorization."
                )
            )
            primaryFlags.add("Direct payment trigger or fee demand")
        }

        // 5. Authority Impersonation
        if (signals.authorityImpersonation) {
            val contribution = 15
            baseScore += contribution
            evidenceList.add(
                EvidenceItem(
                    signalName = "Institutional Impersonation",
                    scoreContribution = contribution,
                    evidenceSnippet = "Claims official backing (Bank, Postal Service, Telecom, Government)",
                    explanation = "Impersonates recognized authorities to create false credibility and disarm natural caution."
                )
            )
            primaryFlags.add("Impersonates reputable institution or authority")
        }

        // 6. Threat Language / Coercion / Disconnection Warning
        if (signals.threatLanguage) {
            val contribution = 20
            baseScore += contribution
            evidenceList.add(
                EvidenceItem(
                    signalName = "Coercive Threat & Intimidation",
                    scoreContribution = contribution,
                    evidenceSnippet = "Punitive threats (disconnection, account block, legal penalty)",
                    explanation = "Uses intimidation and severe consequences to trigger irrational panic."
                )
            )
            primaryFlags.add("Uses coercive intimidation or punitive threats")
        }

        // 7. Urgency / Artificial Time Pressure
        if (signals.urgency) {
            val contribution = 10
            baseScore += contribution
            evidenceList.add(
                EvidenceItem(
                    signalName = "Artificial Time Pressure",
                    scoreContribution = contribution,
                    evidenceSnippet = "Urgent demands ('today', 'immediately', 'within 2 hours')",
                    explanation = "Restricts deliberation time to prevent the target from performing independent verification."
                )
            )
            primaryFlags.add("Imposes artificial urgency deadline")
        }

        // 8. Remote Control / APK Download Instruction
        if (signals.suspiciousApkOrRemoteControl) {
            val contribution = 25
            baseScore += contribution
            evidenceList.add(
                EvidenceItem(
                    signalName = "Malicious Software / Remote Screen Share",
                    scoreContribution = contribution,
                    evidenceSnippet = "Urges app installation or remote desktop tool (AnyDesk/TeamViewer/APK)",
                    explanation = "Aims to establish device remote access or install spyware to steal two-factor codes."
                )
            )
            primaryFlags.add("Requests remote access tool or unknown APK installation")
        }

        // 9. Reward / Greed Bait
        if (signals.rewardGreedTrap) {
            val contribution = 15
            baseScore += contribution
            evidenceList.add(
                EvidenceItem(
                    signalName = "Unsolicited Reward Bait",
                    scoreContribution = contribution,
                    evidenceSnippet = "Unexpected lottery, cashback, or prize claim",
                    explanation = "Promises unearned gains to entice the user into following malicious instructions."
                )
            )
            primaryFlags.add("Unsolicited reward or prize bait")
        }

        // 10. Independent Verification Mitigator (-15)
        if (signals.independentVerificationEvidence) {
            val contribution = -15
            baseScore += contribution
            evidenceList.add(
                EvidenceItem(
                    signalName = "Official Verification Guidance",
                    scoreContribution = contribution,
                    evidenceSnippet = "Directs to official branch or verified application",
                    explanation = "Advises user to independently check via recognized channels, lowering deceit likelihood."
                )
            )
        }

        // 11. Verified Platform Mitigator (-25)
        if (urlAnalysis?.isVerifiedPlatform == true && !hasOtpOrPassword && !hasFinancial) {
            val contribution = -25
            baseScore += contribution
            evidenceList.add(
                EvidenceItem(
                    signalName = "Verified Platform Destination",
                    scoreContribution = contribution,
                    evidenceSnippet = "Destination belongs to verified legitimate registry: ${urlAnalysis.platformName ?: "Verified Service"}",
                    explanation = "The destination is verified against the global directory of trusted services and social platforms."
                )
            )
        }

        // Sensitivity Factor Adjustment & Strict 0..100 Clamping
        val adjustedScore = (baseScore * sensitivityFactor).toInt()
        val finalScore = max(0, min(100, adjustedScore))
        val classification = RiskClassification.fromScore(finalScore)

        // Determine Threat Category strictly supported by evidence
        val category = determineCategory(signals, urlAnalysis, finalScore)

        // Derive Trust Breakdown strictly from real signals
        val trustBreakdown = deriveTrustBreakdown(signals, urlAnalysis, finalScore)

        // Dynamically construct Scam DNA explaining manipulation strategy
        val scamDNA = if (classification == RiskClassification.SAFE) {
            ScamDNA(
                manipulationStrategy = "No deceptive manipulation or coercion detected. Content appears legitimate and safe to proceed.",
                sequence = emptyList()
            )
        } else {
            constructScamDNA(signals, urlAnalysis, category)
        }

        // Educational What Could Happen? scenario
        val whatCouldHappen = if (classification == RiskClassification.SAFE) {
            WhatCouldHappenScenario(
                title = "Standard Safe Digital Interaction",
                chainSteps = listOf(
                    "You interact with a standard verified digital destination or informational code.",
                    "No unauthorized data harvesting, financial coercion, or hostile redirection observed."
                )
            )
        } else {
            constructWhatCouldHappen(signals, urlAnalysis, category)
        }

        // Verify Before You Act recommendations
        val verificationGuidelines = if (classification == RiskClassification.SAFE) {
            listOf(
                VerificationGuideline(
                    title = "Verified Safe Destination",
                    recommendedAction = "Safe to open or interact. No security risk detected.",
                    safeChannel = "Official application or browser",
                    strictWarning = "No threat signatures detected. Standard usage."
                )
            )
        } else {
            constructVerificationGuidelines(signals, urlAnalysis, category)
        }

        // Risk Timeline (Likely manipulation sequence)
        val riskTimeline = constructRiskTimeline(signals, urlAnalysis)

        // Contextual Safe Action Shortcuts
        val safeActions = constructSafeActions(signals, urlAnalysis, category)

        val recommendation = when (classification) {
            RiskClassification.CRITICAL -> "DO NOT PROCEED. Strong indicators of deceptive fraud. Do not scan, authorize payments, or share codes."
            RiskClassification.HIGH_RISK -> "DO NOT PROCEED. High probability of deceptive manipulation. Verify independently before taking any action."
            RiskClassification.SUSPICIOUS -> "EXERCISE CAUTION. Content exhibits coercive or unverified patterns. Verify using official channels."
            RiskClassification.SAFE -> "SAFE TO PROCEED. Verified legitimate destination. No deceptive or hostile signatures detected."
        }

        val flags = if (classification == RiskClassification.SAFE) {
            if (urlAnalysis?.isVerifiedPlatform == true) {
                listOf("Verified safe platform: ${urlAnalysis.platformName ?: "Legitimate"}", "No deceptive threat signatures detected")
            } else {
                listOf("Benign digital content", "No malicious patterns detected")
            }
        } else if (primaryFlags.isEmpty()) {
            listOf("No prominent deception triggers detected")
        } else {
            primaryFlags
        }

        return RiskEvaluation(
            score = finalScore,
            classification = classification,
            threatCategory = category,
            evidenceItems = evidenceList,
            trustBreakdown = trustBreakdown,
            scamDNA = scamDNA,
            whatCouldHappen = whatCouldHappen,
            verificationGuidelines = verificationGuidelines,
            riskTimeline = riskTimeline,
            safeActions = safeActions,
            primaryFlags = flags,
            recommendation = recommendation
        )
    }

    private fun determineCategory(
        signals: RiskSignals,
        urlAnalysis: URLAnalysisResult?,
        score: Int
    ): ThreatCategory {
        if (score < 25) return ThreatCategory.UNKNOWN

        return when {
            signals.otpRequest -> ThreatCategory.OTP_SOCIAL_ENGINEERING
            signals.suspiciousApkOrRemoteControl -> ThreatCategory.FAKE_SUPPORT
            signals.passwordRequest || (urlAnalysis?.isKnownPhishingKeyword == true) -> ThreatCategory.CREDENTIAL_THEFT
            urlAnalysis?.isPaymentUri == true -> ThreatCategory.PAYMENT_FRAUD
            signals.rewardGreedTrap -> ThreatCategory.FAKE_INVESTMENT
            signals.threatLanguage && signals.financialRequest -> ThreatCategory.FINANCIAL_SCAM
            signals.authorityImpersonation && signals.threatLanguage -> ThreatCategory.IMPERSONATION
            urlAnalysis?.isSuspiciousDomain == true -> ThreatCategory.PHISHING
            signals.financialRequest -> ThreatCategory.FINANCIAL_SCAM
            signals.urgency && signals.authorityImpersonation -> ThreatCategory.SOCIAL_ENGINEERING
            urlAnalysis?.urlIntelligence?.excessiveComplexity == true -> ThreatCategory.UNTRUSTED_DESTINATION
            else -> ThreatCategory.SOCIAL_ENGINEERING
        }
    }

    private fun deriveTrustBreakdown(
        signals: RiskSignals,
        urlAnalysis: URLAnalysisResult?,
        finalScore: Int
    ): TrustBreakdown {
        // Identity trust: 100 drops to 15 if authority impersonation or lookalike domain detected
        val identityTrust = when {
            signals.authorityImpersonation && (urlAnalysis?.isSuspiciousDomain == true) -> 10
            signals.authorityImpersonation -> 25
            urlAnalysis?.isSuspiciousDomain == true -> 30
            signals.threatLanguage -> 45
            else -> max(20, 100 - (finalScore / 2))
        }

        // Message trust: drops with urgency and threats
        val messageTrust = when {
            signals.urgency && signals.threatLanguage -> 15
            signals.threatLanguage -> 25
            signals.urgency -> 40
            else -> max(20, 100 - (finalScore / 2))
        }

        // Destination trust: drops if suspicious domain or complex URL
        val destTrust = when {
            urlAnalysis?.isVerifiedPlatform == true -> 100
            urlAnalysis?.isSuspiciousDomain == true -> 10
            urlAnalysis?.isSuspiciousStructure == true -> 35
            urlAnalysis?.isPaymentUri == true -> 30
            else -> max(30, 100 - finalScore)
        }

        // Financial risk
        val finRisk = when {
            urlAnalysis?.isPaymentUri == true -> 90
            signals.financialRequest && signals.threatLanguage -> 85
            signals.financialRequest -> 70
            signals.rewardGreedTrap -> 60
            else -> min(30, finalScore / 3)
        }

        // Urgency score
        val urgencyRisk = when {
            signals.urgency && signals.threatLanguage -> 95
            signals.urgency -> 75
            signals.threatLanguage -> 60
            else -> 10
        }

        // Credential risk
        val credRisk = when {
            signals.otpRequest && signals.passwordRequest -> 98
            signals.otpRequest -> 90
            signals.passwordRequest -> 85
            urlAnalysis?.isKnownPhishingKeyword == true -> 75
            else -> 5
        }

        // Manipulation risk
        val manipRisk = when {
            signals.authorityImpersonation && signals.urgency -> 90
            signals.threatLanguage -> 80
            signals.rewardGreedTrap -> 75
            else -> min(100, finalScore)
        }

        val summary = when {
            finalScore >= 61 -> "Elevated manipulation risk and severely depleted sender identity trust."
            finalScore >= 31 -> "Cautionary indicators observed across message urgency and unverified endpoints."
            else -> "Trust indicators remain balanced with no prominent hostile vectors detected."
        }

        return TrustBreakdown(
            identityTrustScore = identityTrust,
            messageTrustScore = messageTrust,
            destinationTrustScore = destTrust,
            financialRiskScore = finRisk,
            urgencyScore = urgencyRisk,
            credentialRiskScore = credRisk,
            manipulationRiskScore = manipRisk,
            summary = summary
        )
    }

    private fun constructScamDNA(
        signals: RiskSignals,
        urlAnalysis: URLAnalysisResult?,
        category: ThreatCategory
    ): ScamDNA {
        val steps = mutableListOf<ScamDNAStep>()
        var stepIndex = 1

        // Step 1: Hook
        if (signals.authorityImpersonation) {
            steps.add(
                ScamDNAStep(
                    order = stepIndex++,
                    title = "Authority Pretext",
                    tactic = "Impersonation",
                    psychologicalPurpose = "Disarms natural skepticism by donning the mantle of a trusted official institution.",
                    evidenceSnippet = "Claims official banking, utility, or regulatory jurisdiction"
                )
            )
        } else if (signals.rewardGreedTrap) {
            steps.add(
                ScamDNAStep(
                    order = stepIndex++,
                    title = "Reward Hook",
                    tactic = "Greed Bait",
                    psychologicalPurpose = "Entices the victim with unexpected financial gain to stimulate impulsive compliance.",
                    evidenceSnippet = "Unsolicited reward, cashback, or lottery announcement"
                )
            )
        } else {
            steps.add(
                ScamDNAStep(
                    order = stepIndex++,
                    title = "Digital Contact",
                    tactic = "Unsolicited Outbound",
                    psychologicalPurpose = "Establishes initial engagement through an unverified external communication channel.",
                    evidenceSnippet = "Direct external message or unprompted QR code"
                )
            )
        }

        // Step 2: Emotional Trigger (Fear or Urgency)
        if (signals.threatLanguage) {
            steps.add(
                ScamDNAStep(
                    order = stepIndex++,
                    title = "Coercive Pressure",
                    tactic = "Threat of Loss / Disconnection",
                    psychologicalPurpose = "Induces a flight-or-fight response to prevent calm critical thinking.",
                    evidenceSnippet = "Threatens power cutoff, account lock, or legal penalties"
                )
            )
        }

        if (signals.urgency) {
            steps.add(
                ScamDNAStep(
                    order = stepIndex++,
                    title = "Temporal Compression",
                    tactic = "Artificial Deadline",
                    psychologicalPurpose = "Creates intense time pressure so the victim acts before consulting trusted advisors.",
                    evidenceSnippet = "Tight time window ('today', 'immediately', 'hours left')"
                )
            )
        }

        // Step 3: Exploitation Action
        if (signals.otpRequest || signals.passwordRequest) {
            steps.add(
                ScamDNAStep(
                    order = stepIndex++,
                    title = "Credential Harvesting",
                    tactic = "Authentication Hijacking",
                    psychologicalPurpose = "Tricks the user into volunteering the final secret factor protecting their account.",
                    evidenceSnippet = "Solicits OTP, security code, or net banking password"
                )
            )
        } else if (signals.financialRequest || urlAnalysis?.isPaymentUri == true) {
            steps.add(
                ScamDNAStep(
                    order = stepIndex++,
                    title = "Payment Redirection",
                    tactic = "Financial Extraction",
                    psychologicalPurpose = "Directs the victim into authorizing an irreversible transfer under false pretenses.",
                    evidenceSnippet = "Direct UPI QR scan or fee payment request"
                )
            )
        } else if (signals.suspiciousApkOrRemoteControl) {
            steps.add(
                ScamDNAStep(
                    order = stepIndex++,
                    title = "Device Infiltration",
                    tactic = "Remote Access Tool Push",
                    psychologicalPurpose = "Gains screen visibility or control to intercept incoming banking SMS codes.",
                    evidenceSnippet = "Instructs user to install APK or remote desktop app"
                )
            )
        }

        // Step 4: Endpoint Trap
        if (urlAnalysis?.isSuspiciousDomain == true) {
            steps.add(
                ScamDNAStep(
                    order = stepIndex++,
                    title = "Controlled Destination",
                    tactic = "Lookalike / High-Risk Domain",
                    psychologicalPurpose = "Funnel victim to attacker-controlled infrastructure styled to resemble legitimate pages.",
                    evidenceSnippet = "Host: ${urlAnalysis.urlIntelligence.hostname}"
                )
            )
        }

        val strategySummary = when (category) {
            ThreatCategory.OTP_SOCIAL_ENGINEERING ->
                "Manufactures an emergency to manipulate the victim into sharing two-factor authentication codes to breach the account."
            ThreatCategory.PAYMENT_FRAUD, ThreatCategory.FINANCIAL_SCAM ->
                "Exploits manufactured urgency or punitive threats to divert victim funds into an attacker-controlled payment destination."
            ThreatCategory.CREDENTIAL_THEFT, ThreatCategory.PHISHING ->
                "Lures the victim to a lookalike spoofed portal to capture banking credentials and passwords."
            ThreatCategory.FAKE_SUPPORT ->
                "Pretends to offer technical assistance to establish remote control software and siphon sensitive telemetry."
            else ->
                "Utilizes deceptive psychological triggers to pressure the victim into taking an unverified, high-risk action."
        }

        return ScamDNA(
            manipulationStrategy = strategySummary,
            sequence = steps
        )
    }

    private fun constructWhatCouldHappen(
        signals: RiskSignals,
        urlAnalysis: URLAnalysisResult?,
        category: ThreatCategory
    ): WhatCouldHappenScenario {
        val steps = mutableListOf<String>()

        when (category) {
            ThreatCategory.OTP_SOCIAL_ENGINEERING -> {
                steps.add("1. You share the requested OTP under the belief that you are resolving an urgent security issue.")
                steps.add("2. The attacker uses the OTP to authenticate a high-value fund transfer or password reset on your actual account.")
                steps.add("3. You are locked out of your banking app while funds are routed to unrecoverable accounts.")
            }
            ThreatCategory.PAYMENT_FRAUD, ThreatCategory.FINANCIAL_SCAM -> {
                steps.add("1. You scan the provided QR code or approve the UPI transfer expecting to clear a bill or receive a refund.")
                steps.add("2. Entering your UPI PIN authorizes a deduction from your account, rather than crediting any funds.")
                steps.add("3. Once the transfer completes, the attacker severs communication and your funds are lost.")
            }
            ThreatCategory.CREDENTIAL_THEFT, ThreatCategory.PHISHING -> {
                steps.add("1. You tap the unverified link and arrive at a login portal designed to resemble your real institution.")
                steps.add("2. You enter your username, password, or debit card details, which are transmitted directly to the attacker's server.")
                steps.add("3. Automated scripts use your credentials to initiate unauthorized account takeovers.")
            }
            ThreatCategory.FAKE_SUPPORT -> {
                steps.add("1. You follow instructions to download an APK or screen-sharing application like AnyDesk or RustDesk.")
                steps.add("2. The attacker observes your screen as you open your banking app and reads your confidential credentials.")
                steps.add("3. The attacker gains full device access to authorize transfers and suppress security SMS alerts.")
            }
            else -> {
                steps.add("1. Interacting with the unverified trigger confirms that your contact details are active and susceptible.")
                steps.add("2. The sender may attempt follow-up social engineering or route you to an external phishing portal.")
                steps.add("3. Continued interaction exposes personal credentials and financial data to unverified entities.")
            }
        }

        return WhatCouldHappenScenario(
            title = "Educational Threat Trajectory: ${category.displayName}",
            chainSteps = steps
        )
    }

    private fun constructVerificationGuidelines(
        signals: RiskSignals,
        urlAnalysis: URLAnalysisResult?,
        category: ThreatCategory
    ): List<VerificationGuideline> {
        val list = mutableListOf<VerificationGuideline>()

        if (signals.authorityImpersonation || signals.threatLanguage) {
            list.add(
                VerificationGuideline(
                    title = "Independent Authority Check",
                    recommendedAction = "Verify the status of your account directly through the official app or website.",
                    safeChannel = "Official institution mobile app or number on the back of your card",
                    strictWarning = "NEVER dial phone numbers or tap links embedded directly inside this message."
                )
            )
        }

        if (signals.otpRequest || signals.passwordRequest) {
            list.add(
                VerificationGuideline(
                    title = "Protect Security Codes",
                    recommendedAction = "Treat any request for an OTP or PIN as an immediate indicator of account hijacking.",
                    safeChannel = "Customer support number listed on official monthly statements",
                    strictWarning = "No genuine bank employee or customer support agent will EVER ask for your OTP."
                )
            )
        }

        if (signals.financialRequest || urlAnalysis?.isPaymentUri == true) {
            list.add(
                VerificationGuideline(
                    title = "Verify Payment Demand",
                    recommendedAction = "Log in to your electricity, utility, or banking portal through your existing trusted bookmark.",
                    safeChannel = "Official utility bill portal or registered customer service center",
                    strictWarning = "Never pay fees via personal UPI IDs or QR codes sent over SMS or messaging apps."
                )
            )
        }

        if (urlAnalysis?.isSuspiciousDomain == true) {
            list.add(
                VerificationGuideline(
                    title = "Inspect Website Domain",
                    recommendedAction = "Search for the company's verified domain using a known search engine rather than following links.",
                    safeChannel = "Official registered top-level domain (.gov.in, .bank.com, official verified site)",
                    strictWarning = "Do not submit credentials on domains registered with .top, .xyz, or irregular prefixes."
                )
            )
        }

        if (list.isEmpty()) {
            list.add(
                VerificationGuideline(
                    title = "Standard Verification Hygiene",
                    recommendedAction = "Maintain healthy skepticism and verify unexpected communications through independent channels.",
                    safeChannel = "Direct contact via previously verified official channels",
                    strictWarning = "Do not share personal details without confirming identity."
                )
            )
        }

        return list
    }

    private fun constructRiskTimeline(
        signals: RiskSignals,
        urlAnalysis: URLAnalysisResult?
    ): List<RiskTimelineStep> {
        return listOf(
            RiskTimelineStep(
                stage = "1. Attention Hook",
                label = "Contact Initiated",
                isDetected = true,
                description = "Attacker reaches out via unsolicited digital message or physical QR placement."
            ),
            RiskTimelineStep(
                stage = "2. Authority Pretext",
                label = "Claims Official Status",
                isDetected = signals.authorityImpersonation,
                description = if (signals.authorityImpersonation) "Detected claim of official banking or utility backing." else "No prominent authority claim detected."
            ),
            RiskTimelineStep(
                stage = "3. Fear & Intimidation",
                label = "Disconnection / Block Threat",
                isDetected = signals.threatLanguage,
                description = if (signals.threatLanguage) "Coercive threats used to provoke rapid, unthinking panic." else "No overt punitive threats found."
            ),
            RiskTimelineStep(
                stage = "4. Temporal Urgency",
                label = "Artificial Deadline",
                isDetected = signals.urgency,
                description = if (signals.urgency) "Enforces an immediate deadline to prevent independent consultation." else "Standard timeline."
            ),
            RiskTimelineStep(
                stage = "5. Financial Pressure",
                label = "Payment Demand",
                isDetected = signals.financialRequest || urlAnalysis?.isPaymentUri == true,
                description = if (signals.financialRequest || urlAnalysis?.isPaymentUri == true) "Requires immediate money transfer or QR scan." else "No monetary transaction demanded."
            ),
            RiskTimelineStep(
                stage = "6. Action / Exploitation",
                label = "Harmful Compliance",
                isDetected = signals.otpRequest || signals.passwordRequest || (urlAnalysis?.isSuspiciousDomain == true),
                description = if (signals.otpRequest || signals.passwordRequest) "Demands security code or directs to spoofed capture form." else "General call to action."
            )
        )
    }

    private fun constructSafeActions(
        signals: RiskSignals,
        urlAnalysis: URLAnalysisResult?,
        category: ThreatCategory
    ): List<SafeActionShortcut> {
        val actions = mutableListOf<SafeActionShortcut>()

        // 1. Copy sanitized details for cybercrime filing
        actions.add(
            SafeActionShortcut(
                id = "copy_report",
                title = "Copy Incident Summary",
                description = "Copies structured evidence details formatted for official cybercrime reporting portals.",
                actionType = SafeActionType.COPY_FOR_CYBERCRIME,
                payload = "Threat: ${category.displayName} | Flags: ${signals.signalDetails.joinToString("; ")}"
            )
        )

        // 2. If URL present, allow copying sanitized URL safely (never auto-open!)
        if (urlAnalysis != null) {
            actions.add(
                SafeActionShortcut(
                    id = "copy_url",
                    title = "Copy Sanitized Destination",
                    description = "Copies destination string without executing or previewing in browser.",
                    actionType = SafeActionType.COPY_SANITIZED_URL,
                    payload = urlAnalysis.normalizedUrl
                )
            )
        }

        // 3. Recommended Block
        actions.add(
            SafeActionShortcut(
                id = "block_sender",
                title = "Block & Report Sender",
                description = "Guide on blocking sender within your phone's SMS or messaging app.",
                actionType = SafeActionType.RECOMMEND_BLOCK
            )
        )

        // 4. Safe dismiss
        actions.add(
            SafeActionShortcut(
                id = "dismiss_safe",
                title = "Safely Discard & Exit",
                description = "Clear current analysis without interacting with suspect elements.",
                actionType = SafeActionType.SAFE_DISMISS
            )
        )

        return actions
    }
}
