import React from 'react';
import { QrCode } from 'lucide-react';

export const RadarHeroControl: React.FC<{
  onClick: () => void;
}> = ({ onClick }) => {
  return (
    <div
      onClick={onClick}
      className="relative flex items-center justify-center w-64 h-64 sm:w-72 sm:h-72 cursor-pointer group select-none transition-transform duration-200 active:scale-95"
      role="button"
      tabIndex={0}
      aria-label="Scan and Trust button"
    >
      {/* Layer 1: Soft breathing emerald radial ambient glow */}
      <div className="absolute inset-0 rounded-full bg-radial from-[#10B981]/20 via-[#10B981]/5 to-transparent animate-halo pointer-events-none" />

      {/* Layer 2: Precision Radar Guides and SVG Ring Canvas */}
      <svg className="absolute inset-0 w-full h-full pointer-events-none" viewBox="0 0 288 288">
        <defs>
          <linearGradient id="radarSweepGrad" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" stopColor="#34D399" stopOpacity="0.9" />
            <stop offset="100%" stopColor="#10B981" stopOpacity="0.1" />
          </linearGradient>
        </defs>

        {/* Outer guideline ring */}
        <circle cx="144" cy="144" r="132" fill="none" stroke="rgba(255,255,255,0.08)" strokeWidth="1" />

        {/* Middle segmented guideline ring */}
        <circle cx="144" cy="144" r="116" fill="none" stroke="rgba(52, 211, 153, 0.15)" strokeWidth="1" strokeDasharray="4 6" />

        {/* Precision crosshair tick marks at 0, 90, 180, 270 deg */}
        <line x1="144" y1="12" x2="144" y2="24" stroke="#34D399" strokeWidth="1.5" opacity="0.6" />
        <line x1="144" y1="264" x2="144" y2="276" stroke="#34D399" strokeWidth="1.5" opacity="0.6" />
        <line x1="12" y1="144" x2="24" y2="144" stroke="#34D399" strokeWidth="1.5" opacity="0.6" />
        <line x1="264" y1="144" x2="276" y2="144" stroke="#34D399" strokeWidth="1.5" opacity="0.6" />

        {/* Rotating animated scanning ring */}
        <circle
          cx="144"
          cy="144"
          r="132"
          fill="none"
          stroke="url(#radarSweepGrad)"
          strokeWidth="2.5"
          strokeLinecap="round"
          strokeDasharray="140 700"
          className="animate-radar origin-center"
        />

        {/* Counter-rotating subtle micro-arc */}
        <circle
          cx="144"
          cy="144"
          r="116"
          fill="none"
          stroke="rgba(255,255,255,0.3)"
          strokeWidth="1.5"
          strokeLinecap="round"
          strokeDasharray="60 600"
          className="animate-radar-reverse origin-center"
        />
      </svg>

      {/* Layer 3: Deep Metallic Dark Graphite Central Disc */}
      <div className="relative z-10 w-44 h-44 sm:w-48 sm:h-48 rounded-full bg-gradient-to-b from-[#1C1C1C] via-[#141414] to-[#0D0D0D] border border-[#34D399]/25 shadow-2xl flex flex-col items-center justify-center p-4 transition-all duration-300 group-hover:border-[#34D399]/60 group-hover:shadow-[0_0_35px_rgba(16,185,129,0.25)]">
        {/* Central Scanner Glyph Container with soft glow */}
        <div className="w-12 h-12 rounded-xl bg-[#181818] border border-[#34D399]/30 flex items-center justify-center mb-2.5 transition-transform duration-300 group-hover:scale-110">
          <QrCode className="w-6 h-6 text-[#34D399]" />
        </div>

        <span className="text-[13px] sm:text-[14px] font-bold text-[#F5F5F5] tracking-wider uppercase">
          SCAN & TRUST
        </span>

        <span className="text-[9px] sm:text-[10px] font-medium text-white/40 tracking-widest uppercase mt-0.5">
          QR • TEXT • IMAGE
        </span>
      </div>
    </div>
  );
};
