package com.example.security

import com.example.domain.model.URLIntelligence
import java.net.URI

data class URLAnalysisResult(
    val normalizedUrl: String,
    val isSuspiciousDomain: Boolean,
    val isSuspiciousStructure: Boolean,
    val isKnownPhishingKeyword: Boolean,
    val isPaymentUri: Boolean,
    val isVerifiedPlatform: Boolean = false,
    val platformName: String? = null,
    val payeeName: String? = null,
    val findings: List<String>,
    val urlIntelligence: URLIntelligence
)

object URLAnalyzer {

    private val TRUSTED_PLATFORMS = mapOf(
        "instagram.com" to "Instagram",
        "ig.me" to "Instagram",
        "instagr.am" to "Instagram",
        "cdninstagram.com" to "Instagram",
        "facebook.com" to "Facebook",
        "fb.com" to "Facebook",
        "fb.me" to "Facebook",
        "whatsapp.com" to "WhatsApp",
        "wa.me" to "WhatsApp",
        "twitter.com" to "Twitter / X",
        "x.com" to "Twitter / X",
        "t.co" to "Twitter / X",
        "linkedin.com" to "LinkedIn",
        "lnkd.in" to "LinkedIn",
        "youtube.com" to "YouTube",
        "youtu.be" to "YouTube",
        "google.com" to "Google",
        "goo.gl" to "Google",
        "forms.gle" to "Google Forms",
        "apple.com" to "Apple",
        "apple.co" to "Apple",
        "spotify.com" to "Spotify",
        "spoti.fi" to "Spotify",
        "github.com" to "GitHub",
        "github.io" to "GitHub Pages",
        "microsoft.com" to "Microsoft",
        "live.com" to "Microsoft",
        "office.com" to "Microsoft Office",
        "outlook.com" to "Microsoft Outlook",
        "reddit.com" to "Reddit",
        "pinterest.com" to "Pinterest",
        "pin.it" to "Pinterest",
        "telegram.org" to "Telegram",
        "t.me" to "Telegram",
        "wikipedia.org" to "Wikipedia",
        "amazon.com" to "Amazon",
        "amazon.in" to "Amazon India",
        "amzn.to" to "Amazon",
        "netflix.com" to "Netflix",
        "linktr.ee" to "Linktree",
        "qrco.de" to "QR Code Generator",
        "flowcode.com" to "Flowcode",
        "qr-code-generator.com" to "QR Code Generator"
    )

    private val SUSPICIOUS_TLDS = setOf(
        "xyz", "top", "work", "click", "buzz", "rest", "gq", "tk", "ml", "cf",
        "fit", "live", "cc", "su", "cam", "monster", "icu", "vip", "cfd"
    )

    private val SUSPICIOUS_SHORTENERS = setOf(
        "bit.ly", "tinyurl.com", "is.gd", "cutt.ly", "rb.gy", "ow.ly",
        "shorturl.at", "soo.gd", "v.gd"
    )

    private val BRAND_TARGETS = listOf(
        "sbi" to "State Bank of India",
        "hdfc" to "HDFC Bank",
        "icici" to "ICICI Bank",
        "axis" to "Axis Bank",
        "pnb" to "Punjab National Bank",
        "paypal" to "PayPal",
        "amazon" to "Amazon",
        "google" to "Google",
        "microsoft" to "Microsoft",
        "netflix" to "Netflix",
        "apple" to "Apple",
        "indiapost" to "India Post",
        "fedex" to "FedEx",
        "dhl" to "DHL",
        "whatsapp" to "WhatsApp",
        "telegram" to "Telegram"
    )

    private val SUSPICIOUS_PATH_KEYWORDS = listOf(
        "login", "verify", "secure", "account-update", "recover", "banking",
        "auth", "wp-content", "wallet-connect", "claim-reward", "kyc-update",
        "card-check", "pan-link", "refund-claim", "disconnection-prevent"
    )

