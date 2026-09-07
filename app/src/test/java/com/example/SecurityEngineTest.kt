package com.example

import com.example.domain.model.RiskClassification
import com.example.security.RiskEngine
import com.example.security.SignalExtractor
import com.example.security.URLAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityEngineTest {

    @Test
    fun `test electricity disconnection scam detection`() {
        val scamMessage = "Electricity bill unpaid! Power will be disconnected in 2 hours. Scan this QR code immediately and pay ₹500 verification fee to prevent disconnection."
        val signals = SignalExtractor.extract(scamMessage)

        assertTrue("Should detect urgency", signals.urgency)
        assertTrue("Should detect threat language", signals.threatLanguage)
        assertTrue("Should detect financial request", signals.financialRequest)

        val urlAnalysis = URLAnalyzer.analyze("upi://pay?pa=refund-agent88@okaxis&pn=Power+Officer&am=500&cu=INR")
        assertTrue("Should detect UPI payment URI", urlAnalysis.isPaymentUri)

        val eval = RiskEngine.evaluate(signals, urlAnalysis)
        assertTrue("Score should be high risk (>60)", eval.score >= 61)
    }

    @Test
    fun `test bank KYC phishing detection`() {
        val kycPhish = "Dear Customer, your SBI account has been locked today. Update PAN immediately at http://sbi-kyc-verify.top/login to avoid ₹5,000 penalty."
        val signals = SignalExtractor.extract(kycPhish)

        assertTrue("Should detect urgency", signals.urgency)
        assertTrue("Should detect authority", signals.authorityImpersonation)
        assertTrue("Should detect threat", signals.threatLanguage)

        val urlAnalysis = URLAnalyzer.analyze("http://sbi-kyc-verify.top/login")
        assertTrue("Should flag suspicious TLD or brand impersonation", urlAnalysis.isSuspiciousDomain)

        val eval = RiskEngine.evaluate(signals, urlAnalysis)
        assertEquals(RiskClassification.CRITICAL, eval.classification)
    }

    @Test
    fun `test benign appointment message is safe`() {
        val safeText = "Your consultation with Dr. Sharma is scheduled for tomorrow at 10:30 AM at Metro Health Clinic. For changes, visit our clinic or check official site."
        val signals = SignalExtractor.extract(safeText)
        val eval = RiskEngine.evaluate(signals)

        assertEquals(RiskClassification.SAFE, eval.classification)
        assertTrue("Score should be low (<=30)", eval.score <= 30)
    }
}
