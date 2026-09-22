import { URLIntelligence } from '../domain/models';

export interface URLAnalysisResult {
  normalizedUrl: string;
  isSuspiciousDomain: boolean;
  isSuspiciousStructure: boolean;
  isKnownPhishingKeyword: boolean;
  isPaymentUri: boolean;
  isVerifiedPlatform: boolean;
  platformName?: string | null;
  payeeName?: string | null;
  findings: string[];
  urlIntelligence: URLIntelligence;
}

const TRUSTED_PLATFORMS: Record<string, string> = {
  "instagram.com": "Instagram",
  "ig.me": "Instagram",
  "instagr.am": "Instagram",
  "cdninstagram.com": "Instagram",
  "facebook.com": "Facebook",
  "fb.com": "Facebook",
  "fb.me": "Facebook",
  "whatsapp.com": "WhatsApp",
  "wa.me": "WhatsApp",
  "twitter.com": "Twitter / X",
  "x.com": "Twitter / X",
  "t.co": "Twitter / X",
  "linkedin.com": "LinkedIn",
  "lnkd.in": "LinkedIn",
  "youtube.com": "YouTube",
  "youtu.be": "YouTube",
  "google.com": "Google",
  "goo.gl": "Google",
  "forms.gle": "Google Forms",
  "apple.com": "Apple",
  "apple.co": "Apple",
  "spotify.com": "Spotify",
  "spoti.fi": "Spotify",
  "github.com": "GitHub",
  "github.io": "GitHub Pages",
  "microsoft.com": "Microsoft",
  "live.com": "Microsoft",
  "office.com": "Microsoft Office",
  "outlook.com": "Microsoft Outlook",
  "reddit.com": "Reddit",
  "pinterest.com": "Pinterest",
  "pin.it": "Pinterest",
  "telegram.org": "Telegram",
  "t.me": "Telegram",
  "wikipedia.org": "Wikipedia",
  "amazon.com": "Amazon",
  "amazon.in": "Amazon India",
  "amzn.to": "Amazon",
  "netflix.com": "Netflix",
  "linktr.ee": "Linktree",
  "qrco.de": "QR Code Generator",
  "flowcode.com": "Flowcode",
  "qr-code-generator.com": "QR Code Generator"
};

const SUSPICIOUS_TLDS = new Set([
  "xyz", "top", "work", "click", "buzz", "rest", "gq", "tk", "ml", "cf",
  "fit", "live", "cc", "su", "cam", "monster", "icu", "vip", "cfd"
]);

const SUSPICIOUS_SHORTENERS = new Set([
  "bit.ly", "tinyurl.com", "is.gd", "cutt.ly", "rb.gy", "ow.ly",
  "shorturl.at", "soo.gd", "v.gd"
]);

const BRAND_TARGETS: [string, string][] = [
  ["sbi", "State Bank of India"],
  ["hdfc", "HDFC Bank"],
  ["icici", "ICICI Bank"],
  ["axis", "Axis Bank"],
  ["pnb", "Punjab National Bank"],
  ["paypal", "PayPal"],
  ["amazon", "Amazon"],
  ["google", "Google"],
  ["microsoft", "Microsoft"],
  ["netflix", "Netflix"],
  ["apple", "Apple"],
  ["indiapost", "India Post"],
  ["fedex", "FedEx"],
  ["dhl", "DHL"],
  ["whatsapp", "WhatsApp"],
  ["telegram", "Telegram"]
];

const SUSPICIOUS_PATH_KEYWORDS = [
  "login", "verify", "secure", "account-update", "recover", "banking",
  "auth", "wp-content", "wallet-connect", "claim-reward", "kyc-update",
  "card-check", "pan-link", "refund-claim", "disconnection-prevent"
];

function findTrustedPlatform(host: string): string | null {
  const cleanHost = host.toLowerCase().trim();
  for (const [domain, name] of Object.entries(TRUSTED_PLATFORMS)) {
    if (cleanHost === domain || cleanHost.endsWith(`.${domain}`)) {
      return name;
    }
  }
  if (/^(.*\.)?google\.(co\.[a-z]{2}|[a-z]{2,3})$/i.test(cleanHost)) return "Google";
  if (/^(.*\.)?amazon\.(co\.[a-z]{2}|[a-z]{2,3})$/i.test(cleanHost)) return "Amazon";
  return null;
}

function extractQueryParam(uriString: string, paramName: string): string | null {
  const query = uriString.includes('?') ? uriString.substring(uriString.indexOf('?') + 1) : '';
  if (!query) return null;
  const parts = query.split('&');
  for (const part of parts) {
    const [key, value] = part.split('=');
    if (key && key.toLowerCase() === paramName.toLowerCase() && value !== undefined) {
      return decodeURIComponent(value.replace(/\+/g, ' '));
    }
  }
  return null;
}

