export enum ScanType {
  QR_CODE = 'QR_CODE',
  MESSAGE_TEXT = 'MESSAGE_TEXT',
  URL_DESTINATION = 'URL_DESTINATION',
  PAYMENT_SCREENSHOT = 'PAYMENT_SCREENSHOT'
}

export const ScanTypeLabels: Record<ScanType, string> = {
  [ScanType.QR_CODE]: 'QR Code',
  [ScanType.MESSAGE_TEXT]: 'Message / Text',
  [ScanType.URL_DESTINATION]: 'Website / URL',
  [ScanType.PAYMENT_SCREENSHOT]: 'Payment Screenshot'
};

export enum RiskClassification {
  SAFE = 'SAFE',
  SUSPICIOUS = 'SUSPICIOUS',
  HIGH_RISK = 'HIGH_RISK',
  CRITICAL = 'CRITICAL'
}

export interface RiskClassificationInfo {
  label: string;
  levelText: string;
}

export const RiskClassificationDetails: Record<RiskClassification, RiskClassificationInfo> = {
  [RiskClassification.SAFE]: { label: 'SAFE', levelText: 'LOW RISK' },
  [RiskClassification.SUSPICIOUS]: { label: 'SUSPICIOUS', levelText: 'CAUTION REQUIRED' },
  [RiskClassification.HIGH_RISK]: { label: 'HIGH RISK', levelText: 'STRONG THREAT' },
  [RiskClassification.CRITICAL]: { label: 'CRITICAL', levelText: 'DO NOT PROCEED' }
};

export function getRiskClassification(score: number): RiskClassification {
  if (score >= 81) return RiskClassification.CRITICAL;
  if (score >= 61) return RiskClassification.HIGH_RISK;
  if (score >= 31) return RiskClassification.SUSPICIOUS;
  return RiskClassification.SAFE;
}

export enum ThreatCategory {
  FINANCIAL_SCAM = 'FINANCIAL_SCAM',
  CREDENTIAL_THEFT = 'CREDENTIAL_THEFT',
  OTP_SOCIAL_ENGINEERING = 'OTP_SOCIAL_ENGINEERING',
  PHISHING = 'PHISHING',
  IMPERSONATION = 'IMPERSONATION',
  FAKE_SUPPORT = 'FAKE_SUPPORT',
  FAKE_DELIVERY = 'FAKE_DELIVERY',
  FAKE_INVESTMENT = 'FAKE_INVESTMENT',
  PAYMENT_FRAUD = 'PAYMENT_FRAUD',
  SUSPICIOUS_QR = 'SUSPICIOUS_QR',
  UNTRUSTED_DESTINATION = 'UNTRUSTED_DESTINATION',
  SOCIAL_ENGINEERING = 'SOCIAL_ENGINEERING',
  UNKNOWN = 'UNKNOWN'
}

export interface ThreatCategoryInfo {
  displayName: string;
  description: string;
}

export const ThreatCategoryDetails: Record<ThreatCategory, ThreatCategoryInfo> = {
  [ThreatCategory.FINANCIAL_SCAM]: {
    displayName: 'Financial Scam',
    description: 'Attempts unauthorized fund transfers or payment diversion.'
  },
  [ThreatCategory.CREDENTIAL_THEFT]: {
    displayName: 'Credential Theft',
    description: 'Harvests passwords, PINs, or banking login credentials.'
  },
  [ThreatCategory.OTP_SOCIAL_ENGINEERING]: {
    displayName: 'OTP Social Engineering',
    description: 'Coerces or tricks victim into sharing one-time security codes.'
  },
  [ThreatCategory.PHISHING]: {
    displayName: 'Phishing',
    description: 'Spoofs a legitimate portal to steal confidential user details.'
  },
  [ThreatCategory.IMPERSONATION]: {
    displayName: 'Impersonation',
    description: 'Pretends to be a government body, bank, or trusted organization.'
  },
  [ThreatCategory.FAKE_SUPPORT]: {
    displayName: 'Fake Customer Support',
    description: 'Impersonates helpdesk to push remote access software or payments.'
  },
  [ThreatCategory.FAKE_DELIVERY]: {
    displayName: 'Fake Delivery',
    description: 'Claims parcel delivery failure to solicit fees or link clicks.'
  },
  [ThreatCategory.FAKE_INVESTMENT]: {
    displayName: 'Fake Investment',
    description: 'Promises unrealistically high returns or guaranteed yields.'
  },
  [ThreatCategory.PAYMENT_FRAUD]: {
    displayName: 'Payment Fraud',
    description: 'Manipulates UPI, QR, or transaction flows into paying the attacker.'
  },
  [ThreatCategory.SUSPICIOUS_QR]: {
    displayName: 'Suspicious QR',
    description: 'Directs scanner to unverified payment gateways or obfuscated endpoints.'
  },
  [ThreatCategory.UNTRUSTED_DESTINATION]: {
    displayName: 'Untrusted Destination',
    description: 'Directs user to high-risk TLDs, IP hosts, or typosquatted domains.'
  },
  [ThreatCategory.SOCIAL_ENGINEERING]: {
    displayName: 'Social Engineering',
    description: 'Uses psychological triggers (urgency, panic, fear) to bypass scrutiny.'
  },
  [ThreatCategory.UNKNOWN]: {
    displayName: 'Unknown / Unclassified',
    description: 'No distinct single threat pattern confirmed by heuristics.'
  }
};

