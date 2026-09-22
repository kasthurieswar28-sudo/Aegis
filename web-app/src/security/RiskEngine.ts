import {
  EvidenceItem,
  RiskClassification,
  RiskSignals,
  RiskTimelineStep,
  SafeActionShortcut,
  SafeActionType,
  ScamDNA,
  ScamDNAStep,
  ThreatCategory,
  ThreatCategoryDetails,
  TrustBreakdown,
  VerificationGuideline,
  WhatCouldHappenScenario,
  getRiskClassification
} from '../domain/models';
import { URLAnalysisResult } from './URLAnalyzer';

export interface RiskEvaluation {
  score: number;
  classification: RiskClassification;
  threatCategory: ThreatCategory;
  evidenceItems: EvidenceItem[];
  trustBreakdown: TrustBreakdown;
  scamDNA: ScamDNA;
  whatCouldHappen: WhatCouldHappenScenario;
  verificationGuidelines: VerificationGuideline[];
  riskTimeline: RiskTimelineStep[];
  safeActions: SafeActionShortcut[];
  primaryFlags: string[];
  recommendation: string;
}

export function evaluateRisk(
  signals: RiskSignals,
  urlAnalysis?: URLAnalysisResult | null,
  sensitivityFactor: number = 1.0
): RiskEvaluation {
  const evidenceList: EvidenceItem[] = [];
  const primaryFlags: string[] = [];
  let baseScore = 0;

  // 1. Suspicious Domain / Destination Endpoint
  const isDomainSuspicious = signals.suspiciousDomain || urlAnalysis?.isSuspiciousDomain === true;
  if (isDomainSuspicious) {
    const domainSnippet = urlAnalysis?.urlIntelligence?.hostname || "Unverified external destination";
    const contribution = 20;
    baseScore += contribution;
    evidenceList.push({
      signalName: "Suspicious Destination Domain",
      scoreContribution: contribution,
      evidenceSnippet: domainSnippet,
      explanation: "The destination domain uses a high-risk registry extension, an unverified brand lookalike pattern, or raw IP addressing."
    });
    primaryFlags.push("Unverified or high-risk destination domain");
  }

  // 2. Obfuscated URL Structure / Redirects / Shorteners
  const isUrlStructureSuspicious = signals.suspiciousUrlStructure || urlAnalysis?.isSuspiciousStructure === true;
  if (isUrlStructureSuspicious && !isDomainSuspicious) {
    const contribution = 15;
    baseScore += contribution;
    evidenceList.push({
      signalName: "Obfuscated URL Structure",
      scoreContribution: contribution,
      evidenceSnippet: urlAnalysis?.urlIntelligence?.originalUrl?.slice(0, 40) || "Complex nested path",
      explanation: "Unusual path parameters, non-standard ports, or shorteners obscuring the destination."
    });
    primaryFlags.push("Masked or abnormal URL structure");
  }

  // 3. Credential or OTP Request
  const hasOtpOrPassword = signals.otpRequest || signals.passwordRequest;
  if (hasOtpOrPassword) {
    const contribution = 25;
    baseScore += contribution;
    const snippet = signals.otpRequest && signals.passwordRequest
      ? "Requests both OTP and Password / PIN"
      : signals.otpRequest
      ? "Demands One-Time Password (OTP)"
      : "Requests confidential login password or PIN";
    evidenceList.push({
      signalName: "Authentication Credential Request",
      scoreContribution: contribution,
      evidenceSnippet: snippet,
      explanation: "Legitimate institutions never request security codes, OTPs, or passwords over unverified channels."
    });
    primaryFlags.push("Requests sensitive security code or password");
  }

  // 4. Financial Request or Payment QR / UPI Intent
  const hasFinancial = signals.financialRequest || urlAnalysis?.isPaymentUri === true;
  if (hasFinancial) {
    const contribution = 20;
    baseScore += contribution;
    const snippet = urlAnalysis?.isPaymentUri === true
      ? `UPI payment intent URI (${urlAnalysis.payeeName || "Unknown Merchant"})`
      : signals.extractedAmounts.length > 0
      ? `Demands monetary amount: ${signals.extractedAmounts[0]}`
      : "Direct money transfer or payment trigger";
    evidenceList.push({
      signalName: "Financial Movement Trigger",
      scoreContribution: contribution,
      evidenceSnippet: snippet,
      explanation: "Attempts to divert funds, collect fees, or trigger wallet authorization."
    });
    primaryFlags.push("Direct payment trigger or fee demand");
  }

  // 5. Authority Impersonation
  if (signals.authorityImpersonation) {
    const contribution = 15;
    baseScore += contribution;
    evidenceList.push({
      signalName: "Institutional Impersonation",
      scoreContribution: contribution,
      evidenceSnippet: "Claims official backing (Bank, Postal Service, Telecom, Government)",
      explanation: "Impersonates recognized authorities to create false credibility and disarm natural caution."
    });
    primaryFlags.push("Impersonates reputable institution or authority");
  }

  // 6. Threat Language / Coercion / Disconnection Warning
  if (signals.threatLanguage) {
    const contribution = 20;
    baseScore += contribution;
    evidenceList.push({
      signalName: "Coercive Threat & Intimidation",
      scoreContribution: contribution,
      evidenceSnippet: "Punitive threats (disconnection, account block, legal penalty)",
      explanation: "Uses intimidation and severe consequences to trigger irrational panic."
    });
    primaryFlags.push("Uses coercive intimidation or punitive threats");
  }

  // 7. Urgency / Artificial Time Pressure
  if (signals.urgency) {
    const contribution = 10;
    baseScore += contribution;
    evidenceList.push({
      signalName: "Artificial Time Pressure",
      scoreContribution: contribution,
      evidenceSnippet: "Urgent demands ('today', 'immediately', 'within 2 hours')",
      explanation: "Restricts deliberation time to prevent the target from performing independent verification."
    });
    primaryFlags.push("Imposes artificial urgency deadline");
  }

  // 8. Remote Control / APK Download Instruction
  if (signals.suspiciousApkOrRemoteControl) {
    const contribution = 25;
    baseScore += contribution;
    evidenceList.push({
      signalName: "Malicious Software / Remote Screen Share",
      scoreContribution: contribution,
      evidenceSnippet: "Urges app installation or remote desktop tool (AnyDesk/TeamViewer/APK)",
      explanation: "Aims to establish device remote access or install spyware to steal two-factor codes."
    });
    primaryFlags.push("Requests remote access tool or unknown APK installation");
  }

  // 9. Reward / Greed Bait
  if (signals.rewardGreedTrap) {
    const contribution = 15;
    baseScore += contribution;
    evidenceList.push({
      signalName: "Unsolicited Reward Bait",
      scoreContribution: contribution,
      evidenceSnippet: "Unexpected lottery, cashback, or prize claim",
      explanation: "Promises unearned gains to entice the user into following malicious instructions."
    });
    primaryFlags.push("Unsolicited reward or prize bait");
  }

  // 10. Independent Verification Mitigator (-15)
  if (signals.independentVerificationEvidence) {
    const contribution = -15;
    baseScore += contribution;
    evidenceList.push({
      signalName: "Official Verification Guidance",
      scoreContribution: contribution,
      evidenceSnippet: "Directs to official branch or verified application",
      explanation: "Advises user to independently check via recognized channels, lowering deceit likelihood."
    });
  }

  // 11. Verified Platform Mitigator (-25)
  if (urlAnalysis?.isVerifiedPlatform === true && !hasOtpOrPassword && !hasFinancial) {
    const contribution = -25;
    baseScore += contribution;
    evidenceList.push({
      signalName: "Verified Platform Destination",
      scoreContribution: contribution,
      evidenceSnippet: `Destination belongs to verified legitimate registry: ${urlAnalysis.platformName || "Verified Service"}`,
      explanation: "The destination is verified against the global directory of trusted services and social platforms."
    });
  }

  // Sensitivity Factor Adjustment & Strict 0..100 Clamping
  const adjustedScore = Math.floor(baseScore * sensitivityFactor);
  const finalScore = Math.max(0, Math.min(100, adjustedScore));
  const classification = getRiskClassification(finalScore);

  const category = determineCategory(signals, urlAnalysis, finalScore);
  const trustBreakdown = deriveTrustBreakdown(signals, urlAnalysis, finalScore);

  const scamDNA: ScamDNA = classification === RiskClassification.SAFE
    ? {
        manipulationStrategy: "No deceptive manipulation or coercion detected. Content appears legitimate and safe to proceed.",
        sequence: []
      }
    : constructScamDNA(signals, urlAnalysis, category);

  const whatCouldHappen: WhatCouldHappenScenario = classification === RiskClassification.SAFE
    ? {
        title: "Standard Safe Digital Interaction",
        chainSteps: [
          "You interact with a standard verified digital destination or informational code.",
          "No unauthorized data harvesting, financial coercion, or hostile redirection observed."
        ],
        disclaimer: "Educational simulation of a potential attack trajectory based on observed signals."
      }
    : constructWhatCouldHappen(signals, urlAnalysis, category);

  const verificationGuidelines: VerificationGuideline[] = classification === RiskClassification.SAFE
    ? [
        {
          title: "Verified Safe Destination",
          recommendedAction: "Safe to open or interact. No security risk detected.",
          safeChannel: "Official application or browser",
          strictWarning: "No threat signatures detected. Standard usage."
        }
      ]
    : constructVerificationGuidelines(signals, urlAnalysis, category);

  const riskTimeline = constructRiskTimeline(signals, urlAnalysis);
  const safeActions = constructSafeActions(signals, urlAnalysis, category);

  let recommendation = "";
  switch (classification) {
    case RiskClassification.CRITICAL:
      recommendation = "DO NOT PROCEED. Strong indicators of deceptive fraud. Do not scan, authorize payments, or share codes.";
      break;
    case RiskClassification.HIGH_RISK:
      recommendation = "DO NOT PROCEED. High probability of deceptive manipulation. Verify independently before taking any action.";
      break;
    case RiskClassification.SUSPICIOUS:
      recommendation = "EXERCISE CAUTION. Content exhibits coercive or unverified patterns. Verify using official channels.";
      break;
    case RiskClassification.SAFE:
      recommendation = "SAFE TO PROCEED. Verified legitimate destination. No deceptive or hostile signatures detected.";
      break;
  }

  let flags: string[];
  if (classification === RiskClassification.SAFE) {
    if (urlAnalysis?.isVerifiedPlatform) {
      flags = [`Verified safe platform: ${urlAnalysis.platformName || "Legitimate"}`, "No deceptive threat signatures detected"];
    } else {
      flags = ["Benign digital content", "No malicious patterns detected"];
    }
  } else if (primaryFlags.length === 0) {
    flags = ["No prominent deception triggers detected"];
  } else {
    flags = primaryFlags;
  }

  return {
    score: finalScore,
    classification,
    threatCategory: category,
    evidenceItems: evidenceList,
    trustBreakdown,
    scamDNA,
    whatCouldHappen,
    verificationGuidelines,
    riskTimeline,
    safeActions,
    primaryFlags: flags,
    recommendation
  };
}