    private fun findTrustedPlatform(host: String): String? {
        val cleanHost = host.lowercase().trim()
        for ((domain, name) in TRUSTED_PLATFORMS) {
            if (cleanHost == domain || cleanHost.endsWith(".$domain")) {
                return name
            }
        }
        if (cleanHost.matches(Regex("""^(.*\.)?google\.(co\.[a-z]{2}|[a-z]{2,3})$""")) ||
            cleanHost.matches(Regex("""^(.*\.)?amazon\.(co\.[a-z]{2}|[a-z]{2,3})$"""))) {
            return if (cleanHost.contains("google")) "Google" else "Amazon"
        }
        return null
    }

    fun analyze(inputUrl: String): URLAnalysisResult {
        val trimmed = inputUrl.trim()
        val findings = mutableListOf<String>()
        val structuralFlags = mutableListOf<String>()

        // 1. UPI Payment URI Analysis
        if (trimmed.startsWith("upi://", ignoreCase = true)) {
            val payee = extractQueryParam(trimmed, "pn") ?: "Unknown Payee"
            val pa = extractQueryParam(trimmed, "pa") ?: "Unknown VPA"
            val am = extractQueryParam(trimmed, "am")

            findings.add("Direct UPI Payment Intent URI (Payee: $payee)")
            findings.add("VPA Endpoint: $pa")
            if (am != null) findings.add("Pre-filled Amount: ₹$am")
            findings.add("Triggering this QR prompts the user to enter their UPI PIN to authorize money deduction.")

            structuralFlags.add("Protocol: upi:// payment intent")
            structuralFlags.add("Target VPA: $pa")

            val intel = URLIntelligence(
                originalUrl = trimmed,
                hostname = "UPI Payment Handler",
                tld = "N/A",
                isSuspiciousTld = false,
                hasLookalikeDomain = false,
                hasSuspiciousSubdomain = false,
                hasCharacterSubstitutions = false,
                isIpAddress = false,
                isPunycode = false,
                excessiveComplexity = false,
                isPaymentUri = true,
                payeeName = payee,
                structuralFlags = structuralFlags
            )

            return URLAnalysisResult(
                normalizedUrl = trimmed,
                isSuspiciousDomain = false,
                isSuspiciousStructure = true,
                isKnownPhishingKeyword = false,
                isPaymentUri = true,
                isVerifiedPlatform = false,
                platformName = "UPI Payment",
                payeeName = payee,
                findings = findings,
                urlIntelligence = intel
            )
        }

        // 2. Wi-Fi Setup QR Analysis
        if (trimmed.startsWith("WIFI:", ignoreCase = true)) {
            val ssid = Regex("""S:([^;]+)""", RegexOption.IGNORE_CASE).find(trimmed)?.groupValues?.get(1) ?: "Network"
            val type = Regex("""T:([^;]+)""", RegexOption.IGNORE_CASE).find(trimmed)?.groupValues?.get(1) ?: "WPA"
            findings.add("Legitimate Wi-Fi Network Setup QR code")
            findings.add("Target SSID: $ssid (Security: $type)")
            findings.add("Standard network configuration protocol; no malicious redirection detected.")
            structuralFlags.add("Protocol: Wi-Fi Setup barcode")
            structuralFlags.add("Network SSID: $ssid")

            val intel = URLIntelligence(
                originalUrl = trimmed,
                hostname = "Wi-Fi Config ($ssid)",
                tld = "N/A",
                isSuspiciousTld = false,
                hasLookalikeDomain = false,
                hasSuspiciousSubdomain = false,
                hasCharacterSubstitutions = false,
                isIpAddress = false,
                isPunycode = false,
                excessiveComplexity = false,
                isPaymentUri = false,
                structuralFlags = structuralFlags
            )
            return URLAnalysisResult(
                normalizedUrl = trimmed,
                isSuspiciousDomain = false,
                isSuspiciousStructure = false,
                isKnownPhishingKeyword = false,
                isPaymentUri = false,
                isVerifiedPlatform = true,
                platformName = "Wi-Fi Setup",
                findings = findings,
                urlIntelligence = intel
            )
        }

        // 3. Contact Card (vCard / MeCard)
        if (trimmed.startsWith("BEGIN:VCARD", ignoreCase = true) || trimmed.startsWith("MECARD:", ignoreCase = true)) {
            val name = Regex("""(?:FN|N):([^;\n\r]+)""", RegexOption.IGNORE_CASE).find(trimmed)?.groupValues?.get(1) ?: "Contact"
            findings.add("Legitimate Contact vCard / MeCard QR code")
            findings.add("Contact Identity: $name")
            findings.add("Standard electronic business card format; no hostile redirection.")
            structuralFlags.add("Protocol: Contact Card (vCard)")

            val intel = URLIntelligence(
                originalUrl = trimmed,
                hostname = "Contact Card ($name)",
                tld = "N/A",
                isSuspiciousTld = false,
                hasLookalikeDomain = false,
                hasSuspiciousSubdomain = false,
                hasCharacterSubstitutions = false,
                isIpAddress = false,
                isPunycode = false,
                excessiveComplexity = false,
                isPaymentUri = false,
                structuralFlags = structuralFlags
            )
            return URLAnalysisResult(
                normalizedUrl = trimmed,
                isSuspiciousDomain = false,
                isSuspiciousStructure = false,
                isKnownPhishingKeyword = false,
                isPaymentUri = false,
                isVerifiedPlatform = true,
                platformName = "Contact Card",
                findings = findings,
                urlIntelligence = intel
            )
        }

        // 4. Telephone / Email / SMS / Geo Shortcuts
        if (trimmed.startsWith("tel:", ignoreCase = true) ||
            trimmed.startsWith("mailto:", ignoreCase = true) ||
            trimmed.startsWith("sms:", ignoreCase = true) ||
            trimmed.startsWith("geo:", ignoreCase = true)) {
            val scheme = trimmed.substringBefore(":").lowercase()
            findings.add("Standard system action shortcut ($scheme)")
            structuralFlags.add("Protocol: $scheme action")

            val intel = URLIntelligence(
                originalUrl = trimmed,
                hostname = "System Action ($scheme)",
                tld = "N/A",
                isSuspiciousTld = false,
                hasLookalikeDomain = false,
                hasSuspiciousSubdomain = false,
                hasCharacterSubstitutions = false,
                isIpAddress = false,
                isPunycode = false,
                excessiveComplexity = false,
                isPaymentUri = false,
                structuralFlags = structuralFlags
            )
            return URLAnalysisResult(
                normalizedUrl = trimmed,
                isSuspiciousDomain = false,
                isSuspiciousStructure = false,
                isKnownPhishingKeyword = false,
                isPaymentUri = false,
                isVerifiedPlatform = true,
                platformName = "Action Shortcut",
                findings = findings,
                urlIntelligence = intel
            )
        }

        // 5. Social Media In-App Schemes (e.g. instagram://, whatsapp://, etc.)
        if (trimmed.startsWith("instagram://", ignoreCase = true) ||
            trimmed.startsWith("fb://", ignoreCase = true) ||
            trimmed.startsWith("whatsapp://", ignoreCase = true) ||
            trimmed.startsWith("tg://", ignoreCase = true) ||
            trimmed.startsWith("market://", ignoreCase = true)) {
            val appName = when {
                trimmed.startsWith("instagram://", ignoreCase = true) -> "Instagram"
                trimmed.startsWith("whatsapp://", ignoreCase = true) -> "WhatsApp"
                trimmed.startsWith("fb://", ignoreCase = true) -> "Facebook"
                trimmed.startsWith("tg://", ignoreCase = true) -> "Telegram"
                else -> "Application Deep Link"
            }
            findings.add("Verified direct application deep-link ($appName)")
            findings.add("Points directly to verified $appName app resource.")
            structuralFlags.add("Protocol: $appName deep-link")

            val intel = URLIntelligence(
                originalUrl = trimmed,
                hostname = "$appName App",
                tld = "N/A",
                isSuspiciousTld = false,
                hasLookalikeDomain = false,
                hasSuspiciousSubdomain = false,
                hasCharacterSubstitutions = false,
                isIpAddress = false,
                isPunycode = false,
                excessiveComplexity = false,
                isPaymentUri = false,
                structuralFlags = structuralFlags
            )
            return URLAnalysisResult(
                normalizedUrl = trimmed,
                isSuspiciousDomain = false,
                isSuspiciousStructure = false,
                isKnownPhishingKeyword = false,
                isPaymentUri = false,
                isVerifiedPlatform = true,
                platformName = appName,
                findings = findings,
                urlIntelligence = intel
            )
        }

        // 6. Plain text or non-URL data (e.g., ticket ID, serial number, simple note)
        val isLikelyUrl = (trimmed.startsWith("http://", ignoreCase = true) ||
                trimmed.startsWith("https://", ignoreCase = true) ||
                (trimmed.contains(".") && !trimmed.contains(" ") && trimmed.length < 150))

        if (!isLikelyUrl) {
            findings.add("Plain text informational content; no external web redirection detected.")
            structuralFlags.add("Content: Plain Text")

            val intel = URLIntelligence(
                originalUrl = trimmed,
                hostname = "Informational Text",
                tld = "N/A",
                isSuspiciousTld = false,
                hasLookalikeDomain = false,
                hasSuspiciousSubdomain = false,
                hasCharacterSubstitutions = false,
                isIpAddress = false,
                isPunycode = false,
                excessiveComplexity = false,
                isPaymentUri = false,
                structuralFlags = structuralFlags
            )
            return URLAnalysisResult(
                normalizedUrl = trimmed,
                isSuspiciousDomain = false,
                isSuspiciousStructure = false,
                isKnownPhishingKeyword = false,
                isPaymentUri = false,
                isVerifiedPlatform = true,
                platformName = "Informational Data",
                findings = findings,
                urlIntelligence = intel
            )
        }

        var isSuspiciousDomain = false
        var isSuspiciousStructure = false
        var isPhishingKeyword = false

        val uri = try {
            val parsed = if (!trimmed.startsWith("http://", ignoreCase = true) && !trimmed.startsWith("https://", ignoreCase = true)) {
                URI("https://$trimmed")
            } else {
                URI(trimmed)
            }
            parsed
        } catch (_: Exception) {
            val hasObviousExploit = trimmed.contains("@") || trimmed.count { it == '/' } > 8
            if (hasObviousExploit) {
                findings.add("Malformed URL format with potential obfuscation")
            } else {
                findings.add("Non-standard text or identifier; no malicious redirection detected")
            }
            val intel = URLIntelligence(
                originalUrl = trimmed,
                hostname = "Unformatted Text",
                tld = "N/A",
                isSuspiciousTld = false,
                hasLookalikeDomain = false,
                hasSuspiciousSubdomain = false,
                hasCharacterSubstitutions = false,
                isIpAddress = false,
                isPunycode = false,
                excessiveComplexity = false,
                isPaymentUri = false,
                structuralFlags = listOf("Format: Plain/Unformatted String")
            )
            return URLAnalysisResult(
                normalizedUrl = trimmed,
                isSuspiciousDomain = hasObviousExploit,
                isSuspiciousStructure = hasObviousExploit,
                isKnownPhishingKeyword = false,
                isPaymentUri = false,
                isVerifiedPlatform = !hasObviousExploit,
                platformName = if (hasObviousExploit) null else "Plain Content",
                findings = findings,
                urlIntelligence = intel
            )
        }

        val host = uri.host?.lowercase() ?: ""
        val path = uri.path?.lowercase() ?: ""

        // 7. Check against Verified Platform Registry (e.g. Instagram, Facebook, Google, YouTube, etc.)
        val trustedPlatform = findTrustedPlatform(host)
        if (trustedPlatform != null) {
            findings.add("Verified official destination: $trustedPlatform ($host)")
            findings.add("Domain belongs to verified legitimate platform registry; safe to browse.")
            structuralFlags.add("Registry: Verified Official Platform ($trustedPlatform)")

            val intel = URLIntelligence(
                originalUrl = uri.toString(),
                hostname = host,
                tld = host.substringAfterLast(".", ""),
                isSuspiciousTld = false,
                hasLookalikeDomain = false,
                lookalikeBrand = null,
                hasSuspiciousSubdomain = false,
                subdomains = host.split("."),
                hasCharacterSubstitutions = false,
                isIpAddress = false,
                isPunycode = false,
                excessiveComplexity = false,
                isPaymentUri = false,
                structuralFlags = structuralFlags
            )

            return URLAnalysisResult(
                normalizedUrl = uri.toString(),
                isSuspiciousDomain = false,
                isSuspiciousStructure = false,
                isKnownPhishingKeyword = false,
                isPaymentUri = false,
                isVerifiedPlatform = true,
                platformName = trustedPlatform,
                findings = findings,
                urlIntelligence = intel
            )
        }

        // A. IP address as host check
        val isIp = host.matches(Regex("""^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$"""))
        if (isIp) {
            isSuspiciousDomain = true
            findings.add("Direct numeric IP address used instead of legitimate registered domain name ($host)")
            structuralFlags.add("Host type: Raw IPv4 address")
        }

        // B. TLD check
        val tld = host.substringAfterLast(".", "")
        val isSuspiciousTld = SUSPICIOUS_TLDS.contains(tld)
        if (isSuspiciousTld) {
            isSuspiciousDomain = true
            findings.add("High-risk domain extension commonly associated with temporary phishing campaigns (.$tld)")
            structuralFlags.add("TLD: .$tld (High-risk registry category)")
        }

        // C. Shorteners check
        if (SUSPICIOUS_SHORTENERS.contains(host)) {
            isSuspiciousStructure = true
            findings.add("URL Shortener service masks final destination target ($host)")
            structuralFlags.add("Redirection: URL Shortener detected")
        }

        // D. Punycode check (Internationalized Domain Names / Homograph attack)
        val isPunycode = host.contains("xn--")
        if (isPunycode) {
            isSuspiciousDomain = true
            findings.add("Punycode (xn--) encoding detected. Frequently used in homograph lookalike attacks")
            structuralFlags.add("Encoding: Punycode IDN ($host)")
        }

        // E. Character substitutions (e.g., 0 for o, 1 for l, rn for m)
        val hasCharSub = host.contains("0") || host.contains("1") || host.contains("rn") || host.contains("vv")
        if (hasCharSub && (host.contains("amaz0n") || host.contains("paypa1") || host.contains("g00gle") || host.contains("micros0ft"))) {
            isSuspiciousDomain = true
            findings.add("Homoglyph character substitution detected mimicking a well-known brand")
            structuralFlags.add("Obfuscation: Character substitution detected")
        }

        // F. Lookalike / Brand Impersonation check
        var lookalikeBrand: String? = null
        var hasLookalikeDomain = false
        BRAND_TARGETS.forEach { (keyword, brand) ->
            // Only flag if keyword appears as an intentional impersonation segment (subdomain or hyphenated)
            val isLookalikePattern = host.startsWith("$keyword-") ||
                    host.contains("-$keyword-") ||
                    host.endsWith("-$keyword") ||
                    host.startsWith("$keyword.") ||
                    host.contains(".$keyword.") ||
                    host.contains(".$keyword-") ||
                    host.contains("-$keyword.")

            if (isLookalikePattern) {
                val officialRoot = when (keyword) {
                    "sbi" -> "sbi.co.in"
                    "hdfc" -> "hdfcbank.com"
                    "icici" -> "icicibank.com"
                    "axis" -> "axisbank.com"
                    "pnb" -> "pnbindia.in"
                    "paypal" -> "paypal.com"
                    "amazon" -> "amazon.com"
                    "google" -> "google.com"
                    "microsoft" -> "microsoft.com"
                    "netflix" -> "netflix.com"
                    "apple" -> "apple.com"
                    "indiapost" -> "indiapost.gov.in"
                    "fedex" -> "fedex.com"
                    "dhl" -> "dhl.com"
                    "whatsapp" -> "whatsapp.com"
                    "telegram" -> "telegram.org"
                    else -> "$keyword.com"
                }
                if (!host.endsWith(officialRoot) && !host.equals(officialRoot)) {
                    isSuspiciousDomain = true
                    hasLookalikeDomain = true
                    lookalikeBrand = brand
                    findings.add("Potential lookalike impersonation of $brand ($officialRoot) in domain name: $host")
                    structuralFlags.add("Lookalike detected: Target is $brand")
                }
            }
        }

        // G. Excessive subdomains & complexity
        val subdomains = host.split(".")
        val excessiveComplexity = subdomains.size > 4 || host.count { it == '-' } >= 4
        if (excessiveComplexity) {
            isSuspiciousStructure = true
            findings.add("Excessive domain nesting or dash fragmentation (${subdomains.size} segments) obscures real host")
            structuralFlags.add("Subdomain hierarchy: ${subdomains.size} levels deep")
        }

        // H. Suspicious path keywords
        SUSPICIOUS_PATH_KEYWORDS.forEach { kw ->
            if (path.contains(kw)) {
                if (isSuspiciousDomain || isSuspiciousStructure) {
                    isPhishingKeyword = true
                    findings.add("Credential-harvesting or urgent verification path detected ('$kw') on non-official domain")
                }
                structuralFlags.add("Target path keyword: '$kw'")
            }
        }

        // I. Non-standard port
        if (uri.port != -1 && uri.port != 80 && uri.port != 443) {
            isSuspiciousStructure = true
            findings.add("Non-standard communication port: ${uri.port}")
            structuralFlags.add("Port: ${uri.port} (Non-standard web port)")
        }

        val urlIntel = URLIntelligence(
            originalUrl = uri.toString(),
            hostname = host,
            tld = tld,
            isSuspiciousTld = isSuspiciousTld,
            hasLookalikeDomain = hasLookalikeDomain,
            lookalikeBrand = lookalikeBrand,
            hasSuspiciousSubdomain = subdomains.size > 2,
            subdomains = subdomains,
            hasCharacterSubstitutions = hasCharSub,
            isIpAddress = isIp,
            isPunycode = isPunycode,
            excessiveComplexity = excessiveComplexity,
            isPaymentUri = false,
            structuralFlags = structuralFlags
        )

        return URLAnalysisResult(
            normalizedUrl = uri.toString(),
            isSuspiciousDomain = isSuspiciousDomain,
            isSuspiciousStructure = isSuspiciousStructure,
            isKnownPhishingKeyword = isPhishingKeyword,
            isPaymentUri = false,
            isVerifiedPlatform = false,
            findings = findings,
            urlIntelligence = urlIntel
        )
    }

    private fun extractQueryParam(uriString: String, paramName: String): String? {
        val query = uriString.substringAfter("?", "")
        if (query.isBlank()) return null
        return query.split("&")
            .map { it.split("=") }
            .firstOrNull { it.size == 2 && it[0].equals(paramName, ignoreCase = true) }
            ?.get(1)
            ?.replace("+", " ")
    }
}
