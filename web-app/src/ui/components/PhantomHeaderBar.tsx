import React from 'react';
import { ArrowLeft } from 'lucide-react';

export const PhantomHeaderBar: React.FC<{
  subtitle?: string;
  className?: string;
  onBack?: () => void;
}> = ({ subtitle = "Digital Deception Protection", className = '', onBack }) => {
  return (
    <div className={`w-full flex items-center justify-between py-2.5 mb-2 border-b border-white/10 ${className}`}>
      <div className="flex items-center gap-3">
        {onBack && (
          <button
            onClick={onBack}
            className="min-h-[44px] min-w-[44px] rounded-xl bg-white/5 hover:bg-white/10 active:scale-95 border border-white/10 flex items-center justify-center text-white/80 transition-all cursor-pointer"
            aria-label="Go back"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
        )}
        <div>
          <h1 className="text-lg sm:text-xl font-bold tracking-wider text-[#F5F5F5] uppercase">
            AEGIS
          </h1>
          <p className="text-[10px] font-semibold tracking-[0.2em] text-[#34D399] uppercase -mt-0.5">
            {subtitle}
          </p>
        </div>
      </div>

      {/* Elegant circular status badge with glowing emerald dot */}
      <div className="flex items-center gap-1.5 px-3 py-1 rounded-full bg-[#121212] border border-[#34D399]/20 text-[9px] font-mono font-bold text-[#34D399] tracking-wider uppercase">
        <span className="w-1.5 h-1.5 rounded-full bg-[#34D399] animate-pulse" />
        <span>ARMED</span>
      </div>
    </div>
  );
};