export interface RiskSignals {
  financialRequest: boolean;
  otpRequest: boolean;
  passwordRequest: boolean;
  urgency: boolean;
  authorityImpersonation: boolean;
  threatLanguage: boolean;
  suspiciousDomain: boolean;
  suspiciousUrlStructure: boolean;
  rewardGreedTrap: boolean;
  suspiciousApkOrRemoteControl: boolean;
  independentVerificationEvidence: boolean;
  extractedUrls: string[];
  extractedPhones: string[];
  extractedAmounts: string[];
  signalDetails: string[];
}

export interface AttackStep {
  stage: string;
  quoteOrEvidence: string;
  psychologicalHook: string;
  order: number;
}

export interface ScamDNAStep {
  order: number;
  title: string;
  tactic: string;
  psychologicalPurpose: string;
  evidenceSnippet: string;
}

export interface ScamDNA {
  manipulationStrategy: string;
  sequence: ScamDNAStep[];
}

export interface EvidenceItem {
  signalName: string;
  scoreContribution: number;
  evidenceSnippet: string;
  explanation: string;
}

export interface TrustBreakdown {
  identityTrustScore: number;      // 0..100
  messageTrustScore: number;       // 0..100
  destinationTrustScore: number;   // 0..100
  financialRiskScore: number;      // 0..100
  urgencyScore: number;            // 0..100
  credentialRiskScore: number;     // 0..100
  manipulationRiskScore: number;   // 0..100
  summary: string;
}

export interface VerificationGuideline {
  title: string;
  recommendedAction: string;
  safeChannel: string;
  strictWarning: string;
}

export interface WhatCouldHappenScenario {
  title: string;
  chainSteps: string[];
  disclaimer: string;
}

export interface RiskTimelineStep {
  stage: string;
  label: string;
  isDetected: boolean;
  description: string;
}

export enum SafeActionType {
  COPY_FOR_CYBERCRIME = 'COPY_FOR_CYBERCRIME',
  COPY_SANITIZED_URL = 'COPY_SANITIZED_URL',
  RECOMMEND_BLOCK = 'RECOMMEND_BLOCK',
  RECOMMEND_BANK_HELPLINE = 'RECOMMEND_BANK_HELPLINE',
  SAFE_DISMISS = 'SAFE_DISMISS'
}

export interface SafeActionShortcut {
  id: string;
  title: string;
  description: string;
  actionType: SafeActionType;
  payload?: string;
}

export interface URLIntelligence {
  originalUrl: string;
  hostname: string;
  tld: string;
  isSuspiciousTld: boolean;
  hasLookalikeDomain: boolean;
  lookalikeBrand?: string | null;
  hasSuspiciousSubdomain: boolean;
  subdomains: string[];
  hasCharacterSubstitutions: boolean;
  isIpAddress: boolean;
  isPunycode: boolean;
  excessiveComplexity: boolean;
  isPaymentUri: boolean;
  payeeName?: string | null;
  structuralFlags: string[];
  unavailableDataNotes: string[];
}

export interface AskPhantomQnA {
  question: string;
  answer: string;
  timestamp: number;
}

export interface ScanResult {
  id: string | number;
  timestamp: number;
  scanType: ScanType;
  rawContent: string;
  destinationUrl?: string | null;
  riskScore: number;
  classification: RiskClassification;
  threatCategory: ThreatCategory;
  intent: string;
  manipulationTechniques: string[];
  reasons: string[];
  attackChain: AttackStep[];
  scamDNA: ScamDNA;
  evidenceItems: EvidenceItem[];
  trustBreakdown: TrustBreakdown;
  verificationGuidelines: VerificationGuideline[];
  whatCouldHappen: WhatCouldHappenScenario;
  riskTimeline: RiskTimelineStep[];
  safeActions: SafeActionShortcut[];
  urlIntelligence?: URLIntelligence | null;
  phantomInsight: string;
  recommendedAction: string;
  isAIEnhanced: boolean;
  signals: RiskSignals;
  qnaHistory: AskPhantomQnA[];
}

export enum AuthProviderType {
  EMAIL = 'EMAIL',
  GOOGLE = 'GOOGLE',
  GUEST = 'GUEST'
}

export interface AuthUser {
  uid: string;
  email?: string | null;
  displayName?: string | null;
  photoUrl?: string | null;
  isAnonymous: boolean;
  provider: AuthProviderType;
  createdAt: number;
}

export enum StageStatus {
  PENDING = 'PENDING',
  ACTIVE = 'ACTIVE',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED'
}

export interface LiveAnalysisStage {
  title: string;
  detail: string;
  status: StageStatus;
}
