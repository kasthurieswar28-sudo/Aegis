package com.example.ai

import com.example.BuildConfig
import com.example.domain.model.AttackStep
import com.example.domain.model.RiskClassification
import com.example.domain.model.RiskSignals
import com.example.domain.model.ScanResult
import com.example.domain.model.ScanType
import com.example.security.RiskEvaluation
import com.example.security.URLAnalysisResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AIAnalysisOutput(
    val classification: String,
    val intent: String,
    val manipulationTechniques: List<String>,
    val reasons: List<String>,
    val attackChain: List<AttackStep>,
    val phantomInsight: String,
    val recommendation: String,
    val isCloudEnhanced: Boolean
)

class AIReasoningEngine {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun reasonAboutThreat(
        rawContent: String,
        scanType: ScanType,
        destinationUrl: String?,
        signals: RiskSignals,
        urlAnalysis: URLAnalysisResult?,
        evaluation: RiskEvaluation
    ): AIAnalysisOutput = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val cloudOutput = callGeminiRest(rawContent, scanType, signals, evaluation)
                if (cloudOutput != null) {
                    return@withContext cloudOutput
                }
            } catch (_: Exception) {
                // Network or API failure -> seamlessly fallback to local deterministic reasoning
            }
        }

        // Offline / Local flagship reasoning fallback
        return@withContext generateOfflineReasoning(rawContent, scanType, destinationUrl, signals, evaluation)
    }

    suspend fun askPhantom(
        question: String,
        scanResult: ScanResult
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val answer = callGeminiAskPhantom(question, scanResult)
                if (!answer.isNullOrBlank()) {
                    return@withContext answer
                }
            } catch (_: Exception) {
                // Fall through to deterministic answering
            }
        }

        return@withContext generateOfflineAskAnswer(question, scanResult)
    }

    private fun callGeminiAskPhantom(question: String, scanResult: ScanResult): String? {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

        val evidenceSummary = scanResult.evidenceItems.joinToString("\n") {
            "- ${it.signalName} (+${it.scoreContribution} pts): ${it.evidenceSnippet}. ${it.explanation}"
        }

        val prompt = """
            You are "Ask AEGIS", an advanced, objective cybersecurity analyst assistant.
            The user is asking a question about a scanned digital item (SMS, email, QR code, or website).

            ANALYSIS CONTEXT:
            - Content Type: ${scanResult.scanType.displayName}
            - Risk Score: ${scanResult.riskScore}/100 (${scanResult.classification.label})
            - Threat Category: ${scanResult.threatCategory.displayName}
            - Destination: ${scanResult.destinationUrl ?: "None"}
            - Detected Evidence:
            $evidenceSummary
            - Manipulation Strategy: ${scanResult.scamDNA.manipulationStrategy}
            - Primary Recommendation: ${scanResult.recommendedAction}

            USER QUESTION: "$question"

            STRICT GUIDELINES:
            1. Answer using ONLY the provided analysis, detected signals, URL intelligence, and evidence.
            2. Do NOT invent or assume external details (such as whether a real person sent it, or the sender's exact name) that are not in the evidence.
            3. If the evidence is insufficient to answer the question, clearly state that the evidence does not provide that information, and state what is objectively known.
            4. Never advise the user to click unknown links, pay unverified fees, or share credentials.
            5. Keep the response direct, professional, concise (max 3-4 sentences), and free of jargon or hype.
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.2)
                put("maxOutputTokens", 300)
            })
        }

        val request = Request.Builder()
            .url(endpoint)
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) return null

        val responseString = response.body?.string() ?: return null
        val rootObj = JSONObject(responseString)
        val candidate = rootObj.optJSONArray("candidates")?.optJSONObject(0)
        return candidate?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text")?.trim()
    }

    private fun generateOfflineAskAnswer(question: String, scanResult: ScanResult): String {
        val q = question.lowercase()

        return when {
            q.contains("why") && (q.contains("suspicious") || q.contains("flag") || q.contains("risk")) -> {
                val topEvidence = scanResult.evidenceItems.take(2).joinToString(" and ") { it.signalName.lowercase() }
                if (topEvidence.isNotBlank()) {
                    "AEGIS flagged this primarily due to $topEvidence. Specifically: ${scanResult.scamDNA.manipulationStrategy}"
                } else {
                    "AEGIS evaluated this content against heuristic threat patterns. No prominent deception signatures were detected."
                }
            }
            q.contains("what") && (q.contains("attacker") || q.contains("trying") || q.contains("goal")) -> {
                "Based on the detected indicators, the primary goal appears to be ${scanResult.threatCategory.displayName.lowercase()}. ${scanResult.scamDNA.manipulationStrategy}"
            }
            q.contains("what should i do") || q.contains("next") || q.contains("action") || q.contains("how to proceed") -> {
                "Recommended action: ${scanResult.recommendedAction} Avoid clicking links or sharing authentication codes. If in doubt, contact the alleged organization via official channels."
            }
            q.contains("safe to click") || q.contains("safe to pay") || q.contains("safe") -> {
                if (scanResult.riskScore >= 31) {
                    "No. The calculated risk score is ${scanResult.riskScore}/100 (${scanResult.classification.label}). Interacting with this content carries significant risk of fraud or credential theft."
                } else {
                    "The analysis detected no prominent attack signatures (Risk: ${scanResult.riskScore}/100). However, always ensure the destination matches the official website before entering personal details."
                }
            }
            q.contains("who sent") || q.contains("sender") || q.contains("identity") -> {
                "AEGIS cannot verify the sender's real-world identity from content alone. However, the message claims authority backing without cryptographically verifiable proof, a common hallmark of spoofing."
            }
            else -> {
                "Based on available evidence, this item was classified as ${scanResult.classification.label} (${scanResult.threatCategory.displayName}) with a risk estimate of ${scanResult.riskScore}/100. Key recommendation: ${scanResult.recommendedAction}"
            }
        }
    }

    private fun callGeminiRest(
        rawContent: String,
        scanType: ScanType,
        signals: RiskSignals,
        evaluation: RiskEvaluation
    ): AIAnalysisOutput? {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

        val prompt = """
            You are AEGIS, a flagship cybersecurity AI engine specializing in social engineering, scam detection, and cognitive manipulation analysis.
            Analyze the following digital content:
            
            Input Type: ${scanType.displayName}
            Content: "$rawContent"
            Pre-detected Threat Signals: ${signals.signalDetails.joinToString(", ")}
            Pre-calculated Risk Score: ${evaluation.score}/100 (${evaluation.classification.name})
            Threat Category: ${evaluation.threatCategory.displayName}

            Answer strictly in valid JSON with no markdown formatting:
            {
              "classification": "${evaluation.classification.name}",
              "riskScore": ${evaluation.score},
              "intent": "SHORT_INTENT_CATEGORY",
              "manipulation": ["TECHNIQUE_1", "TECHNIQUE_2"],
              "reasons": ["Point 1", "Point 2", "Point 3"],
              "attackChain": [
                {"stage": "STAGE_NAME", "quoteOrEvidence": "QUOTE", "psychologicalHook": "EXPLANATION", "order": 1}
              ],
              "phantomInsight": "Deep psychological insight into attacker intent",
              "recommendation": "Precise actionable advice"
            }
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.2)
            })
        }

        val request = Request.Builder()
            .url(endpoint)
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) return null

        val responseString = response.body?.string() ?: return null
        val rootObj = JSONObject(responseString)
        val candidate = rootObj.optJSONArray("candidates")?.optJSONObject(0)
        val text = candidate?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text") ?: return null

        val parsed = JSONObject(text.trim())
        val intent = parsed.optString("intent", evaluation.threatCategory.displayName)
        val manipList = mutableListOf<String>()
        val manipArray = parsed.optJSONArray("manipulation")
        if (manipArray != null) {
            for (i in 0 until manipArray.length()) {
                manipList.add(manipArray.getString(i))
            }
        }

        val reasonsList = mutableListOf<String>()
        val reasonsArray = parsed.optJSONArray("reasons")
        if (reasonsArray != null) {
            for (i in 0 until reasonsArray.length()) {
                reasonsList.add(reasonsArray.getString(i))
            }
        }

        val chainList = mutableListOf<AttackStep>()
        val chainArray = parsed.optJSONArray("attackChain")
        if (chainArray != null) {
            for (i in 0 until chainArray.length()) {
                val stepObj = chainArray.getJSONObject(i)
                chainList.add(
                    AttackStep(
                        stage = stepObj.optString("stage", "Exploitation Step"),
                        quoteOrEvidence = stepObj.optString("quoteOrEvidence", "Evidence detected"),
                        psychologicalHook = stepObj.optString("psychologicalHook", "Psychological trigger"),
                        order = stepObj.optInt("order", i + 1)
                    )
                )
            }
        }

        return AIAnalysisOutput(
            classification = parsed.optString("classification", evaluation.classification.name),
            intent = intent,
            manipulationTechniques = if (manipList.isEmpty()) evaluation.primaryFlags else manipList,
            reasons = if (reasonsList.isEmpty()) evaluation.primaryFlags else reasonsList,
            attackChain = if (chainList.isEmpty()) generateFallbackChain(signals, evaluation) else chainList,
            phantomInsight = parsed.optString("phantomInsight", evaluation.scamDNA.manipulationStrategy),
            recommendation = parsed.optString("recommendation", evaluation.recommendation),
            isCloudEnhanced = true
        )
    }

    private fun generateOfflineReasoning(
        rawContent: String,
        scanType: ScanType,
        destinationUrl: String?,
        signals: RiskSignals,
        evaluation: RiskEvaluation
    ): AIAnalysisOutput {
        val manipulationList = mutableListOf<String>()
        if (signals.authorityImpersonation) manipulationList.add("AUTHORITY_IMPERSONATION")
        if (signals.threatLanguage) manipulationList.add("COERCION_AND_FEAR")
        if (signals.urgency) manipulationList.add("ARTIFICIAL_URGENCY")
        if (signals.financialRequest) manipulationList.add("FINANCIAL_EXTRACTION")
        if (signals.otpRequest || signals.passwordRequest) manipulationList.add("CREDENTIAL_HARVESTING")
        if (signals.rewardGreedTrap) manipulationList.add("GREED_EXPLOITATION")

        val chain = generateFallbackChain(signals, evaluation)

        return AIAnalysisOutput(
            classification = evaluation.classification.name,
            intent = if (evaluation.classification == RiskClassification.SAFE) "Benign Digital Content" else evaluation.threatCategory.displayName,
            manipulationTechniques = if (evaluation.classification == RiskClassification.SAFE) emptyList() else if (manipulationList.isEmpty()) listOf("UNVERIFIED_CONTENT") else manipulationList,
            reasons = evaluation.primaryFlags,
            attackChain = chain,
            phantomInsight = evaluation.scamDNA.manipulationStrategy,
            recommendation = evaluation.recommendation,
            isCloudEnhanced = false
        )
    }

    private fun generateFallbackChain(signals: RiskSignals, evaluation: RiskEvaluation): List<AttackStep> {
        if (evaluation.classification == RiskClassification.SAFE) {
            return listOf(
                AttackStep(
                    stage = "BENIGN INGRESS",
                    quoteOrEvidence = "Standard digital content",
                    psychologicalHook = "Normal non-deceptive communication",
                    order = 1
                )
            )
        }

        val steps = mutableListOf<AttackStep>()
        var order = 1

        steps.add(
            AttackStep(
                stage = "INITIAL INGRESS",
                quoteOrEvidence = "Unsolicited digital trigger",
                psychologicalHook = "Catches recipient off-guard through unfamiliar communication",
                order = order++
            )
        )

        if (signals.authorityImpersonation) {
            steps.add(
                AttackStep(
                    stage = "FALSE PRETEXT",
                    quoteOrEvidence = "Claims recognized institutional affiliation",
                    psychologicalHook = "Disarms natural skepticism by leveraging institutional trust",
                    order = order++
                )
            )
        }

        if (signals.threatLanguage) {
            steps.add(
                AttackStep(
                    stage = "PSYCHOLOGICAL COERCION",
                    quoteOrEvidence = "Threatens immediate negative consequences",
                    psychologicalHook = "Triggers panic to suppress analytical scrutiny",
                    order = order++
                )
            )
        }

        if (signals.urgency) {
            steps.add(
                AttackStep(
                    stage = "TEMPORAL CONSTRAINT",
                    quoteOrEvidence = "Enforces tight compliance deadline",
                    psychologicalHook = "Prevents target from consulting official helpdesks",
                    order = order++
                )
            )
        }

        if (signals.otpRequest || signals.passwordRequest) {
            steps.add(
                AttackStep(
                    stage = "AUTHENTICATION COMPROMISE",
                    quoteOrEvidence = "Demands OTP or secret PIN",
                    psychologicalHook = "Harvests final key needed for complete account takeover",
                    order = order++
                )
            )
        } else if (signals.financialRequest) {
            steps.add(
                AttackStep(
                    stage = "PAYMENT DIVERSION",
                    quoteOrEvidence = "Directs money transfer or QR code authorization",
                    psychologicalHook = "Diverts funds into unmonitored recipient accounts",
                    order = order++
                )
            )
        }

        return steps
    }
}
