import React, { useState } from 'react';
import {
  Home,
  QrCode,
  History,
  Settings,
  Shield,
  Menu,
  X,
  PlayCircle,
  Globe,
  Lock,
  CreditCard,
  ChevronRight
} from 'lucide-react';
import { ScreenType, useAegis } from '../context/AegisContext';

interface NavItem {
  screen: ScreenType;
  label: string;
  icon: React.ElementType;
}

const PRIMARY_TABS: NavItem[] = [
  { screen: 'home', label: 'Home', icon: Home },
  { screen: 'scanner', label: 'Scan', icon: QrCode },
  { screen: 'history', label: 'History', icon: History },
  { screen: 'settings', label: 'Settings', icon: Settings },
];

const ALL_NAV_ITEMS: { screen: ScreenType; label: string; desc: string; icon: React.ElementType }[] = [
  { screen: 'home', label: 'Home', desc: 'System overview & radar', icon: Home },
  { screen: 'scanner', label: 'QR & Text Scanner', desc: 'Live camera & direct input', icon: QrCode },
  { screen: 'demo_mode', label: 'Threat Simulator', desc: 'Synthetic deception scenarios', icon: PlayCircle },
  { screen: 'url_intelligence', label: 'URL Intelligence', desc: 'Deep domain & lookalike inspection', icon: Globe },
  { screen: 'fakepayment', label: 'Payment Check', desc: 'Receipt integrity analysis', icon: CreditCard },
  { screen: 'history', label: 'Scan History', desc: 'Past threat evaluations', icon: History },
  { screen: 'privacy_center', label: 'Privacy Center', desc: 'On-device zero-knowledge policy', icon: Lock },
  { screen: 'settings', label: 'Settings', desc: 'Security calibration & keys', icon: Settings },
];

