import React from 'react';
import {
  ArrowLeft,
  Zap,
  QrCode,
  CreditCard,
  Smartphone,
  Headphones,
  TrendingUp,
  Globe,
  CheckCircle,
  Shield
} from 'lucide-react';
import { useAegis } from '../context/AegisContext';
import { SYNTHETIC_SCENARIOS } from '../../data/scenarios/SyntheticScenarios';
import { RiskClassification } from '../../domain/models';

const ICON_MAP: Record<string, React.ElementType> = {
  QrCode,
  CreditCard,
  Smartphone,
  Headphones,
  TrendingUp,
  Globe,
  CheckCircle,
  Shield
};

export const DemoScreen: React.FC = () => {
  const { navigateTo, goBack, runSyntheticScenario } = useAegis();

  return (
    <div className="w-full max-w-2xl mx-auto px-4 sm:px-6 pt-4 pb-28 md:pt-20 space-y-5">
      {/* Top Header */}
      <div className="flex items-center gap-3 py-2 border-b border-white/5">
        <button
          onClick={() => goBack('/')}
          className="min-h-[44px] min-w-[44px] rounded-xl bg-white/5 hover:bg-white/10 active:scale-95 flex items-center justify-center text-white/80 transition-all cursor-pointer"
          aria-label="Go back"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>
        <div>
          <h1 className="text-sm sm:text-base font-bold text-[#F5F5F5] tracking-wider uppercase">
            SYNTHETIC THREAT SIMULATOR
          </h1>
          <p className="text-[11px] text-[#34D399]">
            Deterministic Cybersecurity Scenarios
          </p>
        </div>
      </div>

      {/* Info Banner */}
      <div className="bg-[#10B981]/10 border border-[#10B981]/30 rounded-2xl p-4 flex items-start gap-3">
        <Zap className="w-5 h-5 text-[#34D399] shrink-0 mt-0.5" />
        <p className="text-xs text-[#F5F5F5] leading-relaxed">
          These controlled, synthetic scenarios test Aegis's detection engine without exposing real credentials. Tapping any scenario feeds it into the live analysis pipeline to display real Scam DNA, Evidence Mode, and Trust Breakdown.
        </p>
      </div>

      {/* Scenarios List */}
      <div className="space-y-3">
        {SYNTHETIC_SCENARIOS.map((scenario) => {
          const Icon = ICON_MAP[scenario.iconName] || Shield;
          const isCritical = scenario.expectedClassification === RiskClassification.CRITICAL;
          const isHigh = scenario.expectedClassification === RiskClassification.HIGH_RISK;
          const isSafe = scenario.expectedClassification === RiskClassification.SAFE;

          const colorClass = isCritical
            ? 'text-[#EF4444] border-[#EF4444]/40 bg-[#EF4444]/10'
            : isHigh
            ? 'text-[#F59E0B] border-[#F59E0B]/40 bg-[#F59E0B]/10'
            : 'text-[#34D399] border-[#10B981]/40 bg-[#10B981]/10';

          return (
            <div
              key={scenario.id}
              onClick={() =>
                runSyntheticScenario(
                  scenario.title,
                  scenario.syntheticContent,
                  scenario.scanType,
                  scenario.destinationUrl
                )
              }
              className="bg-[#141414] hover:bg-[#181818] border border-white/10 hover:border-[#10B981]/40 rounded-2xl p-4 sm:p-5 cursor-pointer transition-all duration-200 group space-y-3 shadow-md"
            >
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <div className={`w-10 h-10 rounded-xl flex items-center justify-center border ${colorClass}`}>
                    <Icon className="w-5 h-5" />
                  </div>
                  <div>
                    <h3 className="text-sm font-bold text-[#F5F5F5] group-hover:text-[#34D399] transition-colors">
                      {scenario.title}
                    </h3>
                    <p className="text-[11px] text-white/40">
                      {scenario.category.replace(/_/g, ' ')}
                    </p>
                  </div>
                </div>

                <span className={`text-[10px] font-bold px-2.5 py-1 rounded-md border tracking-wider uppercase ${colorClass}`}>
                  {scenario.expectedClassification}
                </span>
              </div>

              <p className="text-xs text-white/70 leading-relaxed">
                {scenario.description}
              </p>

              <div className="bg-[#0A0A0A] rounded-xl p-2.5 font-mono text-[10px] text-[#34D399]/80 truncate">
                {scenario.syntheticContent}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