function determineCategory(
  signals: RiskSignals,
  urlAnalysis?: URLAnalysisResult | null,
  score: number = 0
): ThreatCategory {
  if (score < 25) return ThreatCategory.UNKNOWN;

  if (signals.otpRequest) return ThreatCategory.OTP_SOCIAL_ENGINEERING;
  if (signals.suspiciousApkOrRemoteControl) return ThreatCategory.FAKE_SUPPORT;
  if (signals.passwordRequest || urlAnalysis?.isKnownPhishingKeyword) return ThreatCategory.CREDENTIAL_THEFT;
  if (urlAnalysis?.isPaymentUri) return ThreatCategory.PAYMENT_FRAUD;
  if (signals.rewardGreedTrap) return ThreatCategory.FAKE_INVESTMENT;
  if (signals.threatLanguage && signals.financialRequest) return ThreatCategory.FINANCIAL_SCAM;
  if (signals.authorityImpersonation && signals.threatLanguage) return ThreatCategory.IMPERSONATION;
  if (urlAnalysis?.isSuspiciousDomain) return ThreatCategory.PHISHING;
  if (signals.financialRequest) return ThreatCategory.FINANCIAL_SCAM;
  if (signals.urgency && signals.authorityImpersonation) return ThreatCategory.SOCIAL_ENGINEERING;
  if (urlAnalysis?.urlIntelligence?.excessiveComplexity) return ThreatCategory.UNTRUSTED_DESTINATION;
  return ThreatCategory.SOCIAL_ENGINEERING;
}

