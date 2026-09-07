package com.example.data.repository

import com.example.data.local.ScanHistoryDao
import com.example.data.local.ScanHistoryEntity
import com.example.domain.model.AttackStep
import com.example.domain.model.EvidenceItem
import com.example.domain.model.RiskClassification
import com.example.domain.model.RiskSignals
import com.example.domain.model.RiskTimelineStep
import com.example.domain.model.SafeActionShortcut
import com.example.domain.model.SafeActionType
import com.example.domain.model.ScanResult
import com.example.domain.model.ScanType
import com.example.domain.model.ScamDNA
import com.example.domain.model.ScamDNAStep
import com.example.domain.model.ThreatCategory
import com.example.domain.model.TrustBreakdown
import com.example.domain.model.VerificationGuideline
import com.example.domain.model.WhatCouldHappenScenario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ScanRepository(private val dao: ScanHistoryDao) {

    val allHistory: Flow<List<ScanResult>> = dao.getAllHistory().map { list ->
        list.map { it.toDomain() }
    }

    val recentHistory: Flow<List<ScanResult>> = dao.getRecentHistory().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun saveScan(scanResult: ScanResult): Long {
        val entity = scanResult.toEntity()
        return dao.insertScan(entity)
    }

    suspend fun deleteScan(id: Long) {
        dao.deleteById(id)
    }

    suspend fun clearHistory() {
        dao.clearAll()
    }

    private fun ScanResult.toEntity(): ScanHistoryEntity {
        // Privacy Guard: Strip any detected OTP or password occurrences before persisting
        var sanitized = if (rawContent.length > 100) rawContent.take(100) + "..." else rawContent
        sanitized = sanitized.replace(Regex("""\b\d{4,8}\b"""), "[REDACTED_NUMERIC]")

        val signalsSummary = if (signals.signalDetails.isNotEmpty()) {
            signals.signalDetails.joinToString(", ")
        } else {
            evidenceItems.joinToString(", ") { it.signalName }
        }

        return ScanHistoryEntity(
            id = id,
            timestamp = timestamp,
            scanType = scanType.name,
            summary = sanitized,
            destinationUrl = destinationUrl,
            riskScore = riskScore,
            classification = classification.name,
            threatCategory = threatCategory.name,
            intent = intent,
            detectedSignals = signalsSummary,
            manipulationTechniques = manipulationTechniques.joinToString(","),
            reasons = reasons.joinToString("||"),
            phantomInsight = phantomInsight,
            recommendedAction = recommendedAction,
            isAIEnhanced = isAIEnhanced
        )
    }

    private fun ScanHistoryEntity.toDomain(): ScanResult {
        val type = try {
            ScanType.valueOf(scanType)
        } catch (_: Exception) {
            ScanType.MESSAGE_TEXT
        }

        val riskEnum = try {
            RiskClassification.valueOf(classification)
        } catch (_: Exception) {
            RiskClassification.fromScore(riskScore)
        }

        val category = ThreatCategory.fromName(threatCategory)

        val manipList = if (manipulationTechniques.isBlank()) emptyList()
        else manipulationTechniques.split(",")

        val reasonList = if (reasons.isBlank()) emptyList()
        else reasons.split("||")

        val signalList = if (detectedSignals.isBlank()) emptyList()
        else detectedSignals.split(",").map { it.trim() }

        // Reconstruct evidence items from stored signals
        val reconstructedEvidence = signalList.map { sig ->
            EvidenceItem(
                signalName = sig,
                scoreContribution = when {
                    sig.contains("Credential", ignoreCase = true) || sig.contains("OTP", ignoreCase = true) -> 25
                    sig.contains("Domain", ignoreCase = true) || sig.contains("Payment", ignoreCase = true) -> 20
                    sig.contains("Authority", ignoreCase = true) || sig.contains("Threat", ignoreCase = true) -> 15
                    else -> 10
                },
                evidenceSnippet = summary,
                explanation = "Stored analytical flag: $sig"
            )
        }

        val trust = TrustBreakdown(
            identityTrustScore = if (riskScore > 60) 15 else if (riskScore > 30) 45 else 90,
            messageTrustScore = if (riskScore > 60) 20 else if (riskScore > 30) 50 else 90,
            destinationTrustScore = if (destinationUrl != null && riskScore > 50) 25 else 85,
            financialRiskScore = if (riskScore > 60) 80 else if (riskScore > 30) 40 else 10,
            urgencyScore = if (riskScore > 60) 85 else if (riskScore > 30) 45 else 10,
            credentialRiskScore = if (category == ThreatCategory.CREDENTIAL_THEFT || category == ThreatCategory.OTP_SOCIAL_ENGINEERING) 90 else 10,
            manipulationRiskScore = riskScore,
            summary = "Restored from local historical analysis record."
        )

        val scamDNA = ScamDNA(
            manipulationStrategy = phantomInsight,
            sequence = manipList.mapIndexed { idx, m ->
                ScamDNAStep(
                    order = idx + 1,
                    title = m.replace("_", " "),
                    tactic = m,
                    psychologicalPurpose = "Historical sequence step recorded by PHANTOM engine.",
                    evidenceSnippet = summary
                )
            }
        )

        val whatCouldHappen = WhatCouldHappenScenario(
            title = "Potential Exploitation Scenario (${category.displayName})",
            chainSteps = listOf(
                "1. Target interacts with the unverified trigger ($summary)",
                "2. Potential exposure of financial assets or credentials under false pretenses",
                "3. Compromise of associated communication accounts or funds"
            )
        )

        val guidelines = listOf(
            VerificationGuideline(
                title = "Independent Verification",
                recommendedAction = recommendedAction,
                safeChannel = "Official institution customer support channel",
                strictWarning = "Do not click unverified links or share authentication codes."
            )
        )

        val safeActions = listOf(
            SafeActionShortcut(
                id = "copy_report",
                title = "Copy Incident Summary",
                description = "Copies historical evidence details formatted for reporting.",
                actionType = SafeActionType.COPY_FOR_CYBERCRIME,
                payload = "Category: ${category.displayName} | Score: $riskScore | Signals: $detectedSignals"
            ),
            SafeActionShortcut(
                id = "dismiss_safe",
                title = "Safely Discard",
                description = "Exit back without further interaction.",
                actionType = SafeActionType.SAFE_DISMISS
            )
        )

        return ScanResult(
            id = id,
            timestamp = timestamp,
            scanType = type,
            rawContent = summary,
            destinationUrl = destinationUrl,
            riskScore = riskScore,
            classification = riskEnum,
            threatCategory = category,
            intent = intent,
            manipulationTechniques = manipList,
            reasons = reasonList,
            attackChain = buildSyntheticChain(manipList, intent, summary),
            scamDNA = scamDNA,
            evidenceItems = reconstructedEvidence,
            trustBreakdown = trust,
            verificationGuidelines = guidelines,
            whatCouldHappen = whatCouldHappen,
            riskTimeline = listOf(
                RiskTimelineStep("1. Attention", "Contact Initiated", true, "Recorded entry"),
                RiskTimelineStep("2. Action", "Exploitation Attempt", riskScore > 30, "Historical risk score: $riskScore/100")
            ),
            safeActions = safeActions,
            phantomInsight = phantomInsight,
            recommendedAction = recommendedAction,
            isAIEnhanced = isAIEnhanced,
            signals = RiskSignals(signalDetails = signalList)
        )
    }

    private fun buildSyntheticChain(
        manipulations: List<String>,
        intent: String,
        summary: String
    ): List<AttackStep> {
        val steps = mutableListOf<AttackStep>()
        var order = 1

        steps.add(
            AttackStep(
                stage = "ATTACKER ORIGIN",
                quoteOrEvidence = "External digital trigger (${summary.take(45)}...)",
                psychologicalHook = "Establish unauthorized initial contact",
                order = order++
            )
        )

        manipulations.forEach { manip ->
            steps.add(
                AttackStep(
                    stage = manip.replace("_", " "),
                    quoteOrEvidence = "Manipulative persuasion signature",
                    psychologicalHook = "Guide recipient toward non-standard action",
                    order = order++
                )
            )
        }

        steps.add(
            AttackStep(
                stage = "USER EXPLOITATION",
                quoteOrEvidence = "Goal: $intent",
                psychologicalHook = "Target complies under synthetic duress",
                order = order
            )
        )
        return steps
    }
}
