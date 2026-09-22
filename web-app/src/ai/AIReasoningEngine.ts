import {
  AttackStep,
  RiskClassification,
  RiskSignals,
  ScanResult,
  ScanType,
  ScanTypeLabels
} from '../domain/models';
import { RiskEvaluation } from '../security/RiskEngine';
import { URLAnalysisResult } from '../security/URLAnalyzer';

export interface AIAnalysisOutput {
  classification: string;
  intent: string;
  manipulationTechniques: string[];
  reasons: string[];
  attackChain: AttackStep[];
  phantomInsight: string;
  recommendation: string;
  isCloudEnhanced: boolean;
}

export class AIReasoningEngine {
  private customApiKey: string | null = null;

  setApiKey(key: string | null) {
    this.customApiKey = key;
  }

  getApiKey(): string {
    return this.customApiKey || (import.meta.env.VITE_GEMINI_API_KEY as string) || '';
  }

  async reasonAboutThreat(
    rawContent: string,
    scanType: ScanType,
    destinationUrl: string | null,
    signals: RiskSignals,
    _urlAnalysis: URLAnalysisResult | null,
    evaluation: RiskEvaluation
  ): Promise<AIAnalysisOutput> {
    const apiKey = this.getApiKey();

    if (apiKey && apiKey.trim() !== '' && apiKey !== 'MY_GEMINI_API_KEY') {
      try {
        const cloudOutput = await this.callGeminiRest(rawContent, scanType, signals, evaluation, apiKey);
        if (cloudOutput) {
          return cloudOutput;
        }
      } catch (e) {
        console.warn('Gemini API call failed, falling back to deterministic local reasoning:', e);
      }
    }

    // Offline / Local flagship reasoning fallback
    return this.generateOfflineReasoning(rawContent, scanType, destinationUrl, signals, evaluation);
  }

  async askAegis(question: string, scanResult: ScanResult): Promise<string> {
    const apiKey = this.getApiKey();

    if (apiKey && apiKey.trim() !== '' && apiKey !== 'MY_GEMINI_API_KEY') {
      try {
        const answer = await this.callGeminiAskAegis(question, scanResult, apiKey);
        if (answer && answer.trim()) {
          return answer.trim();
        }
      } catch (e) {
        console.warn('Gemini Ask Aegis call failed, falling back to offline answer:', e);
      }
    }

    return this.generateOfflineAskAnswer(question, scanResult);
  }

