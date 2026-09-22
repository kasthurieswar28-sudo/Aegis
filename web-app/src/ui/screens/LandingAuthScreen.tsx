import React from 'react';
import { Shield, QrCode, Fingerprint, Lock, AlertTriangle, ArrowRight, Loader2 } from 'lucide-react';
import { useAegis } from '../context/AegisContext';

export const LandingAuthScreen: React.FC = () => {
  const { signInAsGuest, authLoading, authError } = useAegis();

  return (
    <div className="min-h-screen bg-[#0A0A0A] flex flex-col items-center justify-center px-4 sm:px-6 py-10 relative overflow-hidden">
      {/* Background Ambient Radial Glows */}
      <div className="absolute top-1/4 left-1/2 -translate-x-1/2 -translate-y-1/2 w-96 h-96 rounded-full bg-[#10B981]/10 blur-3xl pointer-events-none" />
      <div className="absolute bottom-10 right-10 w-72 h-72 rounded-full bg-[#34D399]/5 blur-2xl pointer-events-none" />

      <div className="w-full max-w-md relative z-10 flex flex-col items-center text-center">
        {/* 1. HERO BRANDING & SHIELD EMBLEM */}
        <div className="relative mb-6">
          <div className="w-24 h-24 rounded-full bg-gradient-to-tr from-[#10B981]/25 via-[#10B981]/10 to-transparent border border-[#34D399]/60 flex items-center justify-center shadow-[0_0_30px_rgba(16,185,129,0.3)] animate-pulse-slow">
            <Shield className="w-12 h-12 text-[#34D399]" />
          </div>
        </div>

        <h1 className="text-3xl sm:text-4xl font-black tracking-[0.15em] text-[#F5F5F5] uppercase">
          AEGIS
        </h1>

        <p className="text-[11px] font-bold text-[#10B981] tracking-[0.2em] uppercase mt-2">
          AI CYBERSECURITY & DIGITAL DECEPTION SHIELD
        </p>

        <p className="text-xs sm:text-sm text-white/60 mt-2 max-w-sm">
          Autonomous Scam Neutralization • Zero-Trust On-Device Heuristics
        </p>

        {/* Security Status Pill */}
        <div className="mt-5 inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-[#10B981]/10 border border-[#10B981]/30 text-[10px] font-bold tracking-wider text-[#34D399] uppercase">
          <span className="w-2 h-2 rounded-full bg-[#34D399] animate-pulse" />
          <span>ON-DEVICE ZERO-TRUST ENGINE READY</span>
        </div>

        {/* Error Banner */}
        {authError && (
          <div className="w-full mt-5 p-3.5 rounded-xl bg-[#EF4444]/15 border border-[#EF4444]/40 flex items-center gap-3 text-left text-xs text-[#EF4444]">
            <AlertTriangle className="w-4 h-4 shrink-0" />
            <span>{authError}</span>
          </div>
        )}

        {/* 2. CAPABILITY OVERVIEW CARDS */}
        <div className="w-full mt-8 bg-[#141414] border border-white/10 rounded-2xl p-5 text-left space-y-4">
          <h2 className="text-[11px] font-bold tracking-wider text-[#10B981] uppercase">
            ACTIVE DEFENSE CAPABILITIES
          </h2>

          <div className="flex items-start gap-3">
            <div className="w-8 h-8 rounded-lg bg-[#0A0A0A] border border-white/10 flex items-center justify-center shrink-0">
              <QrCode className="w-4 h-4 text-[#10B981]" />
            </div>
            <div>
              <p className="text-xs font-semibold text-[#F5F5F5]">
                Visual QR & Barcode Threat Analysis
              </p>
              <p className="text-[11px] text-white/50 leading-relaxed mt-0.5">
                Detects hidden quishing payloads and malicious URL redirects
              </p>
            </div>
          </div>

          <div className="flex items-start gap-3">
            <div className="w-8 h-8 rounded-lg bg-[#0A0A0A] border border-white/10 flex items-center justify-center shrink-0">
              <Lock className="w-4 h-4 text-[#10B981]" />
            </div>
            <div>
              <p className="text-xs font-semibold text-[#F5F5F5]">
                Homograph & Deceptive Domain Verification
              </p>
              <p className="text-[11px] text-white/50 leading-relaxed mt-0.5">
                Flags punycode, spoofed top-level domains, and brand impersonations
              </p>
            </div>
          </div>

          <div className="flex items-start gap-3">
            <div className="w-8 h-8 rounded-lg bg-[#0A0A0A] border border-white/10 flex items-center justify-center shrink-0">
              <Fingerprint className="w-4 h-4 text-[#10B981]" />
            </div>
            <div>
              <p className="text-xs font-semibold text-[#F5F5F5]">
                Zero-Knowledge Local Operation
              </p>
              <p className="text-[11px] text-white/50 leading-relaxed mt-0.5">
                Full local privacy: no account or personal data collection required
              </p>
            </div>
          </div>
        </div>

        {/* 3. GUEST ENTRY ACTION CARD */}
        <div className="w-full mt-6 space-y-3">
          <div
            onClick={!authLoading ? signInAsGuest : undefined}
            className="w-full bg-[#141414] border-2 border-[#10B981]/60 hover:border-[#10B981] rounded-2xl p-4 sm:p-5 text-left cursor-pointer transition-all duration-200 group flex items-center justify-between"
          >
            <div className="flex items-center gap-3.5">
              <div className="w-11 h-11 rounded-xl bg-[#10B981]/20 border border-[#10B981]/40 flex items-center justify-center text-[#34D399] shrink-0">
                {authLoading ? (
                  <Loader2 className="w-5 h-5 animate-spin" />
                ) : (
                  <Shield className="w-6 h-6" />
                )}
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <span className="text-sm sm:text-base font-bold text-[#34D399]">
                    Continue as Guest
                  </span>
                  <span className="text-[9px] font-black tracking-wider px-2 py-0.5 rounded bg-[#10B981]/20 text-[#34D399] uppercase">
                    INSTANT
                  </span>
                </div>
                <p className="text-[11px] text-white/50 mt-0.5">
                  Instant zero-credential access • Full on-device protection
                </p>
              </div>
            </div>
            <ArrowRight className="w-5 h-5 text-[#34D399] transition-transform group-hover:translate-x-1 shrink-0" />
          </div>

          <button
            onClick={signInAsGuest}
            disabled={authLoading}
            className="w-full h-12 rounded-xl bg-[#10B981] hover:bg-[#059669] text-[#0A0A0A] font-bold text-sm tracking-wider uppercase flex items-center justify-center gap-2 transition-all duration-200 shadow-[0_0_20px_rgba(16,185,129,0.3)] disabled:opacity-50"
          >
            {authLoading ? (
              <Loader2 className="w-5 h-5 animate-spin" />
            ) : (
              <span>Enter Aegis Scanner</span>
            )}
          </button>
        </div>

        {/* Footer Note */}
        <p className="text-[10px] text-white/30 tracking-wide mt-8">
          Protected by Aegis Autonomous AI Shield • Zero-Knowledge Telemetry
        </p>
      </div>
    </div>
  );
};
