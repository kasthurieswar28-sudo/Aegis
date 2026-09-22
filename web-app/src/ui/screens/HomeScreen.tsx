import React from 'react';
import { Shield, PlayCircle, Globe, Lock, CreditCard } from 'lucide-react';
import { useAegis } from '../context/AegisContext';
import { RadarHeroControl } from '../components/RadarHeroControl';
import { TelemetryWave } from '../components/TelemetryWave';

export const HomeScreen: React.FC = () => {
  const { navigateTo } = useAegis();

  return (
    <div className="w-full max-w-2xl mx-auto px-4 sm:px-6 pt-4 pb-28 md:pt-20 flex flex-col items-center">
      {/* 1. SECURITY STATUS MODULE */}
      <div className="w-full mb-5">
        <div className="relative overflow-hidden w-full rounded-2xl bg-gradient-to-r from-[#141414] to-[#101010] border border-white/10 p-4 sm:p-5 shadow-lg">
          {/* Subtle Ambient illumination */}
          <div className="absolute -top-10 -right-10 w-28 h-28 rounded-full bg-[#10B981]/15 blur-2xl pointer-events-none" />

          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3.5">
              {/* Micro-radar shield icon box */}
              <div className="relative w-11 h-11 rounded-xl bg-[#181818] border border-[#34D399]/25 flex items-center justify-center shrink-0">
                <svg className="absolute inset-0 w-full h-full pointer-events-none" viewBox="0 0 44 44">
                  <circle cx="22" cy="22" r="17" fill="none" stroke="rgba(255,255,255,0.1)" strokeWidth="1.2" />
                  <circle
                    cx="22"
                    cy="22"
                    r="17"
                    fill="none"
                    stroke="#34D399"
                    strokeWidth="1.5"
                    strokeLinecap="round"
                    strokeDasharray="25 100"
                    className="animate-radar origin-center"
                    opacity="0.8"
                  />
                </svg>
                <Shield className="w-5 h-5 text-[#34D399] relative z-10" />
              </div>

              <div>
                <div className="flex items-center gap-1.5">
                  <span className="text-[10px] font-bold tracking-wider text-[#34D399] uppercase">
                    SYSTEM SHIELD
                  </span>
                  <span className="w-1 h-1 rounded-full bg-[#34D399]" />
                  <span className="text-[9px] font-mono font-semibold text-white/30 uppercase">
                    ARMED
                  </span>
                </div>
                <h2 className="text-sm sm:text-base font-semibold text-[#F5F5F5] -mt-0.5">
                  Protection Active
                </h2>
                <p className="text-[11px] text-white/60">
                  On-device deception monitoring
                </p>
              </div>
            </div>

            {/* Minimal telemetry signal bars */}
            <div className="flex items-end gap-1 h-5 shrink-0 pl-2">
              <div className="w-0.5 h-2 rounded-sm bg-[#34D399]/40" />
              <div className="w-0.5 h-3.5 rounded-sm bg-[#34D399]/70" />
              <div className="w-0.5 h-5 rounded-sm bg-[#34D399]" />
            </div>
          </div>
        </div>
      </div>

      {/* 3. PRIMARY HERO: SCAN & TRUST */}
      <div className="w-full flex justify-center py-4 my-2">
        <RadarHeroControl onClick={() => navigateTo('scanner')} />
      </div>

      {/* 4. SYSTEM INTELLIGENCE STRIP */}
      <div className="w-full mb-6">
        <div className="w-full rounded-xl bg-[#101010] border border-white/10 px-4 py-2.5 flex items-center justify-between">
          <div className="flex items-center gap-1.5">
            <span className="w-1.5 h-1.5 rounded-full bg-[#34D399] animate-pulse" />
            <span className="text-[9px] font-mono font-bold tracking-wider text-[#34D399] uppercase">
              ISOLATED ENCLAVE
            </span>
          </div>

          <TelemetryWave width={90} height={16} className="shrink-0" />

          <span className="text-[9px] font-mono font-semibold tracking-wider text-white/30 uppercase">
            ON-DEVICE ENGINE
          </span>
        </div>
      </div>

      {/* 5. ADVANCED PROTECTION TOOLS SECTION */}
      <div className="w-full space-y-3">
        <p className="text-[10px] font-bold tracking-[0.15em] text-white/40 uppercase px-1">
          ADVANCED PROTECTION TOOLS
        </p>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          {/* Tool 1: Threat Simulator */}
          <div
            onClick={() => navigateTo('demo_mode')}
            className="bg-[#131313] hover:bg-[#181818] border border-white/10 hover:border-[#10B981]/40 rounded-2xl p-4 cursor-pointer transition-all duration-200 group flex flex-col justify-between"
          >
            <div className="flex items-center justify-between mb-3">
              <div className="w-9 h-9 rounded-xl bg-[#10B981]/15 border border-[#10B981]/30 flex items-center justify-center">
                <PlayCircle className="w-5 h-5 text-[#34D399]" />
              </div>
              <div className="flex items-center gap-1">
                <span className="w-1 h-1 rounded-full bg-[#34D399]" />
                <span className="text-[8.5px] font-mono font-bold text-[#34D399] tracking-wider uppercase">
                  ARMED
                </span>
              </div>
            </div>
            <div>
              <h3 className="text-sm font-bold text-[#F5F5F5] group-hover:text-[#34D399] transition-colors">
                Threat Simulator
              </h3>
              <p className="text-[11px] text-white/50 mt-0.5">
                Synthetic attack testing
              </p>
            </div>
          </div>

          {/* Tool 2: URL Intelligence */}
          <div
            onClick={() => navigateTo('url_intelligence')}
            className="bg-[#131313] hover:bg-[#181818] border border-white/10 hover:border-[#38BDF8]/40 rounded-2xl p-4 cursor-pointer transition-all duration-200 group flex flex-col justify-between"
          >
            <div className="flex items-center justify-between mb-3">
              <div className="w-9 h-9 rounded-xl bg-[#38BDF8]/15 border border-[#38BDF8]/30 flex items-center justify-center">
                <Globe className="w-5 h-5 text-[#38BDF8]" />
              </div>
              <div className="flex items-center gap-1">
                <span className="w-1 h-1 rounded-full bg-[#38BDF8]" />
                <span className="text-[8.5px] font-mono font-bold text-[#38BDF8] tracking-wider uppercase">
                  ACTIVE
                </span>
              </div>
            </div>
            <div>
              <h3 className="text-sm font-bold text-[#F5F5F5] group-hover:text-[#38BDF8] transition-colors">
                URL Intelligence
              </h3>
              <p className="text-[11px] text-white/50 mt-0.5">
                Deep domain inspection
              </p>
            </div>
          </div>

          {/* Tool 3: Privacy Center */}
          <div
            onClick={() => navigateTo('privacy_center')}
            className="bg-[#131313] hover:bg-[#181818] border border-white/10 hover:border-[#10B981]/40 rounded-2xl p-4 cursor-pointer transition-all duration-200 group flex flex-col justify-between"
          >
            <div className="flex items-center justify-between mb-3">
              <div className="w-9 h-9 rounded-xl bg-[#10B981]/15 border border-[#10B981]/30 flex items-center justify-center">
                <Lock className="w-5 h-5 text-[#34D399]" />
              </div>
              <div className="flex items-center gap-1">
                <span className="w-1 h-1 rounded-full bg-[#34D399]" />
                <span className="text-[8.5px] font-mono font-bold text-[#34D399] tracking-wider uppercase">
                  VERIFIED
                </span>
              </div>
            </div>
            <div>
              <h3 className="text-sm font-bold text-[#F5F5F5] group-hover:text-[#34D399] transition-colors">
                Privacy Center
              </h3>
              <p className="text-[11px] text-white/50 mt-0.5">
                On-device privacy policy
              </p>
            </div>
          </div>

          {/* Tool 4: Payment Check */}
          <div
            onClick={() => navigateTo('fakepayment')}
            className="bg-[#131313] hover:bg-[#181818] border border-white/10 hover:border-[#38BDF8]/40 rounded-2xl p-4 cursor-pointer transition-all duration-200 group flex flex-col justify-between"
          >
            <div className="flex items-center justify-between mb-3">
              <div className="w-9 h-9 rounded-xl bg-[#38BDF8]/15 border border-[#38BDF8]/30 flex items-center justify-center">
                <CreditCard className="w-5 h-5 text-[#38BDF8]" />
              </div>
              <div className="flex items-center gap-1">
                <span className="w-1 h-1 rounded-full bg-[#38BDF8]" />
                <span className="text-[8.5px] font-mono font-bold text-[#38BDF8] tracking-wider uppercase">
                  READY
                </span>
              </div>
            </div>
            <div>
              <h3 className="text-sm font-bold text-[#F5F5F5] group-hover:text-[#38BDF8] transition-colors">
                Payment Check
              </h3>
              <p className="text-[11px] text-white/50 mt-0.5">
                Receipt integrity analysis
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
