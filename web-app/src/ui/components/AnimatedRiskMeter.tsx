import React, { useEffect, useState } from 'react';
import { RiskClassification } from '../../domain/models';
import { RiskClassificationBadge } from './Badges';

export const AnimatedRiskMeter: React.FC<{
  score: number;
  classification: RiskClassification;
  size?: number;
}> = ({ score, classification, size = 200 }) => {
  const [displayScore, setDisplayScore] = useState(0);

  useEffect(() => {
    let start = 0;
    const duration = 1000;
    const startTime = performance.now();

    const animate = (currentTime: number) => {
      const elapsed = currentTime - startTime;
      const progress = Math.min(elapsed / duration, 1);
      // easeOutQuart
      const easeProgress = 1 - Math.pow(1 - progress, 4);
      const currentVal = Math.round(start + (score - start) * easeProgress);
      setDisplayScore(currentVal);

      if (progress < 1) {
        requestAnimationFrame(animate);
      }
    };

    requestAnimationFrame(animate);
  }, [score]);

  const strokeWidth = 14;
  const radius = (size - strokeWidth * 2) / 2;
  const center = size / 2;

  // Arc calculation for 270 degrees (starts at 135 deg to 405 deg)
  const totalArcLength = 2 * Math.PI * radius * (270 / 360);
  const strokeDashoffset = totalArcLength - (totalArcLength * (displayScore / 100));

  const getColor = () => {
    switch (classification) {
      case RiskClassification.SAFE:
        return '#10B981';
      case RiskClassification.SUSPICIOUS:
        return '#F59E0B';
      case RiskClassification.HIGH_RISK:
        return '#EF4444';
      case RiskClassification.CRITICAL:
        return '#A855F7';
    }
  };

  return (
    <div className="relative flex flex-col items-center justify-center" style={{ width: size, height: size }}>
      <svg width={size} height={size} className="transform rotate-0">
        <defs>
          <linearGradient id="meterGradient" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" stopColor="#10B981" />
            <stop offset="50%" stopColor={getColor()} />
            <stop offset="100%" stopColor={getColor()} />
          </linearGradient>
        </defs>

        {/* Background Track Arc */}
        <circle
          cx={center}
          cy={center}
          r={radius}
          fill="none"
          stroke="#1F1F1F"
          strokeWidth={strokeWidth}
          strokeLinecap="round"
          strokeDasharray={`${totalArcLength} ${2 * Math.PI * radius}`}
          transform={`rotate(135 ${center} ${center})`}
        />

        {/* Value Arc */}
        <circle
          cx={center}
          cy={center}
          r={radius}
          fill="none"
          stroke="url(#meterGradient)"
          strokeWidth={strokeWidth}
          strokeLinecap="round"
          strokeDasharray={`${totalArcLength} ${2 * Math.PI * radius}`}
          strokeDashoffset={strokeDashoffset}
          transform={`rotate(135 ${center} ${center})`}
          style={{ transition: 'stroke-dashoffset 0.1s ease-out' }}
        />
      </svg>

      {/* Central Score Display */}
      <div className="absolute inset-0 flex flex-col items-center justify-center text-center">
        <span className="text-4xl sm:text-5xl font-black text-[#F5F5F5] tracking-tight leading-none">
          {displayScore}
        </span>
        <span className="text-[12px] font-semibold text-white/40 tracking-wider uppercase mt-1">
          / 100
        </span>
        <div className="mt-2">
          <RiskClassificationBadge classification={classification} />
        </div>
      </div>
    </div>
  );
};