function deriveTrustBreakdown(
  signals: RiskSignals,
  urlAnalysis?: URLAnalysisResult | null,
  finalScore: number = 0
): TrustBreakdown {
  let identityTrust = Math.max(20, 100 - Math.floor(finalScore / 2));
  if (signals.authorityImpersonation && urlAnalysis?.isSuspiciousDomain) {
    identityTrust = 10;
  } else if (signals.authorityImpersonation) {
    identityTrust = 25;
  } else if (urlAnalysis?.isSuspiciousDomain) {
    identityTrust = 30;
  } else if (signals.threatLanguage) {
    identityTrust = 45;
  }

  let messageTrust = Math.max(20, 100 - Math.floor(finalScore / 2));
  if (signals.urgency && signals.threatLanguage) {
    messageTrust = 15;
  } else if (signals.threatLanguage) {
    messageTrust = 25;
  } else if (signals.urgency) {
    messageTrust = 40;
  }

  let destTrust = Math.max(30, 100 - finalScore);
  if (urlAnalysis?.isVerifiedPlatform) {
    destTrust = 100;
  } else if (urlAnalysis?.isSuspiciousDomain) {
    destTrust = 10;
  } else if (urlAnalysis?.isSuspiciousStructure) {
    destTrust = 35;
  } else if (urlAnalysis?.isPaymentUri) {
    destTrust = 30;
  }

  let finRisk = Math.min(30, Math.floor(finalScore / 3));
  if (urlAnalysis?.isPaymentUri) {
    finRisk = 90;
  } else if (signals.financialRequest && signals.threatLanguage) {
    finRisk = 85;
  } else if (signals.financialRequest) {
    finRisk = 70;
  } else if (signals.rewardGreedTrap) {
    finRisk = 60;
  }

  let urgencyRisk = 10;
  if (signals.urgency && signals.threatLanguage) {
    urgencyRisk = 95;
  } else if (signals.urgency) {
    urgencyRisk = 75;
  } else if (signals.threatLanguage) {
    urgencyRisk = 60;
  }

  let credRisk = 5;
  if (signals.otpRequest && signals.passwordRequest) {
    credRisk = 98;
  } else if (signals.otpRequest) {
    credRisk = 90;
  } else if (signals.passwordRequest) {
    credRisk = 85;
  } else if (urlAnalysis?.isKnownPhishingKeyword) {
    credRisk = 75;
  }

  let manipRisk = Math.min(100, finalScore);
  if (signals.authorityImpersonation && signals.urgency) {
    manipRisk = 90;
  } else if (signals.threatLanguage) {
    manipRisk = 80;
  } else if (signals.rewardGreedTrap) {
    manipRisk = 75;
  }

  let summary = "Trust indicators remain balanced with no prominent hostile vectors detected.";
  if (finalScore >= 61) {
    summary = "Elevated manipulation risk and severely depleted sender identity trust.";
  } else if (finalScore >= 31) {
    summary = "Cautionary indicators observed across message urgency and unverified endpoints.";
  }

  return {
    identityTrustScore: identityTrust,
    messageTrustScore: messageTrust,
    destinationTrustScore: destTrust,
    financialRiskScore: finRisk,
    urgencyScore: urgencyRisk,
    credentialRiskScore: credRisk,
    manipulationRiskScore: manipRisk,
    summary
  };
}

