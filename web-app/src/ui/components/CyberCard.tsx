import React from 'react';

export const CyberCard: React.FC<{
  children: React.ReactNode;
  className?: string;
  borderColor?: string;
  onClick?: () => void;
}> = ({
  children,
  className = '',
  borderColor = 'border-white/10',
  onClick
}) => {
  return (
    <div
      onClick={onClick}
      className={`w-full bg-[#141414] border ${borderColor} rounded-2xl p-4 sm:p-5 transition-all duration-200 ${
        onClick ? 'cursor-pointer hover:border-[#10B981]/40 hover:bg-[#181818]' : ''
      } ${className}`}
    >
      {children}
    </div>
  );
};
