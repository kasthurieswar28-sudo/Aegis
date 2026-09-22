import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { AegisProvider, useAegis } from './ui/context/AegisContext';
import { Navigation } from './ui/components/Navigation';
import { LiveAnalysisModal } from './ui/components/LiveAnalysisModal';
import { LandingAuthScreen } from './ui/screens/LandingAuthScreen';
import { HomeScreen } from './ui/screens/HomeScreen';
import { ScannerScreen } from './ui/screens/ScannerScreen';
import { ResultScreen } from './ui/screens/ResultScreen';
import { AttackExplanationScreen } from './ui/screens/AttackExplanationScreen';
import { HistoryScreen } from './ui/screens/HistoryScreen';
import { SettingsScreen } from './ui/screens/SettingsScreen';
import { DemoScreen } from './ui/screens/DemoScreen';
import { PrivacyCenterScreen } from './ui/screens/PrivacyCenterScreen';
import { UrlIntelligenceScreen } from './ui/screens/UrlIntelligenceScreen';
import { FakePaymentScreen } from './ui/screens/FakePaymentScreen';

const MainAppContent: React.FC = () => {
  const { currentUser, currentScreen } = useAegis();

  if (currentScreen === 'landing' || (!currentUser && currentScreen === 'home')) {
    return <LandingAuthScreen />;
  }

  return (
    <div className="min-h-screen bg-[#0A0A0A] text-[#F5F5F5] font-sans antialiased relative selection:bg-emerald-500 selection:text-black pb-12">
      {/* Dynamic Background Subtle Noise / Grid */}
      <div 
        className="fixed inset-0 pointer-events-none opacity-[0.03] z-0"
        style={{
          backgroundImage: `radial-gradient(circle at 1px 1px, rgba(255,255,255,0.3) 1px, transparent 0)`,
          backgroundSize: '24px 24px'
        }}
      />
      <div className="fixed top-0 left-1/2 -translate-x-1/2 w-[1000px] h-[350px] bg-gradient-to-b from-emerald-500/5 to-transparent blur-3xl pointer-events-none -z-10" />

      {/* Navigation Header / Bar */}
      <Navigation />

      {/* Main Screen Container */}
      <main className="w-full relative z-10">
        <Routes>
          <Route path="/" element={<HomeScreen />} />
          <Route path="/landing" element={<LandingAuthScreen />} />
          <Route path="/scanner" element={<ScannerScreen />} />
          <Route path="/result" element={<ResultScreen />} />
          <Route path="/explanation" element={<AttackExplanationScreen />} />
          <Route path="/history" element={<HistoryScreen />} />
          <Route path="/settings" element={<SettingsScreen />} />
          <Route path="/demo" element={<DemoScreen />} />
          <Route path="/demo-mode" element={<Navigate to="/demo" replace />} />
          <Route path="/privacy" element={<PrivacyCenterScreen />} />
          <Route path="/privacy-center" element={<Navigate to="/privacy" replace />} />
          <Route path="/url-intelligence" element={<UrlIntelligenceScreen />} />
          <Route path="/payment-check" element={<FakePaymentScreen />} />
          <Route path="/fake-payment" element={<Navigate to="/payment-check" replace />} />
          <Route path="/fakepayment" element={<Navigate to="/payment-check" replace />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>

      {/* Progressive Live Analysis Overlay */}
      <LiveAnalysisModal />
    </div>
  );
};

const App: React.FC = () => {
  return (
    <AegisProvider>
      <MainAppContent />
    </AegisProvider>
  );
};

export default App;

