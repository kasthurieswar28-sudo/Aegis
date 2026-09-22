import React from 'react';
import { ArrowLeft, Lock, Cpu, Cloud, Trash2, ShieldCheck } from 'lucide-react';
import { useAegis } from '../context/AegisContext';

export const PrivacyCenterScreen: React.FC = () => {
  const { navigateTo, goBack } = useAegis();

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
            PRIVACY & DATA POLICY
          </h1>
          <p className="text-[11px] text-[#34D399]">
            Zero-Knowledge On-Device Security Architecture
          </p>
        </div>
      </div>

      {/* Core Philosophy Card */}
      <div className="bg-[#10B981]/10 border border-[#10B981]/30 rounded-2xl p-5 space-y-2">
        <div className="flex items-center gap-2">
          <Lock className="w-4 h-4 text-[#34D399]" />
          <h2 className="text-xs font-bold tracking-wider text-[#34D399] uppercase">
            CORE PRIVACY PRINCIPLE
          </h2>
        </div>
        <p className="text-xs text-[#F5F5F5] leading-relaxed">
          Aegis does not monitor your private communications in the background. It analyzes only the specific content or QR codes you explicitly submit. All baseline threat heuristic evaluations occur directly on your device.
        </p>
      </div>

      {/* 1. ON-DEVICE PROCESSING */}
      <div className="bg-[#141414] border border-white/10 rounded-2xl p-5 space-y-3">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-[#10B981]/15 border border-[#10B981]/30 flex items-center justify-center text-[#34D399]">
            <Cpu className="w-5 h-5" />
          </div>
          <div>
            <h3 className="text-xs font-bold text-[#F5F5F5] uppercase tracking-wider">
              WHAT RUNS 100% ON-DEVICE
            </h3>
            <p className="text-[11px] text-white/40">Local Hardware Sandbox</p>
          </div>
        </div>

        <div className="space-y-2.5 pt-2 text-xs text-white/70">
          <div className="flex items-start gap-2.5">
            <span className="w-1.5 h-1.5 rounded-full bg-[#34D399] mt-1.5 shrink-0" />
            <p><strong className="text-[#F5F5F5]">Client-Side Barcode & OCR:</strong> Visual optical characters and barcodes are decoded directly in browser memory without sending frames over the network.</p>
          </div>
          <div className="flex items-start gap-2.5">
            <span className="w-1.5 h-1.5 rounded-full bg-[#34D399] mt-1.5 shrink-0" />
            <p><strong className="text-[#F5F5F5]">Heuristic Signal Extractor:</strong> Regex patterns, token analysis, and psychological coercion metrics execute strictly in-memory.</p>
          </div>
          <div className="flex items-start gap-2.5">
            <span className="w-1.5 h-1.5 rounded-full bg-[#34D399] mt-1.5 shrink-0" />
            <p><strong className="text-[#F5F5F5]">URL Structural Engine:</strong> Hostname parsing, Punycode inspection, and brand lookalike detection run completely offline.</p>
          </div>
          <div className="flex items-start gap-2.5">
            <span className="w-1.5 h-1.5 rounded-full bg-[#34D399] mt-1.5 shrink-0" />
            <p><strong className="text-[#F5F5F5]">Local Encrypted Storage:</strong> Scan history is stored strictly in your browser's private local storage space.</p>
          </div>
        </div>
      </div>

      {/* 2. CLOUD AI & REDACTION */}
      <div className="bg-[#141414] border border-white/10 rounded-2xl p-5 space-y-3">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-[#38BDF8]/15 border border-[#38BDF8]/30 flex items-center justify-center text-[#38BDF8]">
            <Cloud className="w-5 h-5" />
          </div>
          <div>
            <h3 className="text-xs font-bold text-[#F5F5F5] uppercase tracking-wider">
              WHAT IS SENT TO CLOUD AI
            </h3>
            <p className="text-[11px] text-white/40">Optional Gemini Reasoning</p>
          </div>
        </div>

        <div className="space-y-2.5 pt-2 text-xs text-white/70">
          <div className="flex items-start gap-2.5">
            <span className="w-1.5 h-1.5 rounded-full bg-[#38BDF8] mt-1.5 shrink-0" />
            <p>When cloud reasoning is enabled, only high-level threat indicators and sanitized text prompts are sent to Google Gemini.</p>
          </div>
          <div className="flex items-start gap-2.5">
            <span className="w-1.5 h-1.5 rounded-full bg-[#38BDF8] mt-1.5 shrink-0" />
            <p>Authentication code scrubbers redact sensitive numbers (e.g. OTP codes or payment details) before prompt assembly.</p>
          </div>
          <div className="flex items-start gap-2.5">
            <span className="w-1.5 h-1.5 rounded-full bg-[#38BDF8] mt-1.5 shrink-0" />
            <p>If offline or no API key is provided, the on-device rule engine automatically provides full reasoning and answers.</p>
          </div>
        </div>
      </div>

      {/* 3. DATA RETENTION & CONTROL */}
      <div className="bg-[#141414] border border-white/10 rounded-2xl p-5 space-y-3">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-[#F59E0B]/15 border border-[#F59E0B]/30 flex items-center justify-center text-[#F59E0B]">
            <Trash2 className="w-5 h-5" />
          </div>
          <div>
            <h3 className="text-xs font-bold text-[#F5F5F5] uppercase tracking-wider">
              DATA RETENTION & CONTROL
            </h3>
            <p className="text-[11px] text-white/40">Complete User Autonomy</p>
          </div>
        </div>

        <div className="space-y-2.5 pt-2 text-xs text-white/70">
          <div className="flex items-start gap-2.5">
            <span className="w-1.5 h-1.5 rounded-full bg-[#F59E0B] mt-1.5 shrink-0" />
            <p>You retain total control over your security logs. Raw credentials or passwords are never preserved.</p>
          </div>
          <div className="flex items-start gap-2.5">
            <span className="w-1.5 h-1.5 rounded-full bg-[#F59E0B] mt-1.5 shrink-0" />
            <p>You can delete individual scans or clear all stored records with one click in the History screen.</p>
          </div>
        </div>
      </div>

      {/* 4. PERMISSIONS */}
      <div className="bg-[#141414] border border-white/10 rounded-2xl p-5 space-y-3">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-[#10B981]/15 border border-[#10B981]/30 flex items-center justify-center text-[#34D399]">
            <ShieldCheck className="w-5 h-5" />
          </div>
          <div>
            <h3 className="text-xs font-bold text-[#F5F5F5] uppercase tracking-wider">
              PERMISSIONS EXPLAINED
            </h3>
            <p className="text-[11px] text-white/40">Principle of Least Privilege</p>
          </div>
        </div>

        <div className="space-y-2.5 pt-2 text-xs text-white/70">
          <p><strong className="text-[#F5F5F5]">CAMERA:</strong> Used solely while on the Camera Scanner tab to read QR codes in real-time.</p>
          <p><strong className="text-[#F5F5F5]">INTERNET:</strong> Optional network access to connect to Gemini API for deep cognitive reasoning.</p>
          <p><strong className="text-[#F5F5F5]">NO AUDIO / MICROPHONE:</strong> Zero audio permissions or background listening.</p>
        </div>
      </div>
    </div>
  );
};
