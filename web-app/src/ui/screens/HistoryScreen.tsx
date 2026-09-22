import React, { useState } from 'react';
import { History as HistoryIcon, Trash2, Trash, Shield, AlertTriangle, CheckCircle2 } from 'lucide-react';
import { useAegis } from '../context/AegisContext';
import { RiskClassification, ScanResult } from '../../domain/models';
import { RiskClassificationBadge } from '../components/Badges';
import { PhantomHeaderBar } from '../components/PhantomHeaderBar';

export const HistoryScreen: React.FC = () => {
  const { allHistory, selectHistoryItem, deleteScan, clearAllHistory } = useAegis();
  const [selectedFilter, setSelectedFilter] = useState<RiskClassification | null>(null);
  const [showClearConfirm, setShowClearConfirm] = useState(false);

  const filtered = selectedFilter
    ? allHistory.filter(s => s.classification === selectedFilter)
    : allHistory;

  return (
    <div className="w-full max-w-2xl mx-auto px-4 sm:px-6 pt-4 pb-28 md:pt-20 space-y-4">
      <PhantomHeaderBar subtitle="Threat Analysis History" />

      {/* Filter and Clear Bar */}
      <div className="flex items-center justify-between gap-2 py-1">
        <div className="flex items-center gap-2 overflow-x-auto pb-1">
          <button
            onClick={() => setSelectedFilter(null)}
            className={`px-3 py-1.5 rounded-xl text-xs font-semibold tracking-wider transition-all ${
              selectedFilter === null
                ? 'bg-[#10B981]/20 text-[#34D399] border border-[#10B981]/40'
                : 'bg-[#141414] text-white/50 border border-white/5 hover:text-white/80'
            }`}
          >
            All ({allHistory.length})
          </button>

          <button
            onClick={() => setSelectedFilter(selectedFilter === RiskClassification.HIGH_RISK ? null : RiskClassification.HIGH_RISK)}
            className={`px-3 py-1.5 rounded-xl text-xs font-semibold tracking-wider transition-all ${
              selectedFilter === RiskClassification.HIGH_RISK
                ? 'bg-[#EF4444]/20 text-[#EF4444] border border-[#EF4444]/40'
                : 'bg-[#141414] text-white/50 border border-white/5 hover:text-white/80'
            }`}
          >
            Threats
          </button>

          <button
            onClick={() => setSelectedFilter(selectedFilter === RiskClassification.SAFE ? null : RiskClassification.SAFE)}
            className={`px-3 py-1.5 rounded-xl text-xs font-semibold tracking-wider transition-all ${
              selectedFilter === RiskClassification.SAFE
                ? 'bg-[#10B981]/20 text-[#34D399] border border-[#10B981]/40'
                : 'bg-[#141414] text-white/50 border border-white/5 hover:text-white/80'
            }`}
          >
            Safe
          </button>
        </div>

        {allHistory.length > 0 && (
          <button
            onClick={() => setShowClearConfirm(true)}
            className="p-2 rounded-xl bg-white/5 hover:bg-[#EF4444]/15 hover:text-[#EF4444] text-white/40 transition-colors"
            title="Clear all history"
          >
            <Trash2 className="w-4 h-4" />
          </button>
        )}
      </div>

      {/* Clear All Confirmation Dialog */}
      {showClearConfirm && (
        <div className="fixed inset-0 z-50 bg-black/80 flex items-center justify-center p-4">
          <div className="bg-[#141414] border border-white/10 rounded-2xl p-5 max-w-sm w-full space-y-4 shadow-2xl">
            <h3 className="text-sm font-bold text-[#F5F5F5]">Clear Scan History</h3>
            <p className="text-xs text-white/60">
              Permanently delete all stored scan history and threat records from your local storage?
            </p>
            <div className="flex items-center justify-end gap-2 pt-2">
              <button
                onClick={() => setShowClearConfirm(false)}
                className="px-4 py-2 rounded-xl text-xs font-semibold text-white/60 hover:text-white"
              >
                CANCEL
              </button>
              <button
                onClick={() => {
                  clearAllHistory();
                  setShowClearConfirm(false);
                }}
                className="px-4 py-2 rounded-xl bg-[#EF4444] text-white font-bold text-xs"
              >
                CLEAR ALL
              </button>
            </div>
          </div>
        </div>
      )}

      {/* History Items List */}
      {filtered.length === 0 ? (
        <div className="min-h-[40vh] flex flex-col items-center justify-center text-center p-8">
          <HistoryIcon className="w-12 h-12 text-white/20 mb-3" />
          <h3 className="text-sm font-bold text-[#F5F5F5] uppercase tracking-wider">
            NO SCANS IN HISTORY
          </h3>
          <p className="text-xs text-white/40 mt-1 max-w-xs">
            Scanned QR codes and verified messages will be preserved here.
          </p>
        </div>
      ) : (
        <div className="space-y-3">
          {filtered.map((scan) => {
            const scoreColor =
              scan.classification === RiskClassification.SAFE
                ? 'text-[#10B981] bg-[#10B981]/15 border-[#10B981]/40'
                : scan.classification === RiskClassification.SUSPICIOUS
                ? 'text-[#F59E0B] bg-[#F59E0B]/15 border-[#F59E0B]/40'
                : 'text-[#EF4444] bg-[#EF4444]/15 border-[#EF4444]/40';

            const dateStr = new Date(scan.timestamp).toLocaleDateString(undefined, {
              month: 'short',
              day: 'numeric',
              year: 'numeric',
              hour: '2-digit',
              minute: '2-digit'
            });

            return (
              <div
                key={scan.id}
                className="bg-[#141414] hover:bg-[#181818] border border-white/10 hover:border-[#10B981]/40 rounded-2xl p-4 transition-all duration-200 cursor-pointer group space-y-3"
                onClick={() => selectHistoryItem(scan)}
              >
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div className={`w-11 h-11 rounded-xl flex items-center justify-center font-black text-base border ${scoreColor}`}>
                      {scan.riskScore}
                    </div>

                    <div>
                      <div className="flex items-center gap-2">
                        <span className="text-xs font-bold text-[#F5F5F5] group-hover:text-[#34D399] transition-colors">
                          {scan.threatCategory.replace(/_/g, ' ')}
                        </span>
                        <RiskClassificationBadge classification={scan.classification} />
                      </div>
                      <p className="text-[10px] text-white/40 mt-0.5">
                        {dateStr}
                      </p>
                    </div>
                  </div>

                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      deleteScan(scan.id);
                    }}
                    className="p-1.5 rounded-lg text-white/30 hover:text-[#EF4444] hover:bg-white/5 transition-colors"
                  >
                    <Trash className="w-4 h-4" />
                  </button>
                </div>

                <p className="text-xs text-white/70 line-clamp-2 leading-relaxed">
                  {scan.rawContent}
                </p>

                {/* Signals Badges */}
                {scan.signals.signalDetails.length > 0 && (
                  <div className="flex flex-wrap gap-1.5 pt-1">
                    {scan.signals.signalDetails.slice(0, 3).map((sig, i) => (
                      <span
                        key={i}
                        className="text-[9px] font-mono px-2 py-0.5 rounded-md bg-[#0A0A0A] border border-white/5 text-[#34D399]/80 truncate max-w-[200px]"
                      >
                        {sig}
                      </span>
                    ))}
                    {scan.signals.signalDetails.length > 3 && (
                      <span className="text-[9px] text-white/40 self-center">
                        +{scan.signals.signalDetails.length - 3} more
                      </span>
                    )}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
