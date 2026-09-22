import { RiskSignals } from '../domain/models';

const URL_PATTERN = /https?:\/\/[a-zA-Z0-9.\-_~:/?#[\]@!$&'()*+,;=%]+/gi;
const PHONE_PATTERN = /(\+?\d{1,3}[-.\s]?)?\(?\d{3}\)?[-.\s]?\d{3}[-.\s]?\d{4}|\b\d{10}\b/g;
const AMOUNT_PATTERN = /(₹|\$|€|£|INR|USD)\s?[\d,]+(\.\d{1,2})?|\b[\d,]+(\.\d{1,2})?\s?(₹|rupees|dollars|inr|usd)\b/gi;

const OTP_TOKENS = [
  "otp", "one time password", "verification code", "security code",
  "share code", "secret pin", "sms code", "do not share", "auth code"
];

const PASSWORD_TOKENS = [
  "password", "passcode", "atm pin", "cvv", "card details", "login credentials",
  "netbanking password", "mpin", "security question"
];

const FINANCIAL_TOKENS = [
  "send money", "transfer money", "credit card", "debit card", "bank account",
  "payment pending", "wire transfer", "upi pin", "enter pin to receive", "paytm pin",
  "scan to pay", "pay to unblock", "refund claim", "wallet balance transfer"
];

const URGENCY_TOKENS = [
  "immediately", "within 24 hours", "urgently",
  "within 10 minutes", "expires soon", "act fast", "limited time",
  "blocked today", "suspended immediately", "hours left", "action required"
];

const THREAT_TOKENS = [
  "account blocked", "sim blocked", "services suspended", "suspended", "legal action", "arrest", "police",
  "penalty", "frozen", "compromised", "deactivated", "court warrant",
  "police investigation", "jail", "lawsuit", "account closed",
  "electricity disconnect", "disconnection notice", "locked account",
  "disconnected", "disconnection", "locked"
];

const AUTHORITY_TOKENS = [
  "tax department", "bank security", "official notice", "federal reserve",
  "customer care helpline", "cyber crime cell", "telecom department",
  "department of telecommunications", "income tax", "rbi", "state bank of india", "sbi official",
  "sbi", "fbi", "irs", "fraud prevention department", "security department"
];

const REWARD_TOKENS = [
  "lottery", "cashback", "winner", "prize", "congratulations",
  "bonus reward", "free gift", "claim now", "selected for award", "jackpot", "you won"
];

const SUSPICIOUS_INSTRUCTIONS = [
  "anydesk", "teamviewer", "quicksupport", "rustdesk", "install apk",
  "download app", "forward sms", "scan to receive", "enter pin to receive",
  "screen share", "remote access", "allow permission", "unknown source"
];

const INDEPENDENT_VERIFICATION_TOKENS = [
  "visit your nearest branch", "check official website", "call the number on back of card",
  "verified in official app", "informational only", "do not click third party links",
  "official communication"
];

function matchesAny(text: string, tokens: string[]): boolean {
  const lower = text.toLowerCase();
  return tokens.some(token => {
    if (token.includes(' ')) {
      return lower.includes(token.toLowerCase());
    } else {
      const regex = new RegExp(`\\b${escapeRegExp(token)}\\b`, 'i');
      return regex.test(text);
    }
  });
}

function escapeRegExp(string: string): string {
  return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

export function extractSignals(rawText: string): RiskSignals {
  const details: string[] = [];

  const urls = rawText.match(URL_PATTERN) || [];
  const rawPhones = rawText.match(PHONE_PATTERN) || [];
  const phones = rawPhones.map(p => p.trim()).filter(p => p.length >= 7);
  const rawAmounts = rawText.match(AMOUNT_PATTERN) || [];
  const amounts = rawAmounts.map(a => a.trim());

  const hasOtp = matchesAny(rawText, OTP_TOKENS);
  if (hasOtp) details.push("Requests OTP / One-Time Security Code");

  const hasPassword = matchesAny(rawText, PASSWORD_TOKENS);
  if (hasPassword) details.push("Requests Confidential Password or PIN");

  const hasFinancial = matchesAny(rawText, FINANCIAL_TOKENS) || amounts.length > 0;
  if (hasFinancial) details.push("Demands Financial Transaction or Money Movement");

  const hasUrgency = matchesAny(rawText, URGENCY_TOKENS);
  if (hasUrgency) details.push("Employs High Urgency to induce panic");

  const hasThreat = matchesAny(rawText, THREAT_TOKENS);
  if (hasThreat) details.push("Uses Coercive Threat / Legal Intimidation Language");

  const hasAuthority = matchesAny(rawText, AUTHORITY_TOKENS);
  if (hasAuthority) details.push("Impersonates Official or Financial Authority");

  const hasReward = matchesAny(rawText, REWARD_TOKENS);
  if (hasReward) details.push("Offers Unsolicited Reward / Greed Trap");

  const hasSuspiciousInstructions = matchesAny(rawText, SUSPICIOUS_INSTRUCTIONS);
  if (hasSuspiciousInstructions) details.push("Instructs installing Remote Access or Unverified APK");

  const hasVerificationEvidence = matchesAny(rawText, INDEPENDENT_VERIFICATION_TOKENS);
  if (hasVerificationEvidence) details.push("Contains safe independent verification guidance");

  return {
    financialRequest: hasFinancial,
    otpRequest: hasOtp,
    passwordRequest: hasPassword,
    urgency: hasUrgency,
    authorityImpersonation: hasAuthority,
    threatLanguage: hasThreat,
    suspiciousDomain: false,
    suspiciousUrlStructure: false,
    rewardGreedTrap: hasReward,
    suspiciousApkOrRemoteControl: hasSuspiciousInstructions,
    independentVerificationEvidence: hasVerificationEvidence,
    extractedUrls: urls,
    extractedPhones: phones,
    extractedAmounts: amounts,
    signalDetails: details
  };
}
