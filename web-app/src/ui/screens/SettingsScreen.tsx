import React, { useState } from 'react';
import {
  User,
  Shield,
  CheckCircle,
  Trash2,
  Sliders,
  LogOut,
  Key,
  Info
} from 'lucide-react';
import { useAegis } from '../context/AegisContext';
import { PhantomHeaderBar } from '../components/PhantomHeaderBar';

export const SettingsScreen: React.FC = () => {
  const {
    currentUser,
    signOut,
    sensitivity,
    setSensitivity,
    clearAllHistory
  } = useAegis();

  const [localOnlyMode, setLocalOnlyMode] = useState(false);
  const [apiKeyInput, setApiKeyInput] = useState('');
  const [showSavedNotice, setShowSavedNotice] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [showSignOutConfirm, setShowSignOutConfirm] = useState(false);

  const handleSaveApiKey = () => {
    localStorage.setItem('aegis_gemini_api_key', apiKeyInput.trim());
    setShowSavedNotice(true);
    setTimeout(() => setShowSavedNotice(false), 2500);
  };

  return (
    <div className="w-full max-w-2xl mx-auto px-4 sm:px-6 pt-4 pb-28 md:pt-20 space-y-6">
      <PhantomHeaderBar subtitle="Security & Privacy Console" />

      {/* 1. AUTHENTICATED IDENTITY & ACCESS */}
      <div className="space-y-3">
        <span className="text-[11px] font-bold tracking-wider text-[#10B981] uppercase px-1">
          AUTHENTICATED IDENTITY & ACCESS
        </span>

        <div className="bg-[#141414] border border-white/10 rounded-2xl p-5 space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="w-11 h-11 rounded-xl bg-[#10B981]/15 border border-[#10B981]/30 flex items-center justify-center text-[#34D399]">
                <User className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-sm font-bold text-[#F5F5F5]">
                  {currentUser?.displayName || "Authenticated Analyst"}
                </h3>
                <p className="text-xs text-white/50">
                  {currentUser?.isAnonymous ? "Guest Session (Unrestricted Local)" : currentUser?.email}
                </p>
              </div>
            </div>

            <span className="text-[10px] font-bold px-2.5 py-1 rounded-md bg-[#10B981]/20 text-[#34D399] tracking-wider uppercase">
              {currentUser?.provider || "GUEST"}
            </span>
          </div>

          <div className="flex items-center justify-between pt-2 border-t border-white/5">
            <span className="text-[10px] font-mono text-white/40">
              UID: {currentUser?.uid?.slice(0, 16) || "local_active"}...
            </span>

            <button
              onClick={() => setShowSignOutConfirm(true)}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl border border-[#EF4444]/40 hover:bg-[#EF4444]/15 text-[#EF4444] text-xs font-bold uppercase transition-colors"
            >
              <LogOut className="w-3.5 h-3.5" />
              <span>Sign Out</span>
            </button>
          </div>
        </div>
      </div>

      {/* 2. DATA & PRIVACY */}
      <div className="space-y-3">
        <span className="text-[11px] font-bold tracking-wider text-[#10B981] uppercase px-1">
          DATA & PRIVACY
        </span>

        <div className="bg-[#141414] border border-white/10 rounded-2xl p-5 space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <h4 className="text-xs font-semibold text-[#F5F5F5]">Client-Side OCR Guarantee</h4>
              <p className="text-[11px] text-white/50 mt-0.5">
                Camera frames and text recognition run strictly offline on Google ML Kit / Web Canvas.
              </p>
            </div>
            <CheckCircle className="w-5 h-5 text-[#34D399] shrink-0" />
          </div>

          <div className="flex items-center justify-between pt-3 border-t border-white/5">
            <div>
              <h4 className="text-xs font-semibold text-[#F5F5F5]">Strict Offline-Only Mode</h4>
              <p className="text-[11px] text-white/50 mt-0.5">
                Bypass cloud Gemini calls and use deterministic local psychological engine exclusively.
              </p>
            </div>
            <button
              onClick={() => setLocalOnlyMode(!localOnlyMode)}
              className={`w-11 h-6 rounded-full transition-colors relative flex items-center p-0.5 shrink-0 ${
                localOnlyMode ? 'bg-[#10B981]' : 'bg-[#2A2A2A]'
              }`}
            >
              <span
                className={`w-5 h-5 rounded-full bg-[#0A0A0A] shadow transition-transform ${
                  localOnlyMode ? 'translate-x-5' : 'translate-x-0'
                }`}
              />
            </button>
          </div>

          <button
            onClick={() => setShowDeleteConfirm(true)}
            className="w-full py-3 rounded-xl border border-[#EF4444]/40 hover:bg-[#EF4444]/15 text-[#EF4444] text-xs font-bold uppercase tracking-wider flex items-center justify-center gap-2 transition-colors mt-2"
          >
            <Trash2 className="w-4 h-4" />
            <span>DELETE SCAN HISTORY & SIGNALS</span>
          </button>
        </div>
      </div>

      {/* 3. DETECTION SENSITIVITY */}
      <div className="space-y-3">
        <span className="text-[11px] font-bold tracking-wider text-[#10B981] uppercase px-1">
          DETECTION SENSITIVITY
        </span>

        <div className="bg-[#141414] border border-white/10 rounded-2xl p-5 space-y-3">
          <div>
            <h4 className="text-xs font-semibold text-[#F5F5F5]">Threat Calibration Factor</h4>
            <p className="text-[11px] text-white/50 mt-0.5">
              Determines how aggressively Aegis penalizes urgency and unfamiliar domains.
            </p>
          </div>

          <div className="grid grid-cols-3 gap-2 pt-1">
            {[
              { label: 'Standard', factor: 1.0 },
              { label: 'High (+15%)', factor: 1.15 },
              { label: 'Max (+30%)', factor: 1.30 },
            ].map(level => {
              const isSelected = Math.abs(sensitivity - level.factor) < 0.05;
              return (
                <button
                  key={level.label}
                  onClick={() => setSensitivity(level.factor)}
                  className={`py-2.5 rounded-xl text-xs font-semibold uppercase tracking-wider transition-all ${
                    isSelected
                      ? 'bg-[#10B981]/20 text-[#34D399] border border-[#10B981]/40'
                      : 'bg-[#0A0A0A] text-white/50 border border-white/5 hover:text-white/80'
                  }`}
                >
                  {level.label}
                </button>
              );
            })}
          </div>
        </div>
      </div>

      {/* 4. OPTIONAL GEMINI AI KEY CONFIGURATION */}
      <div className="space-y-3">
        <span className="text-[11px] font-bold tracking-wider text-[#38BDF8] uppercase px-1">
          GEMINI COGNITIVE AI CONFIGURATION
        </span>

        <div className="bg-[#141414] border border-white/10 rounded-2xl p-5 space-y-3">
          <div className="flex items-center gap-2">
            <Key className="w-4 h-4 text-[#38BDF8]" />
            <h4 className="text-xs font-semibold text-[#F5F5F5]">Google AI Studio API Key (Optional)</h4>
          </div>
          <p className="text-[11px] text-white/50 leading-relaxed">
            Aegis works 100% offline out-of-the-box. Optionally paste your Gemini API key to enable live cloud reasoning.
          </p>

          <div className="flex items-center gap-2 pt-1">
            <input
              type="password"
              value={apiKeyInput}
              onChange={(e) => setApiKeyInput(e.target.value)}
              placeholder="Enter AI Studio Key (or leave empty for offline engine)"
              className="flex-1 bg-[#0A0A0A] border border-white/10 focus:border-[#38BDF8] rounded-xl px-3.5 py-2.5 text-xs text-[#F5F5F5] placeholder:text-white/30 focus:outline-none font-mono"
            />
            <button
              onClick={handleSaveApiKey}
              className="px-4 py-2.5 bg-[#38BDF8] hover:bg-[#0284C7] text-[#0A0A0A] text-xs font-bold uppercase rounded-xl transition-colors"
            >
              Save
            </button>
          </div>

          {showSavedNotice && (
            <p className="text-[11px] text-[#34D399] font-bold uppercase">
              ✓ API key configuration updated locally!
            </p>
          )}
        </div>
      </div>

      {/* 5. SECURITY ARCHITECTURE OVERVIEW */}
      <div className="space-y-3">
        <span className="text-[11px] font-bold tracking-wider text-white/40 uppercase px-1">
          SECURITY ARCHITECTURE
        </span>

        <div className="bg-[#141414] border border-white/10 rounded-2xl p-5 space-y-2">
          <h4 className="text-xs font-bold text-[#F5F5F5]">AEGIS Flagship Threat Defense v1.0</h4>
          <p className="text-[11px] text-white/50 leading-relaxed">
            Pipeline: Camera / Image → ML Kit OCR/QR → Signal Extractor → Risk Matrix Engine (0-100) → AI Cognitive Reasoning (Gemini + Local) → Behavioral Counter-Measures.
          </p>
          <p className="text-xs font-semibold text-[#34D399] pt-1">
            "Don't trust what your screen shows. Let your phone verify it."
          </p>
        </div>
      </div>

      {/* Dialogs */}
      {showSignOutConfirm && (
        <div className="fixed inset-0 z-50 bg-black/80 flex items-center justify-center p-4">
          <div className="bg-[#141414] border border-white/10 rounded-2xl p-5 max-w-sm w-full space-y-4 shadow-2xl">
            <h3 className="text-sm font-bold text-[#F5F5F5]">Sign Out of Aegis</h3>
            <p className="text-xs text-white/60">
              Are you sure you want to end your current authenticated session?
            </p>
            <div className="flex items-center justify-end gap-2 pt-2">
              <button
                onClick={() => setShowSignOutConfirm(false)}
                className="px-4 py-2 rounded-xl text-xs font-semibold text-white/60 hover:text-white"
              >
                CANCEL
              </button>
              <button
                onClick={() => {
                  setShowSignOutConfirm(false);
                  signOut();
                }}
                className="px-4 py-2 rounded-xl bg-[#EF4444] text-white font-bold text-xs"
              >
                SIGN OUT
              </button>
            </div>
          </div>
        </div>
      )}

      {showDeleteConfirm && (
        <div className="fixed inset-0 z-50 bg-black/80 flex items-center justify-center p-4">
          <div className="bg-[#141414] border border-white/10 rounded-2xl p-5 max-w-sm w-full space-y-4 shadow-2xl">
            <h3 className="text-sm font-bold text-[#F5F5F5]">Delete All History</h3>
            <p className="text-xs text-white/60">
              Permanently erase all scan logs and threat signals stored on this device?
            </p>
            <div className="flex items-center justify-end gap-2 pt-2">
              <button
                onClick={() => setShowDeleteConfirm(false)}
                className="px-4 py-2 rounded-xl text-xs font-semibold text-white/60 hover:text-white"
              >
                CANCEL
              </button>
              <button
                onClick={() => {
                  clearAllHistory();
                  setShowDeleteConfirm(false);
                }}
                className="px-4 py-2 rounded-xl bg-[#EF4444] text-white font-bold text-xs"
              >
                DELETE
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
