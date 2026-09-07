package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AIReasoningEngine
import com.example.camera.DetectionResult
import com.example.camera.MLKitScanner
import com.example.data.local.PhantomDatabase
import com.example.data.repository.ScanRepository
import com.example.data.auth.AuthRepository
import com.example.domain.model.AuthUser
import com.example.domain.model.AskPhantomQnA
import com.example.domain.model.RiskClassification
import com.example.domain.model.RiskSignals
import com.example.domain.model.ScanResult
import com.example.domain.model.ScanType
import com.example.security.RiskEngine
import com.example.security.SignalExtractor
import com.example.security.URLAnalyzer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class StageStatus {
    PENDING,
    ACTIVE,
    COMPLETED,
    FAILED
}

data class LiveAnalysisStage(
    val title: String,
    val detail: String,
    val status: StageStatus
)

class PhantomViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ScanRepository
    private val scanner: MLKitScanner = MLKitScanner(application)
    private val aiEngine: AIReasoningEngine = AIReasoningEngine()
    val authRepository: AuthRepository = AuthRepository.getInstance(application)
    val currentUser: StateFlow<AuthUser?> = authRepository.currentUser

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    init {
        val db = PhantomDatabase.getInstance(application)
        repository = ScanRepository(db.scanHistoryDao())
    }

    val allHistory: StateFlow<List<ScanResult>> = repository.allHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val recentHistory: StateFlow<List<ScanResult>> = repository.recentHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _currentResult = MutableStateFlow<ScanResult?>(null)
    val currentResult: StateFlow<ScanResult?> = _currentResult.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _liveDetection = MutableStateFlow<DetectionResult?>(null)
    val liveDetection: StateFlow<DetectionResult?> = _liveDetection.asStateFlow()

    private val _sensitivity = MutableStateFlow(1.0f) // 1.0 = Standard, 1.15 = High, 1.3 = Maximum
    val sensitivity: StateFlow<Float> = _sensitivity.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    private val _isAskingAi = MutableStateFlow(false)
    val isAskingAi: StateFlow<Boolean> = _isAskingAi.asStateFlow()

    // Live analysis progression stages
    private val _liveStages = MutableStateFlow<List<LiveAnalysisStage>>(emptyList())
    val liveStages: StateFlow<List<LiveAnalysisStage>> = _liveStages.asStateFlow()

    fun onLiveFrameDetected(detection: DetectionResult) {
        _liveDetection.value = detection
    }

    fun clearLiveDetection() {
        _liveDetection.value = null
    }

    fun analyzeDetection(
        detection: DetectionResult,
        overrideType: ScanType? = null
    ) {
        val content = if (detection.hasQr) {
            detection.qrCode ?: detection.textContent
        } else {
            detection.textContent
        }

        val type = overrideType ?: if (detection.hasQr) ScanType.QR_CODE else ScanType.MESSAGE_TEXT
        analyzeContent(content, type, destinationUrl = detection.qrCode)
    }

    fun analyzeDirectText(text: String) {
        val isUrl = text.startsWith("http://", ignoreCase = true) ||
                text.startsWith("https://", ignoreCase = true) ||
                text.startsWith("upi://", ignoreCase = true)

        val type = if (isUrl) ScanType.URL_DESTINATION else ScanType.MESSAGE_TEXT
        analyzeContent(text, type, destinationUrl = if (isUrl) text else null)
    }

    fun analyzeBitmap(bitmap: Bitmap) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            initStages("Media Frame Ingestion")
            updateStage(0, StageStatus.ACTIVE, "Extracting visual optical layers...")
            val detection = scanner.analyzeBitmap(bitmap)
            updateStage(0, StageStatus.COMPLETED, "Visual frame captured successfully")
            analyzeDetection(detection)
        }
    }

    fun runSyntheticScenario(
        title: String,
        content: String,
        scanType: ScanType,
        destinationUrl: String? = null
    ) {
        analyzeContent(content, scanType, destinationUrl)
    }

    private fun initStages(initialLabel: String) {
        _liveStages.value = listOf(
            LiveAnalysisStage("Digital Ingress", initialLabel, StageStatus.ACTIVE),
            LiveAnalysisStage("Heuristic Signal Extraction", "Token & Pattern Analysis", StageStatus.PENDING),
            LiveAnalysisStage("Destination Intelligence", "Structural URI & Domain Inspection", StageStatus.PENDING),
            LiveAnalysisStage("Risk & Trust Engine", "Deduplicated Mathematical Weighting", StageStatus.PENDING),
            LiveAnalysisStage("Scam DNA Synthesis", "Psychological Manipulation Modeling", StageStatus.PENDING)
        )
    }

    private fun updateStage(index: Int, status: StageStatus, detail: String? = null) {
        val current = _liveStages.value.toMutableList()
        if (index in current.indices) {
            val old = current[index]
            current[index] = old.copy(status = status, detail = detail ?: old.detail)
            _liveStages.value = current
        }
    }

    private fun analyzeContent(
        rawText: String,
        scanType: ScanType,
        destinationUrl: String?
    ) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _isSaved.value = false

            initStages("Processing content input")
            delay(120) // Provide subtle fluid transition for telemetry visual
            updateStage(0, StageStatus.COMPLETED, "Input captured: ${rawText.take(30)}...")

            // Stage 1: Heuristic Signal Extraction
            updateStage(1, StageStatus.ACTIVE, "Searching for coercive threats & financial hooks")
            val extractedSignals = SignalExtractor.extract(rawText)
            val extractedCount = extractedSignals.signalDetails.size
            updateStage(1, StageStatus.COMPLETED, "Identified $extractedCount threat signal indicators")

            // Stage 2: URL / QR Destination Intelligence
            updateStage(2, StageStatus.ACTIVE, "Inspecting URI structure & lookalike domains")
            val urlToInspect = destinationUrl ?: extractedSignals.extractedUrls.firstOrNull()
            val urlAnalysis = urlToInspect?.let { URLAnalyzer.analyze(it) }
            val domainFlags = if (urlAnalysis != null) {
                if (urlAnalysis.isVerifiedPlatform) "Verified platform: ${urlAnalysis.platformName ?: "Legitimate"}"
                else if (urlAnalysis.isSuspiciousDomain) "High-risk domain detected"
                else "Domain structure analyzed"
            } else "No external link present"
            updateStage(2, StageStatus.COMPLETED, domainFlags)

            // Combine signals
            val enrichedSignals = extractedSignals.copy(
                suspiciousDomain = extractedSignals.suspiciousDomain || (urlAnalysis?.isSuspiciousDomain == true),
                suspiciousUrlStructure = extractedSignals.suspiciousUrlStructure || (urlAnalysis?.isSuspiciousStructure == true)
            )

            // Stage 3: Risk Engine calculations
            updateStage(3, StageStatus.ACTIVE, "Calculating multi-vector trust breakdown")
            val riskEval = RiskEngine.evaluate(
                signals = enrichedSignals,
                urlAnalysis = urlAnalysis,
                sensitivityFactor = _sensitivity.value
            )
            updateStage(3, StageStatus.COMPLETED, "Risk score computed: ${riskEval.score}/100")

            // Stage 4: AI Reasoning Engine & Scam DNA
            updateStage(4, StageStatus.ACTIVE, "Synthesizing Scam DNA & defense trajectory")
            val aiOutput = aiEngine.reasonAboutThreat(
                rawContent = rawText,
                scanType = scanType,
                destinationUrl = destinationUrl,
                signals = enrichedSignals,
                urlAnalysis = urlAnalysis,
                evaluation = riskEval
            )
            updateStage(4, StageStatus.COMPLETED, "Scam DNA verified (${aiOutput.classification})")

            val finalResult = ScanResult(
                scanType = scanType,
                rawContent = rawText,
                destinationUrl = destinationUrl ?: urlAnalysis?.normalizedUrl,
                riskScore = riskEval.score,
                classification = riskEval.classification,
                threatCategory = riskEval.threatCategory,
                intent = if (riskEval.classification == RiskClassification.SAFE) {
                    if (urlAnalysis?.isVerifiedPlatform == true) "Verified ${urlAnalysis.platformName ?: "Platform"} Destination"
                    else "Benign Digital Content"
                } else aiOutput.intent,
                manipulationTechniques = aiOutput.manipulationTechniques,
                reasons = aiOutput.reasons,
                attackChain = aiOutput.attackChain,
                scamDNA = riskEval.scamDNA,
                evidenceItems = riskEval.evidenceItems,
                trustBreakdown = riskEval.trustBreakdown,
                verificationGuidelines = riskEval.verificationGuidelines,
                whatCouldHappen = riskEval.whatCouldHappen,
                riskTimeline = riskEval.riskTimeline,
                safeActions = riskEval.safeActions,
                urlIntelligence = urlAnalysis?.urlIntelligence,
                phantomInsight = aiOutput.phantomInsight,
                recommendedAction = aiOutput.recommendation,
                isAIEnhanced = aiOutput.isCloudEnhanced,
                signals = enrichedSignals
            )

            _currentResult.value = finalResult
            _isAnalyzing.value = false

            // Automatically persist to local Room database
            repository.saveScan(finalResult)
            _isSaved.value = true
        }
    }

    fun askPhantom(question: String) {
        val current = _currentResult.value ?: return
        if (question.isBlank()) return

        viewModelScope.launch {
            _isAskingAi.value = true
            val answer = aiEngine.askPhantom(question, current)

            val newQnA = AskPhantomQnA(question = question.trim(), answer = answer)
            val updatedHistory = current.qnaHistory + newQnA
            val updatedResult = current.copy(qnaHistory = updatedHistory)

            _currentResult.value = updatedResult
            _isAskingAi.value = false
        }
    }

    fun saveCurrentResult() {
        val current = _currentResult.value ?: return
        viewModelScope.launch {
            repository.saveScan(current)
            _isSaved.value = true
        }
    }

    fun selectHistoryItem(item: ScanResult) {
        _currentResult.value = item
    }

    fun deleteScan(id: Long) {
        viewModelScope.launch {
            repository.deleteScan(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun setSensitivity(level: Float) {
        _sensitivity.value = level
    }

    fun scannerInstance(): MLKitScanner = scanner

    // ==========================================
    // AUTHENTICATION OPERATIONS
    // ==========================================
    fun clearAuthError() {
        _authError.value = null
    }

    fun signInAsGuest(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            val result = authRepository.signInAsGuest()
            _authLoading.value = false
            result.onSuccess {
                onSuccess()
            }.onFailure { err ->
                _authError.value = err.message ?: "Guest session could not be established."
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
    }

    fun askAegis(question: String) = askPhantom(question)
}
