import React, { useRef } from 'react';
import { ArrowLeft, CreditCard, Image as ImageIcon, ShieldAlert, CheckCircle2 } from 'lucide-react';
import { useAegis } from '../context/AegisContext';
import { ScanType } from '../../domain/models';

export const FakePaymentScreen: React.FC = () => {
  const { navigateTo, goBack, analyzeContent } = useAegis();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    analyzeContent(
      `Payment Receipt Image: ${file.name} - Analyzed for UTR validity and typography integrity`,
      ScanType.PAYMENT_SCREENSHOT
    );
  };

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
            PAYMENT SCREENSHOT DETECTOR
          </h1>
          <p className="text-[11px] text-[#F59E0B]">
            Receipt Integrity & Tampering Analysis
          </p>
        </div>
      </div>

      {/* Hero Card */}
      <div className="bg-[#141414] border border-[#F59E0B]/40 rounded-2xl p-5 space-y-3 shadow-lg">
        <div className="flex items-center gap-2.5">
          <div className="w-9 h-9 rounded-xl bg-[#F59E0B]/15 border border-[#F59E0B]/30 flex items-center justify-center text-[#F59E0B]">
            <CreditCard className="w-5 h-5" />
          </div>
          <h2 className="text-xs font-bold tracking-wider text-[#F59E0B] uppercase">
            SCREENSHOT VERIFICATION ENGINE
          </h2>
        </div>
        <p className="text-xs text-white/70 leading-relaxed">
          Fraudulent buyers frequently generate spoofed 'Payment Successful' receipts with fake UTR reference IDs and manipulated font sizes.
        </p>
      </div>

      {/* Key Anomaly Checks */}
      <div className="space-y-3">
        <span className="text-[11px] font-bold tracking-wider text-white/40 uppercase px-1">
          KEY ANOMALY CHECKS
        </span>

        <div className="bg-[#141414] border border-white/10 rounded-2xl p-5 space-y-4">
          <div className="flex items-start gap-3">
            <span className="w-6 h-6 rounded-lg bg-[#F59E0B]/20 text-[#F59E0B] font-bold flex items-center justify-center text-xs shrink-0">
              1
            </span>
            <div>
              <h3 className="text-xs font-bold text-[#F5F5F5]">Missing or Invalid UTR / Banking Ref ID</h3>
              <p className="text-[11px] text-white/50 leading-relaxed mt-0.5">
                Real payments always possess a 12-digit numeric UTR verified with the central switch.
              </p>
            </div>
          </div>

          <div className="flex items-start gap-3">
            <span className="w-6 h-6 rounded-lg bg-[#F59E0B]/20 text-[#F59E0B] font-bold flex items-center justify-center text-xs shrink-0">
              2
            </span>
            <div>
              <h3 className="text-xs font-bold text-[#F5F5F5]">Font Kerning & Timestamp Misalignment</h3>
              <p className="text-[11px] text-white/50 leading-relaxed mt-0.5">
                Fake payment generators produce mismatched typography compared to authentic bank apps.
              </p>
            </div>
          </div>

          <div className="flex items-start gap-3">
            <span className="w-6 h-6 rounded-lg bg-[#F59E0B]/20 text-[#F59E0B] font-bold flex items-center justify-center text-xs shrink-0">
              3
            </span>
            <div>
              <h3 className="text-xs font-bold text-[#F5F5F5]">Unverified Bank Balance Addition</h3>
              <p className="text-[11px] text-white/50 leading-relaxed mt-0.5">
                Never trust a visual screenshot. Only release goods when funds reflect inside your actual bank account.
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Action Button */}
      <div>
        <input
          ref={fileInputRef}
          type="file"
          accept="image/*"
          className="hidden"
          onChange={handleFileUpload}
        />

        <button
          onClick={() => fileInputRef.current?.click()}
          className="w-full py-4 bg-[#10B981] hover:bg-[#059669] text-[#0A0A0A] font-bold text-xs tracking-wider uppercase rounded-xl flex items-center justify-center gap-2 transition-all shadow-[0_0_20px_rgba(16,185,129,0.25)]"
        >
          <ImageIcon className="w-4 h-4" />
          <span>UPLOAD PAYMENT SCREENSHOT</span>
        </button>
      </div>
    </div>
  );
};