function constructScamDNA(
  signals: RiskSignals,
  urlAnalysis?: URLAnalysisResult | null,
  category: ThreatCategory = ThreatCategory.UNKNOWN
): ScamDNA {
  const steps: ScamDNAStep[] = [];
  let stepIndex = 1;

  // Step 1: Hook
  if (signals.authorityImpersonation) {
    steps.push({
      order: stepIndex++,
      title: "Authority Pretext",
      tactic: "Impersonation",
      psychologicalPurpose: "Disarms natural skepticism by donning the mantle of a trusted official institution.",
      evidenceSnippet: "Claims official banking, utility, or regulatory jurisdiction"
    });
  } else if (signals.rewardGreedTrap) {
    steps.push({
      order: stepIndex++,
      title: "Reward Hook",
      tactic: "Greed Bait",
      psychologicalPurpose: "Entices the victim with unexpected financial gain to stimulate impulsive compliance.",
      evidenceSnippet: "Unsolicited reward, cashback, or lottery announcement"
    });
  } else {
    steps.push({
      order: stepIndex++,
      title: "Digital Contact",
      tactic: "Unsolicited Outbound",
      psychologicalPurpose: "Establishes initial engagement through an unverified external communication channel.",
      evidenceSnippet: "Direct external message or unprompted QR code"
    });
  }

  // Step 2: Emotional Trigger
  if (signals.threatLanguage) {
    steps.push({
      order: stepIndex++,
      title: "Coercive Pressure",
      tactic: "Threat of Loss / Disconnection",
      psychologicalPurpose: "Induces a fight-or-flight response to prevent calm critical thinking.",
      evidenceSnippet: "Threatens power cutoff, account lock, or legal penalties"
    });
  }

  if (signals.urgency) {
    steps.push({
      order: stepIndex++,
      title: "Temporal Compression",
      tactic: "Artificial Deadline",
      psychologicalPurpose: "Creates intense time pressure so the victim acts before consulting trusted advisors.",
      evidenceSnippet: "Tight time window ('today', 'immediately', 'hours left')"
    });
  }

  // Step 3: Exploitation Action
  if (signals.otpRequest || signals.passwordRequest) {
    steps.push({
      order: stepIndex++,
      title: "Credential Harvesting",
      tactic: "Authentication Hijacking",
      psychologicalPurpose: "Tricks the user into volunteering the final secret factor protecting their account.",
      evidenceSnippet: "Solicits OTP, security code, or net banking password"
    });
  } else if (signals.financialRequest || urlAnalysis?.isPaymentUri) {
    steps.push({
      order: stepIndex++,
      title: "Payment Redirection",
      tactic: "Financial Extraction",
      psychologicalPurpose: "Directs the victim into authorizing an irreversible transfer under false pretenses.",
      evidenceSnippet: "Direct UPI QR scan or fee payment request"
    });
  } else if (signals.suspiciousApkOrRemoteControl) {
    steps.push({
      order: stepIndex++,
      title: "Device Infiltration",
      tactic: "Remote Access Tool Push",
      psychologicalPurpose: "Gains screen visibility or control to intercept incoming banking SMS codes.",
      evidenceSnippet: "Instructs user to install APK or remote desktop app"
    });
  }

  // Step 4: Endpoint Trap
  if (urlAnalysis?.isSuspiciousDomain) {
    steps.push({
      order: stepIndex++,
      title: "Controlled Destination",
      tactic: "Lookalike / High-Risk Domain",
      psychologicalPurpose: "Funnel victim to attacker-controlled infrastructure styled to resemble legitimate pages.",
      evidenceSnippet: `Host: ${urlAnalysis.urlIntelligence.hostname}`
    });
  }

  let strategySummary = "Utilizes deceptive psychological triggers to pressure the victim into taking an unverified, high-risk action.";
  switch (category) {
    case ThreatCategory.OTP_SOCIAL_ENGINEERING:
      strategySummary = "Manufactures an emergency to manipulate the victim into sharing two-factor authentication codes to breach the account.";
      break;
    case ThreatCategory.PAYMENT_FRAUD:
    case ThreatCategory.FINANCIAL_SCAM:
      strategySummary = "Exploits manufactured urgency or punitive threats to divert victim funds into an attacker-controlled payment destination.";
      break;
    case ThreatCategory.CREDENTIAL_THEFT:
    case ThreatCategory.PHISHING:
      strategySummary = "Lures the victim to a lookalike spoofed portal to capture banking credentials and passwords.";
      break;
    case ThreatCategory.FAKE_SUPPORT:
      strategySummary = "Pretends to offer technical assistance to establish remote control software and siphon sensitive telemetry.";
      break;
  }

  return {
    manipulationStrategy: strategySummary,
    sequence: steps
  };
}

