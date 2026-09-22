import React, { createContext, useContext, useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import {
  AskPhantomQnA,
  AuthUser,
  LiveAnalysisStage,
  RiskClassification,
  ScanResult,
  ScanType,
  StageStatus
} from '../../domain/models';
import { AuthRepository } from '../../data/auth/AuthRepository';
import { StorageRepository } from '../../data/local/StorageRepository';
import { extractSignals } from '../../security/SignalExtractor';
import { analyzeUrl } from '../../security/URLAnalyzer';
import { evaluateRisk } from '../../security/RiskEngine';
import { AIReasoningEngine } from '../../ai/AIReasoningEngine';

export type ScreenType =
  | 'landing'
  | 'home'
  | 'scanner'
  | 'result'
  | 'explanation'
  | 'history'
  | 'settings'
  | 'demo_mode'
  | 'privacy_center'
  | 'url_intelligence'
  | 'fakepayment';

export const SCREEN_TO_ROUTE: Record<ScreenType, string> = {
  landing: '/landing',
  home: '/',
  scanner: '/scanner',
  result: '/result',
  explanation: '/explanation',
  history: '/history',
  settings: '/settings',
  demo_mode: '/demo',
  privacy_center: '/privacy',
  url_intelligence: '/url-intelligence',
  fakepayment: '/payment-check'
};

export const ROUTE_TO_SCREEN: Record<string, ScreenType> = {
  '/': 'home',
  '/landing': 'landing',
  '/scanner': 'scanner',
  '/result': 'result',
  '/explanation': 'explanation',
  '/history': 'history',
  '/settings': 'settings',
  '/demo': 'demo_mode',
  '/demo-mode': 'demo_mode',
  '/privacy': 'privacy_center',
  '/privacy-center': 'privacy_center',
  '/url-intelligence': 'url_intelligence',
  '/payment-check': 'fakepayment',
  '/fake-payment': 'fakepayment',
  '/fakepayment': 'fakepayment'
};

interface AegisContextType {
  currentUser: AuthUser | null;
  authLoading: boolean;
  authError: string | null;
  signInAsGuest: () => Promise<void>;
  signOut: () => void;

  currentScreen: ScreenType;
  navigateTo: (screen: ScreenType | string, options?: { replace?: boolean }) => void;
  goBack: (fallbackRoute?: ScreenType | string) => void;

  allHistory: ScanResult[];
  recentHistory: ScanResult[];
  currentResult: ScanResult | null;
  isAnalyzing: boolean;
  liveStages: LiveAnalysisStage[];
  isAskingAi: boolean;
  sensitivity: number;
  setSensitivity: (level: number) => void;

  analyzeContent: (rawText: string, scanType: ScanType, destinationUrl?: string | null) => Promise<void>;
  analyzeDirectText: (text: string) => Promise<void>;
  runSyntheticScenario: (title: string, content: string, scanType: ScanType, destinationUrl?: string | null) => Promise<void>;
  askAegis: (question: string) => Promise<void>;
  selectHistoryItem: (item: ScanResult) => void;
  deleteScan: (id: string | number) => void;
  clearAllHistory: () => void;
  clearLiveDetection: () => void;
}

const AegisContext = createContext<AegisContextType | undefined>(undefined);

const authRepo = AuthRepository.getInstance();
const storageRepo = StorageRepository.getInstance();
const aiEngine = new AIReasoningEngine();

export const AegisProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const navigate = useNavigate();
  const location = useLocation();

  const [currentUser, setCurrentUser] = useState<AuthUser | null>(authRepo.getCurrentUser());
  const [authLoading, setAuthLoading] = useState(false);
  const [authError, setAuthError] = useState<string | null>(null);

  // Derive currentScreen directly from route location
  const currentScreen: ScreenType = ROUTE_TO_SCREEN[location.pathname] || (currentUser ? 'home' : 'landing');

  const [allHistory, setAllHistory] = useState<ScanResult[]>(storageRepo.getAllHistory());
  const [currentResult, setCurrentResult] = useState<ScanResult | null>(() => {
    try {
      const stored = sessionStorage.getItem('aegis_current_result');
      if (stored) return JSON.parse(stored);
    } catch {}
    const history = storageRepo.getAllHistory();
    return history.length > 0 ? history[0] : null;
  });

  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [liveStages, setLiveStages] = useState<LiveAnalysisStage[]>([]);
  const [isAskingAi, setIsAskingAi] = useState(false);
  const [sensitivity, setSensitivityState] = useState<number>(1.0);

  // Keep sessionStorage in sync with current result for reliable refresh
  useEffect(() => {
    if (currentResult) {
      try {
        sessionStorage.setItem('aegis_current_result', JSON.stringify(currentResult));
      } catch {}
    }
  }, [currentResult]);

  useEffect(() => {
    const unsubAuth = authRepo.subscribe(user => {
      setCurrentUser(user);
    });

    const unsubStorage = storageRepo.subscribe(history => {
      setAllHistory(history);
    });

    return () => {
      unsubAuth();
      unsubStorage();
    };
  }, []);

  const signInAsGuest = async () => {
    setAuthLoading(true);
    setAuthError(null);
    try {
      await authRepo.signInAsGuest();
      navigate('/');
    } catch (e: any) {
      setAuthError(e?.message || 'Guest session could not be established.');
    } finally {
      setAuthLoading(false);
    }
  };

  const signOut = () => {
    authRepo.signOut();
    navigate('/landing');
  };

  const navigateTo = (screenOrPath: ScreenType | string, options?: { replace?: boolean }) => {
    const targetPath = (SCREEN_TO_ROUTE as Record<string, string>)[screenOrPath] || screenOrPath;
    navigate(targetPath, options);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const goBack = (fallbackRoute: ScreenType | string = '/') => {
    // In React Router, window.history.state?.idx tracks SPA history depth
    if (window.history.state && typeof window.history.state.idx === 'number' && window.history.state.idx > 0) {
      navigate(-1);
    } else {
      const targetPath = (SCREEN_TO_ROUTE as Record<string, string>)[fallbackRoute] || fallbackRoute;
      navigate(targetPath, { replace: true });
    }
  };

  const setSensitivity = (level: number) => {
    setSensitivityState(level);
  };

  const initStages = (initialLabel: string) => {
    setLiveStages([
      { title: "Digital Ingress", detail: initialLabel, status: StageStatus.ACTIVE },
      { title: "Heuristic Signal Extraction", detail: "Token & Pattern Analysis", status: StageStatus.PENDING },
      { title: "Destination Intelligence", detail: "Structural URI & Domain Inspection", status: StageStatus.PENDING },
      { title: "Risk & Trust Engine", detail: "Deduplicated Mathematical Weighting", status: StageStatus.PENDING },
      { title: "Scam DNA Synthesis", detail: "Psychological Manipulation Modeling", status: StageStatus.PENDING }
    ]);
  };

  const updateStage = (index: number, status: StageStatus, detail?: string) => {
    setLiveStages(prev => {
      const next = [...prev];
      if (index >= 0 && index < next.length) {
        next[index] = {
          ...next[index],
          status,
          detail: detail !== undefined ? detail : next[index].detail
        };
      }
      return next;
    });
  };

  const delay = (ms: number) => new Promise(res => setTimeout(res, ms));

  const analyzeContent = async (
    rawText: string,
    scanType: ScanType,
    destinationUrl?: string | null
  ) => {
    setIsAnalyzing(true);
    initStages("Processing content input");

    await delay(120);
    updateStage(0, StageStatus.COMPLETED, `Input captured: ${rawText.slice(0, 30)}...`);

    // Stage 1: Heuristic Signal Extraction
    updateStage(1, StageStatus.ACTIVE, "Searching for coercive threats & financial hooks");
    const extractedSignals = extractSignals(rawText);
    const extractedCount = extractedSignals.signalDetails.length;
    updateStage(1, StageStatus.COMPLETED, `Identified ${extractedCount} threat signal indicators`);

    // Stage 2: URL / QR Destination Intelligence
    updateStage(2, StageStatus.ACTIVE, "Inspecting URI structure & lookalike domains");
    const urlToInspect = destinationUrl || (extractedSignals.extractedUrls.length > 0 ? extractedSignals.extractedUrls[0] : null);
    const urlAnalysis = urlToInspect ? analyzeUrl(urlToInspect) : null;
    let domainFlags = "No external link present";
    if (urlAnalysis) {
      if (urlAnalysis.isVerifiedPlatform) domainFlags = `Verified platform: ${urlAnalysis.platformName || "Legitimate"}`;
      else if (urlAnalysis.isSuspiciousDomain) domainFlags = "High-risk domain detected";
      else domainFlags = "Domain structure analyzed";
    }
    updateStage(2, StageStatus.COMPLETED, domainFlags);

    // Combine signals
    const enrichedSignals = {
      ...extractedSignals,
      suspiciousDomain: extractedSignals.suspiciousDomain || (urlAnalysis?.isSuspiciousDomain === true),
      suspiciousUrlStructure: extractedSignals.suspiciousUrlStructure || (urlAnalysis?.isSuspiciousStructure === true)
    };

    // Stage 3: Risk Engine calculations
    updateStage(3, StageStatus.ACTIVE, "Calculating multi-vector trust breakdown");
    const riskEval = evaluateRisk(enrichedSignals, urlAnalysis, sensitivity);
    updateStage(3, StageStatus.COMPLETED, `Risk score computed: ${riskEval.score}/100`);

    // Stage 4: AI Reasoning Engine & Scam DNA
    updateStage(4, StageStatus.ACTIVE, "Synthesizing Scam DNA & defense trajectory");
    const aiOutput = await aiEngine.reasonAboutThreat(
      rawText,
      scanType,
      destinationUrl || null,
      enrichedSignals,
      urlAnalysis,
      riskEval
    );
    updateStage(4, StageStatus.COMPLETED, `Scam DNA verified (${aiOutput.classification})`);

    const finalResult: ScanResult = {
      id: `scan_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`,
      timestamp: Date.now(),
      scanType,
      rawContent: rawText,
      destinationUrl: destinationUrl || urlAnalysis?.normalizedUrl || null,
      riskScore: riskEval.score,
      classification: riskEval.classification,
      threatCategory: riskEval.threatCategory,
      intent: riskEval.classification === RiskClassification.SAFE
        ? (urlAnalysis?.isVerifiedPlatform ? `Verified ${urlAnalysis.platformName || "Platform"} Destination` : "Benign Digital Content")
        : aiOutput.intent,
      manipulationTechniques: aiOutput.manipulationTechniques,
      reasons: aiOutput.reasons,
      attackChain: aiOutput.attackChain,
      scamDNA: riskEval.scamDNA,
      evidenceItems: riskEval.evidenceItems,
      trustBreakdown: riskEval.trustBreakdown,
      verificationGuidelines: riskEval.verificationGuidelines,
      whatCouldHappen: riskEval.whatCouldHappen,
      riskTimeline: riskEval.riskTimeline,
      safeActions: riskEval.safeActions,
      urlIntelligence: urlAnalysis?.urlIntelligence || null,
      phantomInsight: aiOutput.phantomInsight,
      recommendedAction: aiOutput.recommendation,
      isAIEnhanced: aiOutput.isCloudEnhanced,
      signals: enrichedSignals,
      qnaHistory: []
    };

    setCurrentResult(finalResult);
    setIsAnalyzing(false);

    // Save to storage
    storageRepo.saveScan(finalResult);
    navigateTo('result');
  };

  const analyzeDirectText = async (text: string) => {
    const isUrl = text.toLowerCase().startsWith('http://') ||
                  text.toLowerCase().startsWith('https://') ||
                  text.toLowerCase().startsWith('upi://');
    const type = isUrl ? ScanType.URL_DESTINATION : ScanType.MESSAGE_TEXT;
    await analyzeContent(text, type, isUrl ? text : null);
  };

  const runSyntheticScenario = async (
    _title: string,
    content: string,
    scanType: ScanType,
    destinationUrl?: string | null
  ) => {
    await analyzeContent(content, scanType, destinationUrl);
  };

  const askAegis = async (question: string) => {
    if (!currentResult || !question.trim()) return;
    setIsAskingAi(true);
    try {
      const answer = await aiEngine.askAegis(question, currentResult);
      const newQnA: AskPhantomQnA = {
        question: question.trim(),
        answer,
        timestamp: Date.now()
      };
      const updated = {
        ...currentResult,
        qnaHistory: [...currentResult.qnaHistory, newQnA]
      };
      setCurrentResult(updated);
      storageRepo.saveScan(updated);
    } finally {
      setIsAskingAi(false);
    }
  };

  const selectHistoryItem = (item: ScanResult) => {
    setCurrentResult(item);
    navigateTo('result');
  };

  const deleteScan = (id: string | number) => {
    storageRepo.deleteScan(id);
    if (currentResult && currentResult.id === id) {
      setCurrentResult(null);
    }
  };

  const clearAllHistory = () => {
    storageRepo.clearHistory();
    setCurrentResult(null);
  };

  const clearLiveDetection = () => {
    // Ready for next scan
  };

  const recentHistory = allHistory.slice(0, 5);

  return (
    <AegisContext.Provider
      value={{
        currentUser,
        authLoading,
        authError,
        signInAsGuest,
        signOut,
        currentScreen,
        navigateTo,
        goBack,
        allHistory,
        recentHistory,
        currentResult,
        isAnalyzing,
        liveStages,
        isAskingAi,
        sensitivity,
        setSensitivity,
        analyzeContent,
        analyzeDirectText,
        runSyntheticScenario,
        askAegis,
        selectHistoryItem,
        deleteScan,
        clearAllHistory,
        clearLiveDetection
      }}
    >
      {children}
    </AegisContext.Provider>
  );
};

export const useAegis = () => {
  const context = useContext(AegisContext);
  if (!context) {
    throw new Error('useAegis must be used within an AegisProvider');
  }
  return context;
};
