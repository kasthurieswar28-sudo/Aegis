package com.example.security

import com.example.domain.model.RiskSignals
import java.util.regex.Pattern

object SignalExtractor {

    private val URL_PATTERN = Pattern.compile(
        """https?://[a-zA-Z0-9.\-_~:/?#\[\]@!$&'()*+,;=%]+""",
        Pattern.CASE_INSENSITIVE
    )

    private val PHONE_PATTERN = Pattern.compile(
        """(\+?\d{1,3}[-.\s]?)?\(?\d{3}\)?[-.\s]?\d{3}[-.\s]?\d{4}|\b\d{10}\b"""
    )

    private val AMOUNT_PATTERN = Pattern.compile(
        """(₹|\$|€|£|INR|USD)\s?[\d,]+(\.\d{1,2})?|\b[\d,]+(\.\d{1,2})?\s?(₹|rupees|dollars|inr|usd)\b""",
        Pattern.CASE_INSENSITIVE
    )

    // Token Sets
    private val OTP_TOKENS = setOf(
        "otp", "one time password", "verification code", "security code",
        "share code", "secret pin", "sms code", "do not share", "auth code"
    )

    private val PASSWORD_TOKENS = setOf(
        "password", "passcode", "atm pin", "cvv", "card details", "login credentials",
        "netbanking password", "mpin", "security question"
    )

    private val FINANCIAL_TOKENS = setOf(
        "send money", "transfer money", "credit card", "debit card", "bank account",
        "payment pending", "wire transfer", "upi pin", "enter pin to receive", "paytm pin",
        "scan to pay", "pay to unblock", "refund claim", "wallet balance transfer"
    )

    private val URGENCY_TOKENS = setOf(
        "immediately", "within 24 hours", "urgently",
        "within 10 minutes", "expires soon", "act fast", "limited time",
        "blocked today", "suspended immediately", "hours left", "action required"
    )

    private val THREAT_TOKENS = setOf(
        "account blocked", "sim blocked", "services suspended", "suspended", "legal action", "arrest", "police",
        "penalty", "frozen", "compromised", "deactivated", "court warrant",
        "police investigation", "jail", "lawsuit", "account closed",
        "electricity disconnect", "disconnection notice", "locked account",
        "disconnected", "disconnection", "locked"
    )

    private val AUTHORITY_TOKENS = setOf(
        "tax department", "bank security", "official notice", "federal reserve",
        "customer care helpline", "cyber crime cell", "telecom department",
        "department of telecommunications", "income tax", "rbi", "state bank of india", "sbi official",
        "sbi", "fbi", "irs", "fraud prevention department", "security department"
    )

    private val REWARD_TOKENS = setOf(
        "lottery", "cashback", "winner", "prize", "congratulations",
        "bonus reward", "free gift", "claim now", "selected for award", "jackpot", "you won"
    )

    private val SUSPICIOUS_INSTRUCTIONS = setOf(
        "anydesk", "teamviewer", "quicksupport", "rustdesk", "install apk",
        "download app", "forward sms", "scan to receive", "enter pin to receive",
        "screen share", "remote access", "allow permission", "unknown source"
    )

    private val INDEPENDENT_VERIFICATION_TOKENS = setOf(
        "visit your nearest branch", "check official website", "call the number on back of card",
        "verified in official app", "informational only", "do not click third party links",
        "official communication"
    )

    fun extract(rawText: String): RiskSignals {
        val lower = rawText.lowercase()
        val details = mutableListOf<String>()

        val urls = extractUrls(rawText)
        val phones = extractPhones(rawText)
        val amounts = extractAmounts(rawText)

        val hasOtp = matchesAny(lower, OTP_TOKENS)
        if (hasOtp) details.add("Requests OTP / One-Time Security Code")

        val hasPassword = matchesAny(lower, PASSWORD_TOKENS)
        if (hasPassword) details.add("Requests Confidential Password or PIN")

        val hasFinancial = matchesAny(lower, FINANCIAL_TOKENS) || amounts.isNotEmpty()
        if (hasFinancial) details.add("Demands Financial Transaction or Money Movement")

        val hasUrgency = matchesAny(lower, URGENCY_TOKENS)
        if (hasUrgency) details.add("Employs High Urgency to induce panic")

        val hasThreat = matchesAny(lower, THREAT_TOKENS)
        if (hasThreat) details.add("Uses Coercive Threat / Legal Intimidation Language")

        val hasAuthority = matchesAny(lower, AUTHORITY_TOKENS)
        if (hasAuthority) details.add("Impersonates Official or Financial Authority")

        val hasReward = matchesAny(lower, REWARD_TOKENS)
        if (hasReward) details.add("Offers Unsolicited Reward / Greed Trap")

        val hasSuspiciousInstructions = matchesAny(lower, SUSPICIOUS_INSTRUCTIONS)
        if (hasSuspiciousInstructions) details.add("Instructs installing Remote Access or Unverified APK")

        val hasVerificationEvidence = matchesAny(lower, INDEPENDENT_VERIFICATION_TOKENS)
        if (hasVerificationEvidence) details.add("Contains safe independent verification guidance")

        return RiskSignals(
            financialRequest = hasFinancial,
            otpRequest = hasOtp,
            passwordRequest = hasPassword,
            urgency = hasUrgency,
            authorityImpersonation = hasAuthority,
            threatLanguage = hasThreat,
            suspiciousDomain = false, // Will be set by URLAnalyzer
            suspiciousUrlStructure = false,
            rewardGreedTrap = hasReward,
            suspiciousApkOrRemoteControl = hasSuspiciousInstructions,
            independentVerificationEvidence = hasVerificationEvidence,
            extractedUrls = urls,
            extractedPhones = phones,
            extractedAmounts = amounts,
            signalDetails = details
        )
    }

    private fun matchesAny(text: String, tokens: Set<String>): Boolean {
        return tokens.any { token ->
            if (token.contains(" ")) {
                text.contains(token, ignoreCase = true)
            } else {
                val regex = Regex("""(?i)\b${Regex.escape(token)}\b""")
                regex.containsMatchIn(text)
            }
        }
    }

    private fun extractUrls(text: String): List<String> {
        val matcher = URL_PATTERN.matcher(text)
        val list = mutableListOf<String>()
        while (matcher.find()) {
            list.add(matcher.group())
        }
        return list
    }

    private fun extractPhones(text: String): List<String> {
        val matcher = PHONE_PATTERN.matcher(text)
        val list = mutableListOf<String>()
        while (matcher.find()) {
            val phone = matcher.group().trim()
            if (phone.length >= 7) {
                list.add(phone)
            }
        }
        return list
    }

    private fun extractAmounts(text: String): List<String> {
        val matcher = AMOUNT_PATTERN.matcher(text)
        val list = mutableListOf<String>()
        while (matcher.find()) {
            list.add(matcher.group().trim())
        }
        return list
    }
}