function constructWhatCouldHappen(
  _signals: RiskSignals,
  _urlAnalysis?: URLAnalysisResult | null,
  category: ThreatCategory = ThreatCategory.UNKNOWN
): WhatCouldHappenScenario {
  const steps: string[] = [];

  switch (category) {
    case ThreatCategory.OTP_SOCIAL_ENGINEERING:
      steps.push("1. You share the requested OTP under the belief that you are resolving an urgent security issue.");
      steps.push("2. The attacker uses the OTP to authenticate a high-value fund transfer or password reset on your actual account.");
      steps.push("3. You are locked out of your banking app while funds are routed to unrecoverable accounts.");
      break;
    case ThreatCategory.PAYMENT_FRAUD:
    case ThreatCategory.FINANCIAL_SCAM:
      steps.push("1. You scan the provided QR code or approve the UPI transfer expecting to clear a bill or receive a refund.");
      steps.push("2. Entering your UPI PIN authorizes a deduction from your account, rather than crediting any funds.");
      steps.push("3. Once the transfer completes, the attacker severs communication and your funds are lost.");
      break;
    case ThreatCategory.CREDENTIAL_THEFT:
    case ThreatCategory.PHISHING:
      steps.push("1. You tap the unverified link and arrive at a login portal designed to resemble your real institution.");
      steps.push("2. You enter your username, password, or debit card details, which are transmitted directly to the attacker's server.");
      steps.push("3. Automated scripts use your credentials to initiate unauthorized account takeovers.");
      break;
    case ThreatCategory.FAKE_SUPPORT:
      steps.push("1. You follow instructions to download an APK or screen-sharing application like AnyDesk or RustDesk.");
      steps.push("2. The attacker observes your screen as you open your banking app and reads your confidential credentials.");
      steps.push("3. The attacker gains full device access to authorize transfers and suppress security SMS alerts.");
      break;
    default:
      steps.push("1. Interacting with the unverified trigger confirms that your contact details are active and susceptible.");
      steps.push("2. The sender may attempt follow-up social engineering or route you to an external phishing portal.");
      steps.push("3. Continued interaction exposes personal credentials and financial data to unverified entities.");
      break;
  }

  return {
    title: `Educational Threat Trajectory: ${ThreatCategoryDetails[category]?.displayName || "Unclassified Threat"}`,
    chainSteps: steps,
    disclaimer: "Educational simulation of a potential attack trajectory based on observed signals. Not an assertion of absolute certainty."
  };
}