  private async callGeminiRest(
    rawContent: string,
    scanType: ScanType,
    signals: RiskSignals,
    evaluation: RiskEvaluation,
    apiKey: string
  ): Promise<AIAnalysisOutput | null> {
    const endpoint = `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${apiKey}`;

    const prompt = `
You are AEGIS, a flagship cybersecurity AI engine specializing in social engineering, scam detection, and cognitive manipulation analysis.
Analyze the following digital content:

Input Type: ${ScanTypeLabels[scanType]}
Content: "${rawContent}"
Pre-detected Threat Signals: ${signals.signalDetails.join(", ")}
Pre-calculated Risk Score: ${evaluation.score}/100 (${evaluation.classification})
Threat Category: ${evaluation.threatCategory}

Answer strictly in valid JSON with no markdown formatting:
{
  "classification": "${evaluation.classification}",
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
    `.trim();

    const response = await fetch(endpoint, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        contents: [
          {
            parts: [{ text: prompt }]
          }
        ],
        generationConfig: {
          responseMimeType: 'application/json',
          temperature: 0.2
        }
      })
    });

    if (!response.ok) return null;
    const data = await response.json();
    const text = data?.candidates?.[0]?.content?.parts?.[0]?.text;
    if (!text) return null;

    const parsed = JSON.parse(text.trim());

    const chainList: AttackStep[] = Array.isArray(parsed.attackChain)
      ? parsed.attackChain.map((step: any, index: number) => ({
          stage: step.stage || "Exploitation Step",
          quoteOrEvidence: step.quoteOrEvidence || "Evidence detected",
          psychologicalHook: step.psychologicalHook || "Psychological trigger",
          order: step.order || index + 1
        }))
      : this.generateFallbackChain(signals, evaluation);

    return {
      classification: parsed.classification || evaluation.classification,
      intent: parsed.intent || evaluation.threatCategory,
      manipulationTechniques: Array.isArray(parsed.manipulation) && parsed.manipulation.length > 0
        ? parsed.manipulation
        : evaluation.primaryFlags,
      reasons: Array.isArray(parsed.reasons) && parsed.reasons.length > 0
        ? parsed.reasons
        : evaluation.primaryFlags,
      attackChain: chainList,
      phantomInsight: parsed.phantomInsight || evaluation.scamDNA.manipulationStrategy,
      recommendation: parsed.recommendation || evaluation.recommendation,
      isCloudEnhanced: true
    };
  }

  private async callGeminiAskAegis(question: string, scanResult: ScanResult, apiKey: string): Promise<string | null> {
    const endpoint = `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${apiKey}`;

    const evidenceSummary = scanResult.evidenceItems
      .map(it => `- ${it.signalName} (+${it.scoreContribution} pts): ${it.evidenceSnippet}. ${it.explanation}`)
      .join("\n");

    const prompt = `
You are "Ask AEGIS", an advanced, objective cybersecurity analyst assistant.
The user is asking a question about a scanned digital item (SMS, email, QR code, or website).

ANALYSIS CONTEXT:
- Content Type: ${ScanTypeLabels[scanResult.scanType]}
- Risk Score: ${scanResult.riskScore}/100 (${scanResult.classification})
- Threat Category: ${scanResult.threatCategory}
- Destination: ${scanResult.destinationUrl || "None"}
- Detected Evidence:
${evidenceSummary}
- Manipulation Strategy: ${scanResult.scamDNA.manipulationStrategy}
- Primary Recommendation: ${scanResult.recommendedAction}

USER QUESTION: "${question}"

STRICT GUIDELINES:
1. Answer using ONLY the provided analysis, detected signals, URL intelligence, and evidence.
2. Do NOT invent or assume external details (such as whether a real person sent it, or the sender's exact name) that are not in the evidence.
3. If the evidence is insufficient to answer the question, clearly state that the evidence does not provide that information, and state what is objectively known.
4. Never advise the user to click unknown links, pay unverified fees, or share credentials.
5. Keep the response direct, professional, concise (max 3-4 sentences), and free of jargon or hype.
    `.trim();

    const response = await fetch(endpoint, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        contents: [
          {
            parts: [{ text: prompt }]
          }
        ],
        generationConfig: {
          temperature: 0.2,
          maxOutputTokens: 300
        }
      })
    });

    if (!response.ok) return null;
    const data = await response.json();
    return data?.candidates?.[0]?.content?.parts?.[0]?.text?.trim() || null;
  }

  private generateOfflineReasoning(
    _rawContent: string,
    _scanType: ScanType,
    _destinationUrl: string | null,
    signals: RiskSignals,
    evaluation: RiskEvaluation
  ): AIAnalysisOutput {
    const manipulationList: string[] = [];
    if (signals.authorityImpersonation) manipulationList.push("AUTHORITY_IMPERSONATION");
    if (signals.threatLanguage) manipulationList.push("COERCION_AND_FEAR");
    if (signals.urgency) manipulationList.push("ARTIFICIAL_URGENCY");
    if (signals.financialRequest) manipulationList.push("FINANCIAL_EXTRACTION");
    if (signals.otpRequest || signals.passwordRequest) manipulationList.push("CREDENTIAL_HARVESTING");
    if (signals.rewardGreedTrap) manipulationList.push("GREED_EXPLOITATION");

    const chain = this.generateFallbackChain(signals, evaluation);

    return {
      classification: evaluation.classification,
      intent: evaluation.classification === RiskClassification.SAFE ? "Benign Digital Content" : evaluation.threatCategory,
      manipulationTechniques: evaluation.classification === RiskClassification.SAFE
        ? []
        : manipulationList.length > 0 ? manipulationList : ["UNVERIFIED_CONTENT"],
      reasons: evaluation.primaryFlags,
      attackChain: chain,
      phantomInsight: evaluation.scamDNA.manipulationStrategy,
      recommendation: evaluation.recommendation,
      isCloudEnhanced: false
    };
  }

  private generateFallbackChain(signals: RiskSignals, evaluation: RiskEvaluation): AttackStep[] {
    if (evaluation.classification === RiskClassification.SAFE) {
      return [
        {
          stage: "BENIGN INGRESS",
          quoteOrEvidence: "Standard digital content",
          psychologicalHook: "Normal non-deceptive communication",
          order: 1
        }
      ];
    }

    const steps: AttackStep[] = [];
    let order = 1;

    steps.push({
      stage: "INITIAL INGRESS",
      quoteOrEvidence: "Unsolicited digital trigger",
      psychologicalHook: "Catches recipient off-guard through unfamiliar communication",
      order: order++
    });

    if (signals.authorityImpersonation) {
      steps.push({
        stage: "FALSE PRETEXT",
        quoteOrEvidence: "Claims recognized institutional affiliation",
        psychologicalHook: "Disarms natural skepticism by leveraging institutional trust",
        order: order++
      });
    }

    if (signals.threatLanguage) {
      steps.push({
        stage: "PSYCHOLOGICAL COERCION",
        quoteOrEvidence: "Threatens immediate negative consequences",
        psychologicalHook: "Triggers panic to suppress analytical scrutiny",
        order: order++
      });
    }

    if (signals.urgency) {
      steps.push({
        stage: "TEMPORAL CONSTRAINT",
        quoteOrEvidence: "Enforces tight compliance deadline",
        psychologicalHook: "Prevents target from consulting official helpdesks",
        order: order++
      });
    }

    if (signals.otpRequest || signals.passwordRequest) {
      steps.push({
        stage: "AUTHENTICATION COMPROMISE",
        quoteOrEvidence: "Demands OTP or secret PIN",
        psychologicalHook: "Harvests final key needed for complete account takeover",
        order: order++
      });
    } else if (signals.financialRequest) {
      steps.push({
        stage: "PAYMENT DIVERSION",
        quoteOrEvidence: "Directs money transfer or QR code authorization",
        psychologicalHook: "Diverts funds into unmonitored recipient accounts",
        order: order++
      });
    }

    return steps;
  }

  private generateOfflineAskAnswer(question: string, scanResult: ScanResult): string {
    const q = question.toLowerCase();

    if (q.includes("why") && (q.includes("suspicious") || q.includes("flag") || q.includes("risk"))) {
      const topEvidence = scanResult.evidenceItems.slice(0, 2).map(it => it.signalName.toLowerCase()).join(" and ");
      if (topEvidence) {
        return `AEGIS flagged this primarily due to ${topEvidence}. Specifically: ${scanResult.scamDNA.manipulationStrategy}`;
      } else {
        return `AEGIS evaluated this content against heuristic threat patterns. No prominent deception signatures were detected.`;
      }
    }

    if (q.includes("what") && (q.includes("attacker") || q.includes("trying") || q.includes("goal"))) {
      return `Based on the detected indicators, the primary goal appears to be ${scanResult.threatCategory.toLowerCase()}. ${scanResult.scamDNA.manipulationStrategy}`;
    }

    if (q.includes("what should i do") || q.includes("next") || q.includes("action") || q.includes("how to proceed")) {
      return `Recommended action: ${scanResult.recommendedAction} Avoid clicking links or sharing authentication codes. If in doubt, contact the alleged organization via official channels.`;
    }

    if (q.includes("safe to click") || q.includes("safe to pay") || q.includes("safe")) {
      if (scanResult.riskScore >= 31) {
        return `No. The calculated risk score is ${scanResult.riskScore}/100 (${scanResult.classification}). Interacting with this content carries significant risk of fraud or credential theft.`;
      } else {
        return `The analysis detected no prominent attack signatures (Risk: ${scanResult.riskScore}/100). However, always ensure the destination matches the official website before entering personal details.`;
      }
    }

    if (q.includes("who sent") || q.includes("sender") || q.includes("identity")) {
      return `AEGIS cannot verify the sender's real-world identity from content alone. However, the message claims authority backing without cryptographically verifiable proof, a common hallmark of spoofing.`;
    }

    return `Based on available evidence, this item was classified as ${scanResult.classification} (${scanResult.threatCategory}) with a risk estimate of ${scanResult.riskScore}/100. Key recommendation: ${scanResult.recommendedAction}`;
  }
}
