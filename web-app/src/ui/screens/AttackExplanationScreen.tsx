import React from 'react';
import { ArrowLeft, Brain, Shield, AlertTriangle } from 'lucide-react';
import { useAegis } from '../context/AegisContext';

export const AttackExplanationScreen: React.FC = () => {
  const { currentResult, navigateTo, goBack } = useAegis();

  if (!currentResult) {
    return (
      <div className="min-h-[70vh] flex flex-col items-center justify-center text-center p-6">
        <p className="text-sm text-white/60">No active scan analysis available.</p>
        <button
          onClick={() => navigateTo('home')}
          className="mt-4 px-5 py-2.5 rounded-xl bg-[#10B981] text-[#0A0A0A] font-bold text-xs uppercase"
        >
          Return Home
        </button>
      </div>
    );
  }

  const result = currentResult;

  return (
    <div className="w-full max-w-2xl mx-auto px-4 sm:px-6 pt-4 pb-28 md:pt-20 space-y-6">
      {/* Top Header */}
      <div className="flex items-center gap-3 py-2 border-b border-white/5">
        <button
          onClick={() => goBack('/result')}
          className="min-h-[44px] min-w-[44px] rounded-xl bg-white/5 hover:bg-white/10 active:scale-95 flex items-center justify-center text-white/80 transition-all cursor-pointer"
          aria-label="Go back to result"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>
        <div>
          <h1 className="text-sm sm:text-base font-bold text-[#F5F5F5] tracking-wider uppercase">
            ATTACK REASONING & CHAIN
          </h1>
          <p className="text-[11px] text-[#34D399]">
            Psychological Vector Breakdown
          </p>
        </div>
      </div>

      {/* AEGIS Insight Card */}
      <div className="bg-[#141414] border border-white/10 rounded-2xl p-5 shadow-lg">
        <div className="flex items-center gap-2.5 mb-3">
          <div className="w-8 h-8 rounded-lg bg-[#10B981]/15 border border-[#10B981]/30 flex items-center justify-center text-[#34D399]">
            <Brain className="w-4 h-4" />
          </div>
          <span className="text-[11px] font-bold tracking-wider text-[#34D399] uppercase">
            AEGIS INSIGHT
          </span>
        </div>
        <p className="text-sm sm:text-base text-[#F5F5F5] leading-relaxed italic">
          "{result.phantomInsight}"
        </p>
      </div>

      {/* Attack Chain Steps */}
      <div className="space-y-3">
        <div>
          <h2 className="text-xs font-bold tracking-wider text-white/50 uppercase">
            STEP-BY-STEP ATTACK CHAIN
          </h2>
          <p className="text-[11px] text-white/40 mt-0.5">
            How the attacker attempts to engineer compliance:
          </p>
        </div>

        <div className="space-y-3 pt-1">
          {result.attackChain.map((step, idx) => {
            const isLast = idx === result.attackChain.length - 1;
            return (
              <div key={idx} className="flex items-start gap-3">
                <div className="flex flex-col items-center">
                  <div
                    className={`w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold text-[#0A0A0A] shrink-0 ${
                      isLast ? 'bg-[#EF4444]' : 'bg-[#10B981]'
                    }`}
                  >
                    {step.order || idx + 1}
                  </div>
                  {!isLast && <div className="w-0.5 h-12 bg-white/10 my-1" />}
                </div>

                <div className="flex-1 bg-[#141414] border border-white/10 rounded-xl p-3.5 space-y-1">
                  <span className="text-xs font-bold text-[#34D399] uppercase tracking-wider block">
                    {step.stage}
                  </span>
                  {step.quoteOrEvidence && (
                    <p className="text-xs font-semibold text-[#F5F5F5]">
                      {step.quoteOrEvidence}
                    </p>
                  )}
                  <p className="text-[11px] text-white/60 leading-relaxed">
                    Psychological Hook: {step.psychologicalHook}
                  </p>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Safeguards & Immediate Counter-Measures */}
      <div className="bg-[#141414] border border-[#10B981]/30 rounded-2xl p-5 space-y-4">
        <div className="flex items-center gap-2">
          <Shield className="w-4 h-4 text-[#10B981]" />
          <h3 className="text-xs font-bold tracking-wider text-[#10B981] uppercase">
            SAFEGUARDS & IMMEDIATE COUNTER-MEASURES
          </h3>
        </div>

        <div className="space-y-3 text-xs">
          <div className="flex items-start gap-3">
            <span className="w-5 h-5 rounded-full bg-[#10B981]/20 text-[#34D399] font-bold flex items-center justify-center shrink-0 text-[10px]">
              1
            </span>
            <div>
              <p className="font-bold text-[#F5F5F5]">Never act inside the attacker's timeline</p>
              <p className="text-white/60 mt-0.5 leading-relaxed">
                Artificial deadlines are designed to prevent independent thinking. Take a breath and pause.
              </p>
            </div>
          </div>

          <div className="flex items-start gap-3">
            <span className="w-5 h-5 rounded-full bg-[#10B981]/20 text-[#34D399] font-bold flex items-center justify-center shrink-0 text-[10px]">
              2
            </span>
            <div>
              <p className="font-bold text-[#F5F5F5]">Verify via independent official channels</p>
              <p className="text-white/60 mt-0.5 leading-relaxed">
                Close the link/message. Open your official banking app or call the verified phone number printed on the back of your card.
              </p>
            </div>
          </div>

          <div className="flex items-start gap-3">
            <span className="w-5 h-5 rounded-full bg-[#10B981]/20 text-[#34D399] font-bold flex items-center justify-center shrink-0 text-[10px]">
              3
            </span>
            <div>
              <p className="font-bold text-[#F5F5F5]">Do not share OTP or scan QR to receive money</p>
              <p className="text-white/60 mt-0.5 leading-relaxed">
                Scanning a QR code in payment apps is strictly for sending money out, never for receiving refunds.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