function constructVerificationGuidelines(
  signals: RiskSignals,
  urlAnalysis?: URLAnalysisResult | null,
  _category: ThreatCategory = ThreatCategory.UNKNOWN
): VerificationGuideline[] {
  const list: VerificationGuideline[] = [];

  if (signals.authorityImpersonation || signals.threatLanguage) {
    list.push({
      title: "Independent Authority Check",
      recommendedAction: "Verify the status of your account directly through the official app or website.",
      safeChannel: "Official institution mobile app or number on the back of your card",
      strictWarning: "NEVER dial phone numbers or tap links embedded directly inside this message."
    });
  }

  if (signals.otpRequest || signals.passwordRequest) {
    list.push({
      title: "Protect Security Codes",
      recommendedAction: "Treat any request for an OTP or PIN as an immediate indicator of account hijacking.",
      safeChannel: "Customer support number listed on official monthly statements",
      strictWarning: "No genuine bank employee or customer support agent will EVER ask for your OTP."
    });
  }

  if (signals.financialRequest || urlAnalysis?.isPaymentUri) {
    list.push({
      title: "Verify Payment Demand",
      recommendedAction: "Log in to your electricity, utility, or banking portal through your existing trusted bookmark.",
      safeChannel: "Official utility bill portal or registered customer service center",
      strictWarning: "Never pay fees via personal UPI IDs or QR codes sent over SMS or messaging apps."
    });
  }

  if (urlAnalysis?.isSuspiciousDomain) {
    list.push({
      title: "Inspect Website Domain",
      recommendedAction: "Search for the company's verified domain using a known search engine rather than following links.",
      safeChannel: "Official registered top-level domain (.gov.in, .bank.com, official verified site)",
      strictWarning: "Do not submit credentials on domains registered with .top, .xyz, or irregular prefixes."
    });
  }

  if (list.length === 0) {
    list.push({
      title: "Standard Verification Hygiene",
      recommendedAction: "Maintain healthy skepticism and verify unexpected communications through independent channels.",
      safeChannel: "Direct contact via previously verified official channels",
      strictWarning: "Do not share personal details without confirming identity."
    });
  }

  return list;
}

