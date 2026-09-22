import React, { useState } from 'react';
import {
  ArrowLeft,
  Shield,
  CheckCircle2,
  AlertTriangle,
  GitBranch,
  HelpCircle,
  Clock,
  Copy,
  Send,
  Loader2,
  Brain,
  QrCode,
  ExternalLink,
  Ban,
  Check
} from 'lucide-react';
import { useAegis } from '../context/AegisContext';
import { AnimatedRiskMeter } from '../components/AnimatedRiskMeter';
import { RiskClassification, SafeActionType, ScanTypeLabels } from '../../domain/models';

export const ResultScreen: React.FC = () => {
  const { currentResult, navigateTo, goBack, askAegis, isAskingAi } = useAegis();
  const [showEvidenceMode, setShowEvidenceMode] = useState(true);
  const [userQuestion, setUserQuestion] = useState('');
  const [copiedActionId, setCopiedActionId] = useState<string | null>(null);

  if (!currentResult) {
    return (
      <div className="min-h-[70vh] flex flex-col items-center justify-center text-center p-6">
        <Shield className="w-12 h-12 text-white/20 mb-3" />
        <p className="text-sm text-white/60">No active scan analysis available.</p>
        <button
          onClick={() => navigateTo('scanner')}
          className="mt-4 px-5 py-2.5 rounded-xl bg-[#10B981] text-[#0A0A0A] font-bold text-xs uppercase tracking-wider"
        >
          Go to Scanner
        </button>
      </div>
    );
  }

  const result = currentResult;

  const handlePerformSafeAction = (actionType: SafeActionType, payload?: string, actionId?: string) => {
    if (actionId) setCopiedActionId(actionId);
    setTimeout(() => setCopiedActionId(null), 2000);

    switch (actionType) {
      case SafeActionType.COPY_FOR_CYBERCRIME:
        if (payload) navigator.clipboard.writeText(payload);
        break;
      case SafeActionType.COPY_SANITIZED_URL:
        if (payload) navigator.clipboard.writeText(payload);
        break;
      case SafeActionType.SAFE_DISMISS:
        goBack('/');
        break;
      case SafeActionType.RECOMMEND_BLOCK:
        alert("To block this sender, open your SMS or messaging application, select the conversation, and tap 'Block & Report Spam'.");
        break;
      default:
        break;
    }
  };

  const displayCategory = result.classification === RiskClassification.SAFE
    ? (result.destinationUrl?.toLowerCase().includes('instagram') || result.urlIntelligence?.hostname.toLowerCase().includes('instagram')
      ? 'Verified Instagram Profile'
      : result.scanType === 'QR_CODE'
      ? 'Verified Safe QR Code'
      : 'Verified Benign Content')
    : result.threatCategory.replace(/_/g, ' ');

  return (
    <div className="w-full max-w-2xl mx-auto px-4 sm:px-6 pt-4 pb-28 md:pt-20 space-y-6">
      {/* Top Bar */}
      <div className="flex items-center justify-between py-2 border-b border-white/5">
        <div className="flex items-center gap-3">
          <button
            onClick={() => goBack('/')}
            className="min-h-[44px] min-w-[44px] rounded-xl bg-white/5 hover:bg-white/10 active:scale-95 flex items-center justify-center text-white/80 transition-all cursor-pointer"
            aria-label="Go back"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          <div>
            <h1 className="text-sm sm:text-base font-bold text-[#F5F5F5] tracking-wider uppercase">
              DECEPTION ASSESSMENT
            </h1>
            <p className="text-[11px] text-[#34D399]">
              {ScanTypeLabels[result.scanType]} • {displayCategory}
            </p>
          </div>
        </div>

        {result.isAIEnhanced && (
          <div className="flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-[#10B981]/15 border border-[#10B981]/40 text-[10px] font-bold text-[#10B981] uppercase tracking-wider">
            <span className="w-1.5 h-1.5 rounded-full bg-[#10B981] animate-pulse" />
            <span>AI REASONED</span>
          </div>
        )}
      </div>

      {/* Hero Circular Risk Meter */}
      <div className="flex flex-col items-center justify-center text-center py-2">
        <AnimatedRiskMeter
          score={result.riskScore}
          classification={result.classification}
          size={190}
        />
        <h2 className="text-sm font-bold tracking-wider uppercase mt-4 text-[#F5F5F5]">
          {displayCategory}
        </h2>
        <p className="text-xs text-white/60 max-w-md mt-1 px-4 leading-relaxed">
          {result.intent}
        </p>
      </div>

      {/* 1. EVIDENCE MODE SECTION */}
      <div className="bg-[#141414] border border-white/10 rounded-2xl p-4 sm:p-5">
        <div className="flex items-center justify-between mb-3">
          <div className="flex items-center gap-2">
            <Shield className="w-4 h-4 text-[#10B981]" />
            <h3 className="text-[11px] font-bold tracking-wider text-[#10B981] uppercase">
              EVIDENCE MODE
            </h3>
          </div>
          <button
            onClick={() => setShowEvidenceMode(!showEvidenceMode)}
            className="text-[10px] font-bold text-white/40 hover:text-white/80 uppercase"
          >
            {showEvidenceMode ? 'HIDE' : 'SHOW DETAILS'}
          </button>
        </div>

        {showEvidenceMode && (
          <div className="space-y-3 pt-1">
            <p className="text-[11px] text-white/50 leading-relaxed">
              Score: {result.riskScore}/100 • Risk estimate based on verified heuristic indicators rather than proof of fraud.
            </p>

            {result.evidenceItems.length === 0 ? (
              <p className="text-xs text-[#34D399] font-medium">
                No anomalous risk signals detected in content.
              </p>
            ) : (
              <div className="space-y-2.5 pt-1">
                {result.evidenceItems.map((item, idx) => (
                  <div key={idx} className="bg-[#0A0A0A] border border-white/5 rounded-xl p-3">
                    <div className="flex items-center justify-between mb-1">
                      <span className="text-xs font-bold text-[#F5F5F5]">
                        {item.signalName}
                      </span>
                      <span
                        className={`text-[10px] font-bold px-2 py-0.5 rounded ${
                          item.scoreContribution > 0
                            ? 'bg-[#EF4444]/20 text-[#EF4444]'
                            : 'bg-[#10B981]/20 text-[#10B981]'
                        }`}
                      >
                        {item.scoreContribution > 0 ? `+${item.scoreContribution} pts` : `${item.scoreContribution} pts`}
                      </span>
                    </div>
                    <p className="text-[11px] font-mono text-[#34D399] truncate">
                      Snippet: {item.evidenceSnippet}
                    </p>
                    <p className="text-[11px] text-white/60 mt-1 leading-normal">
                      {item.explanation}
                    </p>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
      </div>

      {/* 2. SCAM DNA SECTION */}
      <div className="bg-[#141414] border border-[#10B981]/30 rounded-2xl p-4 sm:p-5">
        <div className="flex items-center gap-2 mb-3">
          <GitBranch className="w-4 h-4 text-[#34D399]" />
          <h3 className="text-[11px] font-bold tracking-wider text-[#34D399] uppercase">
            SCAM DNA • MANIPULATION STRATEGY
          </h3>
        </div>

        <p className="text-xs sm:text-sm font-semibold text-[#F5F5F5] leading-relaxed">
          {result.scamDNA.manipulationStrategy}
        </p>

        {result.scamDNA.sequence.length > 0 && (
          <div className="mt-4 space-y-3 border-t border-white/5 pt-3">
            {result.scamDNA.sequence.map((step, idx) => (
              <div key={idx} className="flex items-start gap-3">
                <div className="w-5 h-5 rounded-full bg-[#10B981]/20 border border-[#10B981]/50 flex items-center justify-center text-[10px] font-bold text-[#34D399] shrink-0 mt-0.5">
                  {step.order}
                </div>
                <div>
                  <div className="flex items-center gap-2">
                    <span className="text-xs font-bold text-[#F5F5F5]">{step.title}</span>
                    <span className="text-[9px] font-mono px-1.5 py-0.5 rounded bg-white/5 text-white/50">
                      {step.tactic}
                    </span>
                  </div>
                  <p className="text-[11px] text-white/60 mt-0.5 leading-relaxed">
                    {step.psychologicalPurpose}
                  </p>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* 3. TRUST BREAKDOWN (7 Multi-Dimensional Trust Indicators) */}
      <div className="bg-[#141414] border border-white/10 rounded-2xl p-4 sm:p-5">
        <div className="flex items-center gap-2 mb-2">
          <Shield className="w-4 h-4 text-[#38BDF8]" />
          <h3 className="text-[11px] font-bold tracking-wider text-[#38BDF8] uppercase">
            TRUST BREAKDOWN (7-AXIS MATRIX)
          </h3>
        </div>

        <p className="text-[11px] text-white/50 mb-4">
          {result.trustBreakdown.summary}
        </p>

        <div className="space-y-3">
          {[
            { label: 'Identity Trust Score', value: result.trustBreakdown.identityTrustScore, isRisk: false },
            { label: 'Message Integrity', value: result.trustBreakdown.messageTrustScore, isRisk: false },
            { label: 'Destination Trust', value: result.trustBreakdown.destinationTrustScore, isRisk: false },
            { label: 'Financial Extraction Risk', value: result.trustBreakdown.financialRiskScore, isRisk: true },
            { label: 'Artificial Urgency Level', value: result.trustBreakdown.urgencyScore, isRisk: true },
            { label: 'Credential Harvesting Risk', value: result.trustBreakdown.credentialRiskScore, isRisk: true },
            { label: 'Psychological Manipulation', value: result.trustBreakdown.manipulationRiskScore, isRisk: true },
          ].map((dim, idx) => (
            <div key={idx} className="space-y-1">
              <div className="flex items-center justify-between text-xs">
                <span className="text-white/70 font-medium">{dim.label}</span>
                <span className={`font-mono font-bold ${dim.isRisk ? (dim.value > 50 ? 'text-[#EF4444]' : 'text-[#34D399]') : (dim.value > 50 ? 'text-[#34D399]' : 'text-[#EF4444]')}`}>
                  {dim.value}%
                </span>
              </div>
              <div className="w-full h-1.5 bg-[#0A0A0A] rounded-full overflow-hidden">
                <div
                  className={`h-full rounded-full transition-all duration-500 ${
                    dim.isRisk
                      ? dim.value > 50 ? 'bg-[#EF4444]' : 'bg-[#34D399]'
                      : dim.value > 50 ? 'bg-[#34D399]' : 'bg-[#EF4444]'
                  }`}
                  style={{ width: `${dim.value}%` }}
                />
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* 4. VERIFY BEFORE YOU ACT */}
      <div className="space-y-3">
        <div className="flex items-center gap-2">
          <HelpCircle className="w-4 h-4 text-[#F59E0B]" />
          <h3 className="text-[11px] font-bold tracking-wider text-[#F59E0B] uppercase">
            VERIFY BEFORE YOU ACT
          </h3>
        </div>

        {result.verificationGuidelines.map((item, idx) => (
          <div key={idx} className="bg-[#141414] border border-[#F59E0B]/30 rounded-2xl p-4 space-y-2">
            <h4 className="text-xs font-bold text-[#F5F5F5]">{item.title}</h4>
            <p className="text-xs text-white/70 leading-relaxed">{item.recommendedAction}</p>
            <div className="pt-1 flex flex-col gap-1 text-[11px]">
              <div className="flex items-center gap-1.5">
                <span className="font-bold text-[#34D399]">Safe Channel:</span>
                <span className="text-[#F5F5F5]">{item.safeChannel}</span>
              </div>
              <div className="text-[#EF4444] font-semibold">{item.strictWarning}</div>
            </div>
          </div>
        ))}
      </div>

      {/* 5. WHAT COULD HAPPEN? */}
      <div className="bg-[#141414] border border-[#A855F7]/30 rounded-2xl p-4 sm:p-5 space-y-3">
        <div className="flex items-center gap-2">
          <AlertTriangle className="w-4 h-4 text-[#A855F7]" />
          <h3 className="text-[11px] font-bold tracking-wider text-[#A855F7] uppercase">
            WHAT COULD HAPPEN?
          </h3>
        </div>

        <h4 className="text-xs font-bold text-[#F5F5F5]">{result.whatCouldHappen.title}</h4>

        <div className="space-y-2">
          {result.whatCouldHappen.chainSteps.map((step, idx) => (
            <p key={idx} className="text-xs text-white/70 leading-relaxed pl-2 border-l border-[#A855F7]/40">
              {step}
            </p>
          ))}
        </div>

        <p className="text-[10px] text-white/40 pt-1">
          ⚠ {result.whatCouldHappen.disclaimer}
        </p>
      </div>

      {/* 6. RISK TIMELINE */}
      <div className="bg-[#141414] border border-white/10 rounded-2xl p-4 sm:p-5 space-y-3">
        <div className="flex items-center gap-2">
          <Clock className="w-4 h-4 text-[#10B981]" />
          <h3 className="text-[11px] font-bold tracking-wider text-[#10B981] uppercase">
            RISK TIMELINE • MANIPULATION SEQUENCE
          </h3>
        </div>

        <p className="text-[10px] text-white/40">
          Estimated progression based on detected cognitive hooks. Aegis does not assert knowledge of the attacker's actual timeline.
        </p>

        <div className="space-y-3 pt-2">
          {result.riskTimeline.map((step, idx) => (
            <div key={idx} className="flex items-start gap-3 text-xs">
              <span
                className={`w-2 h-2 rounded-full mt-1 shrink-0 ${
                  step.isDetected ? 'bg-[#EF4444] ring-4 ring-[#EF4444]/20' : 'bg-white/20'
                }`}
              />
              <div>
                <div className="flex items-center gap-2">
                  <span className="font-bold text-[#F5F5F5]">{step.stage}</span>
                  {step.isDetected && (
                    <span className="text-[9px] font-bold px-1.5 py-0.5 rounded bg-[#EF4444]/20 text-[#EF4444] uppercase">
                      DETECTED
                    </span>
                  )}
                </div>
                <p className="text-white/60 mt-0.5 leading-normal">{step.description}</p>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* 7. SAFE ACTION SHORTCUTS */}
      {result.safeActions.length > 0 && (
        <div className="space-y-3">
          <h3 className="text-[11px] font-bold tracking-wider text-[#34D399] uppercase px-1">
            SAFE ACTION SHORTCUTS
          </h3>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            {result.safeActions.map((action) => {
              const isCopied = copiedActionId === action.id;
              return (
                <div
                  key={action.id}
                  onClick={() => handlePerformSafeAction(action.actionType, action.payload, action.id)}
                  className="bg-[#141414] hover:bg-[#1A1A1A] border border-white/10 hover:border-[#10B981]/40 rounded-xl p-3.5 cursor-pointer transition-all flex flex-col justify-between"
                >
                  <div>
                    <div className="flex items-center justify-between">
                      <span className="text-xs font-bold text-[#F5F5F5]">{action.title}</span>
                      {isCopied ? (
                        <Check className="w-3.5 h-3.5 text-[#34D399]" />
                      ) : (
                        <Copy className="w-3.5 h-3.5 text-white/40" />
                      )}
                    </div>
                    <p className="text-[11px] text-white/50 mt-1 leading-relaxed">
                      {action.description}
                    </p>
                  </div>
                  {isCopied && (
                    <span className="text-[9px] font-bold text-[#34D399] uppercase mt-2">
                      Copied to clipboard!
                    </span>
                  )}
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* 8. ASK AEGIS (Interactive Q&A Assistant) */}
      <div className="bg-[#141414] border border-[#10B981]/30 rounded-2xl p-4 sm:p-5 space-y-3">
        <div className="flex items-center gap-2">
          <Brain className="w-4 h-4 text-[#10B981]" />
          <h3 className="text-[11px] font-bold tracking-wider text-[#10B981] uppercase">
            ASK AEGIS
          </h3>
        </div>

        <p className="text-[11px] text-white/50">
          Ask questions regarding this specific analysis. Answers are strictly constrained to verified evidence without assumptions.
        </p>

        {/* Suggested Query Chips */}
        <div className="flex flex-wrap gap-2 pt-1">
          {[
            "Why is this suspicious?",
            "What is the attacker trying to do?",
            "What should I do next?",
            "Is it safe to click or pay?"
          ].map((chip, idx) => (
            <button
              key={idx}
              onClick={() => {
                setUserQuestion(chip);
                askAegis(chip);
              }}
              className="text-[10px] font-medium px-2.5 py-1 rounded-full bg-[#0A0A0A] border border-white/10 hover:border-[#10B981]/50 text-white/80 transition-colors"
            >
              {chip}
            </button>
          ))}
        </div>

        {/* Input Field */}
        <div className="flex items-center gap-2 pt-2">
          <input
            type="text"
            value={userQuestion}
            onChange={(e) => setUserQuestion(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && userQuestion.trim() && askAegis(userQuestion)}
            placeholder="Ask a question about this threat..."
            className="flex-1 bg-[#0A0A0A] border border-white/10 focus:border-[#10B981] rounded-xl px-3.5 py-2.5 text-xs text-[#F5F5F5] placeholder:text-white/30 focus:outline-none"
          />
          <button
            onClick={() => userQuestion.trim() && askAegis(userQuestion)}
            disabled={isAskingAi || !userQuestion.trim()}
            className="w-10 h-10 rounded-xl bg-[#10B981] hover:bg-[#059669] disabled:opacity-40 text-[#0A0A0A] flex items-center justify-center transition-colors shrink-0"
          >
            {isAskingAi ? <Loader2 className="w-4 h-4 animate-spin" /> : <Send className="w-4 h-4" />}
          </button>
        </div>

        {/* Q&A History */}
        {result.qnaHistory.length > 0 && (
          <div className="space-y-2.5 pt-3 border-t border-white/5">
            {result.qnaHistory.map((qna, idx) => (
              <div key={idx} className="bg-[#0A0A0A] rounded-xl p-3 space-y-1.5">
                <p className="text-xs font-bold text-[#34D399]">Q: {qna.question}</p>
                <p className="text-xs text-[#F5F5F5] leading-relaxed">{qna.answer}</p>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Action Buttons */}
      <div className="space-y-3 pt-2">
        <button
          onClick={() => navigateTo('explanation')}
          className="w-full py-3.5 rounded-xl bg-[#10B981] hover:bg-[#059669] text-[#0A0A0A] font-bold text-xs tracking-wider uppercase flex items-center justify-center gap-2 transition-all shadow-[0_0_20px_rgba(16,185,129,0.25)]"
        >
          <Brain className="w-4 h-4" />
          <span>VIEW FULL ATTACK REASONING</span>
        </button>

        <div className="grid grid-cols-2 gap-3">
          <button
            onClick={() => navigateTo('scanner')}
            className="py-3 rounded-xl bg-white/5 hover:bg-white/10 border border-white/10 text-[#34D399] font-bold text-xs tracking-wider uppercase flex items-center justify-center gap-2 transition-colors"
          >
            <QrCode className="w-4 h-4" />
            <span>Scan Another</span>
          </button>

          <button
            onClick={() => navigateTo('history')}
            className="py-3 rounded-xl bg-white/5 hover:bg-white/10 border border-white/10 text-white/80 font-bold text-xs tracking-wider uppercase flex items-center justify-center gap-2 transition-colors"
          >
            <span>Scan History</span>
          </button>
        </div>
      </div>
    </div>
  );
};