export const Navigation: React.FC = () => {
  const { currentScreen, navigateTo } = useAegis();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  // Show navigation only on primary screens
  const showNav = ['home', 'scanner', 'history', 'settings'].includes(currentScreen);

  const handleSelectNav = (screen: ScreenType) => {
    setIsMobileMenuOpen(false);
    navigateTo(screen);
  };

  if (!showNav) return null;

  return (
    <>
      {/* DESKTOP HEADER NAVIGATION */}
      <header className="hidden md:flex fixed top-0 left-0 right-0 z-50 h-16 bg-[#0A0A0A]/90 backdrop-blur-md border-b border-white/10 px-6 lg:px-8 items-center justify-between">
        <div
          onClick={() => navigateTo('home')}
          className="flex items-center gap-3 cursor-pointer group select-none"
        >
          <div className="w-9 h-9 rounded-xl bg-[#10B981]/15 border border-[#10B981]/40 flex items-center justify-center transition-transform group-hover:scale-105">
            <Shield className="w-5 h-5 text-[#34D399]" />
          </div>
          <div>
            <span className="text-lg font-black tracking-widest text-[#F5F5F5]">AEGIS</span>
            <span className="block text-[9px] font-semibold text-[#34D399] tracking-wider uppercase -mt-1">
              AI Cyber Shield
            </span>
          </div>
        </div>

        <nav className="flex items-center gap-2">
          {PRIMARY_TABS.map(tab => {
            const Icon = tab.icon;
            const isSelected = currentScreen === tab.screen;
            return (
              <button
                key={tab.screen}
                onClick={() => navigateTo(tab.screen)}
                className={`flex items-center gap-2 px-4 py-2.5 rounded-xl text-xs font-bold tracking-wider uppercase transition-all duration-200 cursor-pointer ${
                  isSelected
                    ? 'bg-[#10B981]/20 text-[#34D399] border border-[#10B981]/40 shadow-[0_0_15px_rgba(16,185,129,0.2)]'
                    : 'text-white/50 hover:text-white/90 hover:bg-white/5'
                }`}
              >
                <Icon className={`w-4 h-4 ${isSelected ? 'text-[#34D399]' : 'text-white/40'}`} />
                <span>{tab.label}</span>
              </button>
            );
          })}
        </nav>

        <div className="flex items-center gap-2 px-3 py-1.5 rounded-full bg-[#121212] border border-[#34D399]/20 text-[10px] font-mono font-bold text-[#34D399]">
          <span className="w-2 h-2 rounded-full bg-[#34D399] animate-pulse" />
          <span>ON-DEVICE ZERO TRUST</span>
        </div>
      </header>

      {/* MOBILE TOP HEADER BAR */}
      <header className="md:hidden sticky top-0 left-0 right-0 z-40 bg-[#0A0A0A]/95 backdrop-blur-md border-b border-white/10 px-4 py-3 flex items-center justify-between">
        <div
          onClick={() => navigateTo('home')}
          className="flex items-center gap-2.5 cursor-pointer select-none"
        >
          <div className="w-8 h-8 rounded-lg bg-[#10B981]/15 border border-[#10B981]/40 flex items-center justify-center">
            <Shield className="w-4 h-4 text-[#34D399]" />
          </div>
          <div>
            <span className="text-base font-black tracking-widest text-[#F5F5F5]">AEGIS</span>
            <span className="block text-[8px] font-semibold text-[#34D399] tracking-wider uppercase -mt-0.5">
              AI Cyber Shield
            </span>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <div className="flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-[#121212] border border-[#34D399]/20 text-[9px] font-mono font-bold text-[#34D399]">
            <span className="w-1.5 h-1.5 rounded-full bg-[#34D399] animate-pulse" />
            <span>ACTIVE</span>
          </div>

          <button
            onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
            className="w-9 h-9 rounded-xl bg-white/5 border border-white/10 flex items-center justify-center text-white/80 active:scale-95 transition-transform"
            aria-label="Toggle navigation menu"
          >
            {isMobileMenuOpen ? <X className="w-5 h-5 text-[#34D399]" /> : <Menu className="w-5 h-5" />}
          </button>
        </div>
      </header>

      {/* MOBILE SLIDE-DOWN DRAWER MENU */}
      {isMobileMenuOpen && (
        <div className="md:hidden fixed inset-0 z-50 bg-black/80 backdrop-blur-md pt-16 flex flex-col justify-between p-4 animate-fadeIn">
          <div className="bg-[#121212] border border-white/10 rounded-2xl p-4 space-y-3 overflow-y-auto max-h-[75vh] shadow-2xl">
            <div className="flex items-center justify-between pb-2 border-b border-white/10">
              <span className="text-[10px] font-bold tracking-widest text-[#10B981] uppercase">
                AEGIS QUICK NAVIGATION
              </span>
              <button
                onClick={() => setIsMobileMenuOpen(false)}
                className="text-xs font-bold text-white/40 hover:text-white uppercase p-1"
              >
                Close
              </button>
            </div>

            <div className="space-y-1.5">
              {ALL_NAV_ITEMS.map((item) => {
                const Icon = item.icon;
                const isSelected = currentScreen === item.screen;
                return (
                  <button
                    key={item.screen}
                    onClick={() => handleSelectNav(item.screen)}
                    className={`w-full flex items-center justify-between p-3 rounded-xl transition-all text-left ${
                      isSelected
                        ? 'bg-[#10B981]/20 border border-[#10B981]/40 text-[#34D399]'
                        : 'bg-[#181818] border border-white/5 text-white/70 hover:text-white active:bg-white/10'
                    }`}
                  >
                    <div className="flex items-center gap-3">
                      <div
                        className={`w-8 h-8 rounded-lg flex items-center justify-center ${
                          isSelected ? 'bg-[#10B981]/30 text-[#34D399]' : 'bg-[#0A0A0A] text-white/50'
                        }`}
                      >
                        <Icon className="w-4 h-4" />
                      </div>
                      <div>
                        <p className={`text-xs font-bold ${isSelected ? 'text-[#34D399]' : 'text-[#F5F5F5]'}`}>
                          {item.label}
                        </p>
                        <p className="text-[10px] text-white/40">{item.desc}</p>
                      </div>
                    </div>
                    <ChevronRight className={`w-4 h-4 ${isSelected ? 'text-[#34D399]' : 'text-white/20'}`} />
                  </button>
                );
              })}
            </div>
          </div>

          <div className="p-2 text-center text-[10px] text-white/30 font-mono">
            Zero-Trust Local Engine • Tap anywhere outside to close
          </div>
        </div>
      )}

      {/* MOBILE FLOATING BOTTOM NAV BAR */}
      <div className="md:hidden fixed bottom-0 left-0 right-0 z-40 p-3 pb-[max(1.25rem,env(safe-area-inset-bottom))] pointer-events-none">
        <div className="max-w-md mx-auto pointer-events-auto">
          <nav className="bg-[#121212]/95 backdrop-blur-xl border border-white/10 rounded-[28px] p-1.5 shadow-[0_10px_30px_rgba(0,0,0,0.8)] flex items-center justify-around">
            {PRIMARY_TABS.map(tab => {
              const Icon = tab.icon;
              const isSelected = currentScreen === tab.screen;
              return (
                <button
                  key={tab.screen}
                  onClick={() => navigateTo(tab.screen)}
                  className={`min-h-[44px] min-w-[44px] flex items-center justify-center gap-1.5 px-3.5 py-2 rounded-[22px] transition-all duration-200 active:scale-95 ${
                    isSelected
                      ? 'bg-[#10B981]/20 text-[#F5F5F5] border border-[#34D399]/40'
                      : 'text-white/40 hover:text-white/70'
                  }`}
                >
                  <Icon className={`w-5 h-5 ${isSelected ? 'text-[#34D399]' : 'text-white/40'}`} />
                  {isSelected && (
                    <span className="text-[10px] font-bold tracking-wider uppercase text-[#F5F5F5]">
                      {tab.label}
                    </span>
                  )}
                </button>
              );
            })}
          </nav>
        </div>
      </div>
    </>
  );
};

