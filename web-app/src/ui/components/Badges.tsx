import React from 'react';
import { RiskClassification, RiskClassificationDetails } from '../../domain/models';

export const RiskClassificationBadge: React.FC<{
  classification: RiskClassification;
  className?: string;
}> = ({ classification, className = '' }) => {
  const details = RiskClassificationDetails[classification];

  const getColors = () => {
    switch (classification) {
      case RiskClassification.SAFE:
        return 'bg-[#10B981]/15 text-[#10B981] border-[#10B981]/50';
      case RiskClassification.SUSPICIOUS:
        return 'bg-[#F59E0B]/15 text-[#F59E0B] border-[#F59E0B]/50';
      case RiskClassification.HIGH_RISK:
        return 'bg-[#EF4444]/15 text-[#EF4444] border-[#EF4444]/50';
      case RiskClassification.CRITICAL:
        return 'bg-[#A855F7]/20 text-[#A855F7] border-[#A855F7]/60';
    }
  };

  const getDotColor = () => {
    switch (classification) {
      case RiskClassification.SAFE:
        return 'bg-[#10B981]';
      case RiskClassification.SUSPICIOUS:
        return 'bg-[#F59E0B]';
      case RiskClassification.HIGH_RISK:
        return 'bg-[#EF4444]';
      case RiskClassification.CRITICAL:
        return 'bg-[#A855F7]';
    }
  };

  return (
    <div
      className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md border text-[11px] font-bold tracking-wider ${getColors()} ${className}`}
    >
      <span className={`w-1.5 h-1.5 rounded-full ${getDotColor()}`} />
      <span>{details.label}</span>
    </div>
  );
};
