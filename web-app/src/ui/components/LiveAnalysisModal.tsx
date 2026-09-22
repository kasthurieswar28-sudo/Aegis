import React from 'react';
import { CheckCircle, AlertTriangle, Loader2 } from 'lucide-react';
import { useAegis } from '../context/AegisContext';
import { StageStatus } from '../../domain/models';

export const LiveAnalysisModal: React.FC = () => {
  const { isAnalyzing, liveStages } = useAegis();

  if (!isAnalyzing) return null;

  return (
    <div className="fixed inset-0 z-50 bg-black/85 backdrop-blur-md flex items-center justify-center p-4 sm:p-6 animate-fadeIn">
      <div className="w-full max-w-md bg-[#141414] border border-[#10B981]/40 rounded-2xl p-5 sm:p-6 shadow-[0_0_50px_rgba(0,0,0,0.9)]">
        {/* Modal Header */}
        <div className="flex items-center gap-3 mb-5">
          <div className="w-8 h-8 rounded-lg bg-[#10B981]/15 border border-[#10B981]/30 flex items-center justify-center">
            <Loader2 className="w-4 h-4 text-[#10B981] animate-spin" />
          </div>
          <div>
            <h3 className="text-xs font-bold tracking-widest text-[#10B981] uppercase">
              LIVE DECEPTION PIPELINE
            </h3>
            <p className="text-[10px] text-white/40">
              Multi-vector heuristic verification in progress
            </p>
          </div>
        </div>

        {/* 5-Stage Live Pipeline Steps */}
        <div className="space-y-3.5">
          {liveStages.map((stage, idx) => {
            return (
              <div key={idx} className="flex items-start gap-3">
                <div className="mt-0.5 shrink-0">
                  {stage.status === StageStatus.COMPLETED && (
                    <CheckCircle className="w-4 h-4 text-[#10B981]" />
                  )}
                  {stage.status === StageStatus.ACTIVE && (
                    <Loader2 className="w-4 h-4 text-[#34D399] animate-spin" />
                  )}
                  {stage.status === StageStatus.FAILED && (
                    <AlertTriangle className="w-4 h-4 text-[#EF4444]" />
                  )}
                  {stage.status === StageStatus.PENDING && (
                    <div className="w-2.5 h-2.5 rounded-full bg-white/20 mt-0.5 ml-0.5" />
                  )}
                </div>

                <div className="flex-1 min-w-0">
                  <p
                    className={`text-xs font-bold leading-none ${
                      stage.status === StageStatus.PENDING ? 'text-white/40' : 'text-[#F5F5F5]'
                    }`}
                  >
                    {stage.title}
                  </p>
                  <p
                    className={`text-[10px] mt-1 truncate ${
                      stage.status === StageStatus.ACTIVE ? 'text-[#34D399]' : 'text-white/40'
                    }`}
                  >
                    {stage.detail}
                  </p>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};