function constructRiskTimeline(
  signals: RiskSignals,
  urlAnalysis?: URLAnalysisResult | null
): RiskTimelineStep[] {
  return [
    {
      stage: "1. Attention Hook",
      label: "Contact Initiated",
      isDetected: true,
      description: "Attacker reaches out via unsolicited digital message or physical QR placement."
    },
    {
      stage: "2. Authority Pretext",
      label: "Claims Official Status",
      isDetected: signals.authorityImpersonation,
      description: signals.authorityImpersonation
        ? "Detected claim of official banking or utility backing."
        : "No prominent authority claim detected."
    },
    {
      stage: "3. Fear & Intimidation",
      label: "Disconnection / Block Threat",
      isDetected: signals.threatLanguage,
      description: signals.threatLanguage
        ? "Coercive threats used to provoke rapid, unthinking panic."
        : "No overt punitive threats found."
    },
    {
      stage: "4. Temporal Urgency",
      label: "Artificial Deadline",
      isDetected: signals.urgency,
      description: signals.urgency
        ? "Enforces an immediate deadline to prevent independent consultation."
        : "Standard timeline."
    },
    {
      stage: "5. Financial Pressure",
      label: "Payment Demand",
      isDetected: signals.financialRequest || urlAnalysis?.isPaymentUri === true,
      description: signals.financialRequest || urlAnalysis?.isPaymentUri === true
        ? "Requires immediate money transfer or QR scan."
        : "No monetary transaction demanded."
    },
    {
      stage: "6. Action / Exploitation",
      label: "Harmful Compliance",
      isDetected: signals.otpRequest || signals.passwordRequest || urlAnalysis?.isSuspiciousDomain === true,
      description: signals.otpRequest || signals.passwordRequest
        ? "Demands security code or directs to spoofed capture form."
        : "General call to action."
    }
  ];
}

function constructSafeActions(
  signals: RiskSignals,
  urlAnalysis?: URLAnalysisResult | null,
  category: ThreatCategory = ThreatCategory.UNKNOWN
): SafeActionShortcut[] {
  const actions: SafeActionShortcut[] = [
    {
      id: "copy_report",
      title: "Copy Incident Summary",
      description: "Copies structured evidence details formatted for official cybercrime reporting portals.",
      actionType: SafeActionType.COPY_FOR_CYBERCRIME,
      payload: `Threat: ${ThreatCategoryDetails[category]?.displayName || "Unknown"} | Flags: ${signals.signalDetails.join("; ")}`
    }
  ];

  if (urlAnalysis) {
    actions.push({
      id: "copy_url",
      title: "Copy Sanitized Destination",
      description: "Copies destination string without executing or previewing in browser.",
      actionType: SafeActionType.COPY_SANITIZED_URL,
      payload: urlAnalysis.normalizedUrl
    });
  }

  actions.push({
    id: "block_sender",
    title: "Block & Report Sender",
    description: "Guide on blocking sender within your phone's SMS or messaging app.",
    actionType: SafeActionType.RECOMMEND_BLOCK
  });

  actions.push({
    id: "dismiss_safe",
    title: "Safely Discard & Exit",
    description: "Clear current analysis without interacting with suspect elements.",
    actionType: SafeActionType.SAFE_DISMISS
  });

  return actions;
}