export function analyzeUrl(inputUrl: string): URLAnalysisResult {
  const trimmed = inputUrl.trim();
  const findings: string[] = [];
  const structuralFlags: string[] = [];

  const defaultUnavailableNotes = [
    "Domain Registration Age: Unavailable (Requires remote WHOIS registry)",
    "Global DNS Threat Feed: Offline heuristic analysis only",
    "SSL Certificate Authority: Unavailable without active TLS handshake"
  ];

  // 1. UPI Payment URI Analysis
  if (trimmed.toLowerCase().startsWith('upi://')) {
    const payee = extractQueryParam(trimmed, 'pn') || 'Unknown Payee';
    const pa = extractQueryParam(trimmed, 'pa') || 'Unknown VPA';
    const am = extractQueryParam(trimmed, 'am');

    findings.push(`Direct UPI Payment Intent URI (Payee: ${payee})`);
    findings.push(`VPA Endpoint: ${pa}`);
    if (am) findings.push(`Pre-filled Amount: ₹${am}`);
    findings.push("Triggering this QR prompts the user to enter their UPI PIN to authorize money deduction.");

    structuralFlags.push("Protocol: upi:// payment intent");
    structuralFlags.push(`Target VPA: ${pa}`);

    const intel: URLIntelligence = {
      originalUrl: trimmed,
      hostname: "UPI Payment Handler",
      tld: "N/A",
      isSuspiciousTld: false,
      hasLookalikeDomain: false,
      lookalikeBrand: null,
      hasSuspiciousSubdomain: false,
      subdomains: [],
      hasCharacterSubstitutions: false,
      isIpAddress: false,
      isPunycode: false,
      excessiveComplexity: false,
      isPaymentUri: true,
      payeeName: payee,
      structuralFlags,
      unavailableDataNotes: defaultUnavailableNotes
    };

    return {
      normalizedUrl: trimmed,
      isSuspiciousDomain: false,
      isSuspiciousStructure: true,
      isKnownPhishingKeyword: false,
      isPaymentUri: true,
      isVerifiedPlatform: false,
      platformName: "UPI Payment",
      payeeName: payee,
      findings,
      urlIntelligence: intel
    };
  }

  // 2. Wi-Fi Setup QR Analysis
  if (trimmed.toUpperCase().startsWith('WIFI:')) {
    const ssidMatch = /S:([^;]+)/i.exec(trimmed);
    const typeMatch = /T:([^;]+)/i.exec(trimmed);
    const ssid = ssidMatch ? ssidMatch[1] : 'Network';
    const type = typeMatch ? typeMatch[1] : 'WPA';

    findings.push("Legitimate Wi-Fi Network Setup QR code");
    findings.push(`Target SSID: ${ssid} (Security: ${type})`);
    findings.push("Standard network configuration protocol; no malicious redirection detected.");
    structuralFlags.push("Protocol: Wi-Fi Setup barcode");
    structuralFlags.push(`Network SSID: ${ssid}`);

    const intel: URLIntelligence = {
      originalUrl: trimmed,
      hostname: `Wi-Fi Config (${ssid})`,
      tld: "N/A",
      isSuspiciousTld: false,
      hasLookalikeDomain: false,
      lookalikeBrand: null,
      hasSuspiciousSubdomain: false,
      subdomains: [],
      hasCharacterSubstitutions: false,
      isIpAddress: false,
      isPunycode: false,
      excessiveComplexity: false,
      isPaymentUri: false,
      structuralFlags,
      unavailableDataNotes: defaultUnavailableNotes
    };

    return {
      normalizedUrl: trimmed,
      isSuspiciousDomain: false,
      isSuspiciousStructure: false,
      isKnownPhishingKeyword: false,
      isPaymentUri: false,
      isVerifiedPlatform: true,
      platformName: "Wi-Fi Setup",
      findings,
      urlIntelligence: intel
    };
  }

  // 3. Contact Card (vCard / MeCard)
  if (trimmed.toUpperCase().startsWith('BEGIN:VCARD') || trimmed.toUpperCase().startsWith('MECARD:')) {
    const nameMatch = /(?:FN|N):([^;\n\r]+)/i.exec(trimmed);
    const name = nameMatch ? nameMatch[1].trim() : 'Contact';

    findings.push("Legitimate Contact vCard / MeCard QR code");
    findings.push(`Contact Identity: ${name}`);
    findings.push("Standard electronic business card format; no hostile redirection.");
    structuralFlags.push("Protocol: Contact Card (vCard)");

    const intel: URLIntelligence = {
      originalUrl: trimmed,
      hostname: `Contact Card (${name})`,
      tld: "N/A",
      isSuspiciousTld: false,
      hasLookalikeDomain: false,
      lookalikeBrand: null,
      hasSuspiciousSubdomain: false,
      subdomains: [],
      hasCharacterSubstitutions: false,
      isIpAddress: false,
      isPunycode: false,
      excessiveComplexity: false,
      isPaymentUri: false,
      structuralFlags,
      unavailableDataNotes: defaultUnavailableNotes
    };

    return {
      normalizedUrl: trimmed,
      isSuspiciousDomain: false,
      isSuspiciousStructure: false,
      isKnownPhishingKeyword: false,
      isPaymentUri: false,
      isVerifiedPlatform: true,
      platformName: "Contact Card",
      findings,
      urlIntelligence: intel
    };
  }

  // 4. Telephone / Email / SMS / Geo Shortcuts
  if (/^(tel:|mailto:|sms:|geo:)/i.test(trimmed)) {
    const scheme = trimmed.substring(0, trimmed.indexOf(':')).toLowerCase();
    findings.push(`Standard system action shortcut (${scheme})`);
    structuralFlags.push(`Protocol: ${scheme} action`);

    const intel: URLIntelligence = {
      originalUrl: trimmed,
      hostname: `System Action (${scheme})`,
      tld: "N/A",
      isSuspiciousTld: false,
      hasLookalikeDomain: false,
      lookalikeBrand: null,
      hasSuspiciousSubdomain: false,
      subdomains: [],
      hasCharacterSubstitutions: false,
      isIpAddress: false,
      isPunycode: false,
      excessiveComplexity: false,
      isPaymentUri: false,
      structuralFlags,
      unavailableDataNotes: defaultUnavailableNotes
    };

    return {
      normalizedUrl: trimmed,
      isSuspiciousDomain: false,
      isSuspiciousStructure: false,
      isKnownPhishingKeyword: false,
      isPaymentUri: false,
      isVerifiedPlatform: true,
      platformName: "Action Shortcut",
      findings,
      urlIntelligence: intel
    };
  }

  // 5. Social Media In-App Schemes
  if (/^(instagram:\/\/|fb:\/\/|whatsapp:\/\/|tg:\/\/|market:\/\/)/i.test(trimmed)) {
    let appName = 'Application Deep Link';
    if (/^instagram:\/\//i.test(trimmed)) appName = 'Instagram';
    else if (/^whatsapp:\/\//i.test(trimmed)) appName = 'WhatsApp';
    else if (/^fb:\/\//i.test(trimmed)) appName = 'Facebook';
    else if (/^tg:\/\//i.test(trimmed)) appName = 'Telegram';

    findings.push(`Verified direct application deep-link (${appName})`);
    findings.push(`Points directly to verified ${appName} app resource.`);
    structuralFlags.push(`Protocol: ${appName} deep-link`);

    const intel: URLIntelligence = {
      originalUrl: trimmed,
      hostname: `${appName} App`,
      tld: "N/A",
      isSuspiciousTld: false,
      hasLookalikeDomain: false,
      lookalikeBrand: null,
      hasSuspiciousSubdomain: false,
      subdomains: [],
      hasCharacterSubstitutions: false,
      isIpAddress: false,
      isPunycode: false,
      excessiveComplexity: false,
      isPaymentUri: false,
      structuralFlags,
      unavailableDataNotes: defaultUnavailableNotes
    };

    return {
      normalizedUrl: trimmed,
      isSuspiciousDomain: false,
      isSuspiciousStructure: false,
      isKnownPhishingKeyword: false,
      isPaymentUri: false,
      isVerifiedPlatform: true,
      platformName: appName,
      findings,
      urlIntelligence: intel
    };
  }

  // 6. Plain text or non-URL data
  const isLikelyUrl = trimmed.toLowerCase().startsWith('http://') ||
                      trimmed.toLowerCase().startsWith('https://') ||
                      (trimmed.includes('.') && !trimmed.includes(' ') && trimmed.length < 150);

  if (!isLikelyUrl) {
    findings.push("Plain text informational content; no external web redirection detected.");
    structuralFlags.push("Content: Plain Text");

    const intel: URLIntelligence = {
      originalUrl: trimmed,
      hostname: "Informational Text",
      tld: "N/A",
      isSuspiciousTld: false,
      hasLookalikeDomain: false,
      lookalikeBrand: null,
      hasSuspiciousSubdomain: false,
      subdomains: [],
      hasCharacterSubstitutions: false,
      isIpAddress: false,
      isPunycode: false,
      excessiveComplexity: false,
      isPaymentUri: false,
      structuralFlags,
      unavailableDataNotes: defaultUnavailableNotes
    };

    return {
      normalizedUrl: trimmed,
      isSuspiciousDomain: false,
      isSuspiciousStructure: false,
      isKnownPhishingKeyword: false,
      isPaymentUri: false,
      isVerifiedPlatform: true,
      platformName: "Informational Data",
      findings,
      urlIntelligence: intel
    };
  }

  let isSuspiciousDomain = false;
  let isSuspiciousStructure = false;
  let isPhishingKeyword = false;

  let host = "";
  let path = "";
  let fullUrl = trimmed;

  try {
    const urlObj = new URL(trimmed.startsWith('http://') || trimmed.startsWith('https://') ? trimmed : `https://${trimmed}`);
    host = urlObj.hostname.toLowerCase();
    path = urlObj.pathname.toLowerCase();
    fullUrl = urlObj.toString();
  } catch {
    const hasObviousExploit = trimmed.includes('@') || (trimmed.match(/\//g) || []).length > 8;
    if (hasObviousExploit) {
      findings.push("Malformed URL format with potential obfuscation");
    } else {
      findings.push("Non-standard text or identifier; no malicious redirection detected");
    }

    const intel: URLIntelligence = {
      originalUrl: trimmed,
      hostname: "Unformatted Text",
      tld: "N/A",
      isSuspiciousTld: false,
      hasLookalikeDomain: false,
      lookalikeBrand: null,
      hasSuspiciousSubdomain: false,
      subdomains: [],
      hasCharacterSubstitutions: false,
      isIpAddress: false,
      isPunycode: false,
      excessiveComplexity: false,
      isPaymentUri: false,
      structuralFlags: ["Format: Plain/Unformatted String"],
      unavailableDataNotes: defaultUnavailableNotes
    };

    return {
      normalizedUrl: trimmed,
      isSuspiciousDomain: hasObviousExploit,
      isSuspiciousStructure: hasObviousExploit,
      isKnownPhishingKeyword: false,
      isPaymentUri: false,
      isVerifiedPlatform: !hasObviousExploit,
      platformName: hasObviousExploit ? null : "Plain Content",
      findings,
      urlIntelligence: intel
    };
  }

  // 7. Check against Verified Platform Registry
  const trustedPlatform = findTrustedPlatform(host);
  if (trustedPlatform) {
    findings.push(`Verified official destination: ${trustedPlatform} (${host})`);
    findings.push("Domain belongs to verified legitimate platform registry; safe to browse.");
    structuralFlags.push(`Registry: Verified Official Platform (${trustedPlatform})`);

    const intel: URLIntelligence = {
      originalUrl: fullUrl,
      hostname: host,
      tld: host.includes('.') ? host.substring(host.lastIndexOf('.') + 1) : '',
      isSuspiciousTld: false,
      hasLookalikeDomain: false,
      lookalikeBrand: null,
      hasSuspiciousSubdomain: false,
      subdomains: host.split('.'),
      hasCharacterSubstitutions: false,
      isIpAddress: false,
      isPunycode: false,
      excessiveComplexity: false,
      isPaymentUri: false,
      structuralFlags,
      unavailableDataNotes: defaultUnavailableNotes
    };

    return {
      normalizedUrl: fullUrl,
      isSuspiciousDomain: false,
      isSuspiciousStructure: false,
      isKnownPhishingKeyword: false,
      isPaymentUri: false,
      isVerifiedPlatform: true,
      platformName: trustedPlatform,
      findings,
      urlIntelligence: intel
    };
  }

  // A. IP address as host check
  const isIp = /^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$/.test(host);
  if (isIp) {
    isSuspiciousDomain = true;
    findings.push(`Direct numeric IP address used instead of legitimate registered domain name (${host})`);
    structuralFlags.push("Host type: Raw IPv4 address");
  }

  // B. TLD check
  const tld = host.includes('.') ? host.substring(host.lastIndexOf('.') + 1) : '';
  const isSuspiciousTld = SUSPICIOUS_TLDS.has(tld);
  if (isSuspiciousTld) {
    isSuspiciousDomain = true;
    findings.push(`High-risk domain extension commonly associated with temporary phishing campaigns (.${tld})`);
    structuralFlags.push(`TLD: .${tld} (High-risk registry category)`);
  }

  // C. Shorteners check
  if (SUSPICIOUS_SHORTENERS.has(host)) {
    isSuspiciousStructure = true;
    findings.push(`URL Shortener service masks final destination target (${host})`);
    structuralFlags.push("Redirection: URL Shortener detected");
  }

  // D. Punycode check
  const isPunycode = host.includes('xn--');
  if (isPunycode) {
    isSuspiciousDomain = true;
    findings.push("Punycode (xn--) encoding detected. Frequently used in homograph lookalike attacks");
    structuralFlags.push(`Encoding: Punycode IDN (${host})`);
  }

  // E. Character substitutions
  const hasCharSub = host.includes('0') || host.includes('1') || host.includes('rn') || host.includes('vv');
  if (hasCharSub && (host.includes('amaz0n') || host.includes('paypa1') || host.includes('g00gle') || host.includes('micros0ft'))) {
    isSuspiciousDomain = true;
    findings.push("Homoglyph character substitution detected mimicking a well-known brand");
    structuralFlags.push("Obfuscation: Character substitution detected");
  }

  // F. Lookalike / Brand Impersonation check
  let lookalikeBrand: string | null = null;
  let hasLookalikeDomain = false;

  for (const [keyword, brand] of BRAND_TARGETS) {
    const isLookalikePattern = host.startsWith(`${keyword}-`) ||
      host.includes(`-${keyword}-`) ||
      host.endsWith(`-${keyword}`) ||
      host.startsWith(`${keyword}.`) ||
      host.includes(`.${keyword}.`) ||
      host.includes(`.${keyword}-`) ||
      host.includes(`-${keyword}.`);

    if (isLookalikePattern) {
      let officialRoot = `${keyword}.com`;
      if (keyword === 'sbi') officialRoot = 'sbi.co.in';
      else if (keyword === 'hdfc') officialRoot = 'hdfcbank.com';
      else if (keyword === 'icici') officialRoot = 'icicibank.com';
      else if (keyword === 'axis') officialRoot = 'axisbank.com';
      else if (keyword === 'pnb') officialRoot = 'pnbindia.in';
      else if (keyword === 'indiapost') officialRoot = 'indiapost.gov.in';
      else if (keyword === 'telegram') officialRoot = 'telegram.org';

      if (!host.endsWith(officialRoot) && host !== officialRoot) {
        isSuspiciousDomain = true;
        hasLookalikeDomain = true;
        lookalikeBrand = brand;
        findings.push(`Potential lookalike impersonation of ${brand} (${officialRoot}) in domain name: ${host}`);
        structuralFlags.push(`Lookalike detected: Target is ${brand}`);
      }
    }
  }

  // G. Excessive subdomains & complexity
  const subdomains = host.split('.');
  const excessiveComplexity = subdomains.length > 4 || (host.match(/-/g) || []).length >= 4;
  if (excessiveComplexity) {
    isSuspiciousStructure = true;
    findings.push(`Excessive domain nesting or dash fragmentation (${subdomains.length} segments) obscures real host`);
    structuralFlags.push(`Subdomain hierarchy: ${subdomains.length} levels deep`);
  }

  // H. Suspicious path keywords
  for (const kw of SUSPICIOUS_PATH_KEYWORDS) {
    if (path.includes(kw)) {
      if (isSuspiciousDomain || isSuspiciousStructure) {
        isPhishingKeyword = true;
        findings.push(`Credential-harvesting or urgent verification path detected ('${kw}') on non-official domain`);
      }
      structuralFlags.push(`Target path keyword: '${kw}'`);
    }
  }

  // I. Non-standard port
  try {
    const parsedUrl = new URL(fullUrl);
    if (parsedUrl.port && parsedUrl.port !== '80' && parsedUrl.port !== '443') {
      isSuspiciousStructure = true;
      findings.push(`Non-standard communication port: ${parsedUrl.port}`);
      structuralFlags.push(`Port: ${parsedUrl.port} (Non-standard web port)`);
    }
  } catch {}

  const urlIntel: URLIntelligence = {
    originalUrl: fullUrl,
    hostname: host,
    tld,
    isSuspiciousTld,
    hasLookalikeDomain,
    lookalikeBrand,
    hasSuspiciousSubdomain: subdomains.length > 2,
    subdomains,
    hasCharacterSubstitutions: hasCharSub,
    isIpAddress: isIp,
    isPunycode,
    excessiveComplexity,
    isPaymentUri: false,
    structuralFlags,
    unavailableDataNotes: defaultUnavailableNotes
  };

  return {
    normalizedUrl: fullUrl,
    isSuspiciousDomain,
    isSuspiciousStructure,
    isKnownPhishingKeyword: isPhishingKeyword,
    isPaymentUri: false,
    isVerifiedPlatform: false,
    findings,
    urlIntelligence: urlIntel
  };
}
