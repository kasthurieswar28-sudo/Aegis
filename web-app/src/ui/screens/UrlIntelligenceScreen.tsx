import React, { useState } from 'react';
import { ArrowLeft, Globe, Search, Shield, CheckCircle2, AlertTriangle, Info } from 'lucide-react';
import { useAegis } from '../context/AegisContext';
import { analyzeUrl, URLAnalysisResult } from '../../security/URLAnalyzer';
import { ScanType } from '../../domain/models';

export const UrlIntelligenceScreen: React.FC = () => {
  const { navigateTo, goBack, runSyntheticScenario } = useAegis();
  const [urlInput, setUrlInput] = useState('https://sbi-kyc-update-portal.top/login');
  const [analysisResult, setAnalysisResult] = useState<URLAnalysisResult | null>(() =>
    analyzeUrl('https://sbi-kyc-update-portal.top/login')
  );

  const handleInspect = (val: string) => {
    setUrlInput(val);
    if (val.trim()) {
      setAnalysisResult(analyzeUrl(val.trim()));
    } else {
      setAnalysisResult(null);
    }
  };

  const handleRunPipeline = () => {
    if (!urlInput.trim()) return;
    runSyntheticScenario('URL Intelligence Scan', urlInput.trim(), ScanType.URL_DESTINATION, urlInput.trim());
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
            DEEP URL INTELLIGENCE
          </h1>
          <p className="text-[11px] text-[#34D399]">
            Structural Domain & Lookalike Inspection Engine
          </p>
        </div>
      </div>

      {/* Input Card */}
      <div className="bg-[#141414] border border-white/10 rounded-2xl p-4 sm:p-5 space-y-3">
        <span className="text-[11px] font-bold tracking-wider text-[#10B981] uppercase block">
          INPUT TARGET DESTINATION
        </span>

        <div className="relative flex items-center">
          <input
            type="text"
            value={urlInput}
            onChange={(e) => handleInspect(e.target.value)}
            placeholder="Paste URL or domain (e.g. sbi.co.in)"
            className="w-full bg-[#0A0A0A] border border-white/10 focus:border-[#10B981] rounded-xl px-4 py-3 text-xs text-[#F5F5F5] placeholder:text-white/30 focus:outline-none pr-10 font-mono"
          />
          <button
            onClick={() => handleInspect(urlInput)}
            className="absolute right-3 text-[#10B981] hover:text-[#34D399]"
          >
            <Search className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Inspection Details */}
      {analysisResult && (
        <div className="space-y-4">
          <div
            className={`bg-[#141414] border rounded-2xl p-5 space-y-4 ${
              analysisResult.isSuspiciousDomain ? 'border-[#EF4444]/40' : 'border-[#10B981]/40'
            }`}
          >
            <div className="flex items-center gap-3">
              {analysisResult.isSuspiciousDomain ? (
                <AlertTriangle className="w-5 h-5 text-[#EF4444] shrink-0" />
              ) : (
                <CheckCircle2 className="w-5 h-5 text-[#34D399] shrink-0" />
              )}
              <div>
                <span
                  className={`text-xs font-bold uppercase tracking-wider block ${
                    analysisResult.isSuspiciousDomain ? 'text-[#EF4444]' : 'text-[#34D399]'
                  }`}
                >
                  {analysisResult.isSuspiciousDomain ? 'HIGH-RISK DESTINATION' : 'STANDARD STRUCTURE'}
                </span>
                <span className="text-xs font-mono text-[#F5F5F5]">
                  {analysisResult.urlIntelligence.hostname}
                </span>
              </div>
            </div>

            {/* Attributes Table */}
            <div className="space-y-2 border-t border-white/5 pt-3 text-xs">
              <div className="flex justify-between py-1 border-b border-white/5">
                <span className="text-white/50">Protocol / Scheme</span>
                <span className="font-mono font-semibold text-[#F5F5F5]">
                  {analysisResult.urlIntelligence.isPaymentUri ? 'upi://' : 'https://'}
                </span>
              </div>

              <div className="flex justify-between py-1 border-b border-white/5">
                <span className="text-white/50">Domain Extension (TLD)</span>
                <span className="font-mono font-semibold text-[#F5F5F5]">
                  .{analysisResult.urlIntelligence.tld}
                  {analysisResult.urlIntelligence.isSuspiciousTld ? ' (High Risk)' : ' (Standard)'}
                </span>
              </div>

              <div className="flex justify-between py-1 border-b border-white/5">
                <span className="text-white/50">Lookalike Impersonation</span>
                <span className="font-semibold text-[#F5F5F5]">
                  {analysisResult.urlIntelligence.lookalikeBrand
                    ? `Mimicking ${analysisResult.urlIntelligence.lookalikeBrand}`
                    : 'None detected'}
                </span>
              </div>

              <div className="flex justify-between py-1 border-b border-white/5">
                <span className="text-white/50">Punycode / Homoglyph</span>
                <span className="font-mono font-semibold text-[#F5F5F5]">
                  {analysisResult.urlIntelligence.isPunycode ? 'Active (xn-- present)' : 'None'}
                </span>
              </div>

              <div className="flex justify-between py-1 border-b border-white/5">
                <span className="text-white/50">Subdomain Depth</span>
                <span className="font-mono font-semibold text-[#F5F5F5]">
                  {analysisResult.urlIntelligence.subdomains.length} segments
                </span>
              </div>

              <div className="flex justify-between py-1">
                <span className="text-white/50">Raw IP Host</span>
                <span className="font-mono font-semibold text-[#F5F5F5]">
                  {analysisResult.urlIntelligence.isIpAddress ? 'Yes (Obfuscated)' : 'No'}
                </span>
              </div>
            </div>

            {/* Analytical Findings */}
            {analysisResult.findings.length > 0 && (
              <div className="space-y-2 border-t border-white/5 pt-3">
                <span className="text-[10px] font-bold text-white/40 tracking-wider uppercase block">
                  ANALYTICAL FINDINGS
                </span>
                <div className="space-y-1.5">
                  {analysisResult.findings.map((f, i) => (
                    <div key={i} className="flex items-start gap-2 text-xs text-[#F5F5F5]">
                      <span
                        className={`w-1.5 h-1.5 rounded-full mt-1.5 shrink-0 ${
                          analysisResult.isSuspiciousDomain ? 'bg-[#EF4444]' : 'bg-[#10B981]'
                        }`}
                      />
                      <span>{f}</span>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>

          {/* Explicitly noted offline unavailable data */}
          <div className="bg-[#141414]/60 border border-white/5 rounded-2xl p-4 space-y-2">
            <div className="flex items-center gap-2 text-white/40">
              <Info className="w-4 h-4" />
              <span className="text-[10px] font-bold tracking-wider uppercase">
                UNAVAILABLE OFFLINE DATA EXPLICITLY NOTED
              </span>
            </div>
            <div className="space-y-1 text-[11px] text-white/50">
              {analysisResult.urlIntelligence.unavailableDataNotes.map((note, idx) => (
                <p key={idx}>• {note}</p>
              ))}
            </div>
          </div>

          {/* Pipeline Button */}
          <button
            onClick={handleRunPipeline}
            className="w-full py-3.5 bg-[#10B981] hover:bg-[#059669] text-[#0A0A0A] font-bold text-xs tracking-wider uppercase rounded-xl flex items-center justify-center gap-2 transition-all shadow-[0_0_20px_rgba(16,185,129,0.2)]"
          >
            <Shield className="w-4 h-4" />
            <span>ANALYZE IN AEGIS PIPELINE</span>
          </button>
        </div>
      )}
    </div>
  );
};
