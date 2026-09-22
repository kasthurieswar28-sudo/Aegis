import React, { useEffect, useRef, useState } from 'react';
import { Camera, Type, Image as ImageIcon, Shield, QrCode, AlertCircle, ArrowRight } from 'lucide-react';
import jsQR from 'jsqr';
import { ScanType } from '../../domain/models';
import { useAegis } from '../context/AegisContext';

export const ScannerScreen: React.FC = () => {
  const { analyzeContent, analyzeDirectText } = useAegis();
  const [selectedTab, setSelectedTab] = useState<'camera' | 'text'>('camera');
  const [textInput, setTextInput] = useState('');
  const [detectedContent, setDetectedContent] = useState<string | null>(null);
  const [cameraError, setCameraError] = useState<string | null>(null);
  const [isCameraActive, setIsCameraActive] = useState(false);

  const videoRef = useRef<HTMLVideoElement>(null);
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const streamRef = useRef<MediaStream | null>(null);

  // Initialize camera when tab is camera
  useEffect(() => {
    let animationFrameId: number;

    const startCamera = async () => {
      setCameraError(null);
      try {
        const stream = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: 'environment', width: { ideal: 1280 }, height: { ideal: 720 } }
        });
        streamRef.current = stream;
        if (videoRef.current) {
          videoRef.current.srcObject = stream;
          videoRef.current.setAttribute('playsinline', 'true');
          await videoRef.current.play();
          setIsCameraActive(true);
          scanFrame();
        }
      } catch (err: any) {
        console.warn('Camera access issue:', err);
        setCameraError(
          err.name === 'NotAllowedError'
            ? 'Camera access permission was denied. Please allow camera access or use direct text / file upload.'
            : 'No active camera hardware detected. Use image upload or manual text input below.'
        );
        setIsCameraActive(false);
      }
    };

    const scanFrame = () => {
      if (videoRef.current && canvasRef.current && videoRef.current.readyState === videoRef.current.HAVE_ENOUGH_DATA) {
        const canvas = canvasRef.current;
        const video = videoRef.current;
        const ctx = canvas.getContext('2d', { willReadFrequently: true });

        if (ctx) {
          canvas.width = video.videoWidth;
          canvas.height = video.videoHeight;
          ctx.drawImage(video, 0, 0, canvas.width, canvas.height);

          const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
          const code = jsQR(imageData.data, imageData.width, imageData.height, {
            inversionAttempts: 'dontInvert'
          });

          if (code && code.data) {
            setDetectedContent(code.data);
          }
        }
      }
      animationFrameId = requestAnimationFrame(scanFrame);
    };

    if (selectedTab === 'camera') {
      startCamera();
    } else {
      if (streamRef.current) {
        streamRef.current.getTracks().forEach(t => t.stop());
        streamRef.current = null;
      }
      setIsCameraActive(false);
    }

    return () => {
      if (streamRef.current) {
        streamRef.current.getTracks().forEach(t => t.stop());
        streamRef.current = null;
      }
      cancelAnimationFrame(animationFrameId);
    };
  }, [selectedTab]);

  // Handle Image File Upload (QR or text parsing)
  const handleImageUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (event) => {
      const img = new Image();
      img.onload = () => {
        const canvas = document.createElement('canvas');
        canvas.width = img.width;
        canvas.height = img.height;
        const ctx = canvas.getContext('2d');
        if (ctx) {
          ctx.drawImage(img, 0, 0);
          const imageData = ctx.getImageData(0, 0, img.width, img.height);
          const code = jsQR(imageData.data, imageData.width, imageData.height);

          if (code && code.data) {
            analyzeContent(code.data, ScanType.QR_CODE, code.data);
          } else {
            // If no QR found, analyze image note or prompt user
            analyzeContent(`Uploaded Image: ${file.name} (Receipt/Document)`, ScanType.PAYMENT_SCREENSHOT);
          }
        }
      };
      img.src = event.target?.result as string;
    };
    reader.readAsDataURL(file);
  };

  return (
    <div className="w-full max-w-2xl mx-auto px-4 sm:px-6 pt-4 pb-24 md:pt-20 flex flex-col items-center">
      {/* Tab Switcher */}
      <div className="w-full grid grid-cols-2 bg-[#141414] border border-white/10 rounded-xl p-1 mb-5">
        <button
          onClick={() => setSelectedTab('camera')}
          className={`flex items-center justify-center gap-2 py-2.5 rounded-lg text-xs font-bold tracking-wider uppercase transition-all duration-200 ${
            selectedTab === 'camera'
              ? 'bg-[#10B981]/20 text-[#34D399] border border-[#10B981]/40 shadow-[0_0_15px_rgba(16,185,129,0.15)]'
              : 'text-white/40 hover:text-white/70'
          }`}
        >
          <Camera className="w-4 h-4" />
          <span>Live Camera</span>
        </button>

        <button
          onClick={() => setSelectedTab('text')}
          className={`flex items-center justify-center gap-2 py-2.5 rounded-lg text-xs font-bold tracking-wider uppercase transition-all duration-200 ${
            selectedTab === 'text'
              ? 'bg-[#10B981]/20 text-[#34D399] border border-[#10B981]/40 shadow-[0_0_15px_rgba(16,185,129,0.15)]'
              : 'text-white/40 hover:text-white/70'
          }`}
        >
          <Type className="w-4 h-4" />
          <span>Direct Text / URL</span>
        </button>
      </div>

      {/* Main View Container */}
      <div className="w-full bg-[#141414] border border-white/10 rounded-2xl overflow-hidden min-h-[380px] flex flex-col justify-between relative shadow-xl">
        {selectedTab === 'camera' ? (
          <div className="relative w-full h-[380px] sm:h-[420px] bg-black flex items-center justify-center overflow-hidden">
            {isCameraActive ? (
              <>
                <video
                  ref={videoRef}
                  className="w-full h-full object-cover"
                  autoPlay
                  playsInline
                  muted
                />
                <canvas ref={canvasRef} className="hidden" />

                {/* Target Reticle Corners and Scanning Laser */}
                <div className="absolute inset-0 pointer-events-none flex items-center justify-center p-8">
                  <div className="relative w-60 h-60 sm:w-68 sm:h-68">
                    {/* Corner Brackets */}
                    <div className="absolute top-0 left-0 w-8 h-8 border-t-2 border-l-2 border-[#34D399]" />
                    <div className="absolute top-0 right-0 w-8 h-8 border-t-2 border-r-2 border-[#34D399]" />
                    <div className="absolute bottom-0 left-0 w-8 h-8 border-b-2 border-l-2 border-[#34D399]" />
                    <div className="absolute bottom-0 right-0 w-8 h-8 border-b-2 border-r-2 border-[#34D399]" />

                    {/* Animated Scanning Laser */}
                    <div className="absolute left-2 right-2 h-0.5 bg-[#34D399] shadow-[0_0_12px_#34D399] animate-laser" />
                  </div>
                </div>

                {/* Detected QR Card Overlay Banner */}
                <div className="absolute bottom-4 left-4 right-4 z-20">
                  {detectedContent ? (
                    <div className="bg-[#0F1E2E]/95 border border-[#10B981] rounded-xl p-3.5 backdrop-blur-md shadow-2xl animate-fadeIn">
                      <div className="flex items-center gap-2 text-[#34D399] text-xs font-bold uppercase tracking-wider mb-1">
                        <span className="w-2 h-2 rounded-full bg-[#34D399] animate-ping" />
                        <span>QR CODE DETECTED</span>
                      </div>
                      <p className="text-xs text-[#F5F5F5] font-mono truncate mb-3">
                        {detectedContent}
                      </p>
                      <button
                        onClick={() => analyzeContent(detectedContent, ScanType.QR_CODE, detectedContent)}
                        className="w-full py-2 bg-[#10B981] hover:bg-[#059669] text-[#0A0A0A] font-bold text-xs rounded-lg tracking-wider uppercase transition-colors"
                      >
                        ANALYZE NOW
                      </button>
                    </div>
                  ) : (
                    <div className="bg-black/70 backdrop-blur-md border border-white/10 rounded-full px-4 py-2 flex items-center justify-center gap-2 text-xs text-[#F5F5F5]">
                      <span className="w-2 h-2 rounded-full bg-[#34D399]" />
                      <span>Point camera at QR code or message text</span>
                    </div>
                  )}
                </div>
              </>
            ) : (
              <div className="p-6 text-center flex flex-col items-center justify-center max-w-sm">
                <div className="w-14 h-14 rounded-2xl bg-[#10B981]/15 border border-[#10B981]/30 flex items-center justify-center text-[#34D399] mb-4">
                  <Camera className="w-7 h-7" />
                </div>
                <h3 className="text-base font-bold text-[#F5F5F5] uppercase tracking-wider">
                  CAMERA SCANNER READY
                </h3>
                <p className="text-xs text-white/60 mt-2 leading-relaxed">
                  {cameraError || "Point camera at any physical QR code or display screen to inspect for deception."}
                </p>
                <div className="mt-5 flex flex-col sm:flex-row gap-2 w-full">
                  <button
                    onClick={() => fileInputRef.current?.click()}
                    className="flex-1 py-2.5 px-4 rounded-xl bg-[#10B981] hover:bg-[#059669] text-[#0A0A0A] font-bold text-xs tracking-wider uppercase transition-all"
                  >
                    Pick QR Image
                  </button>
                  <button
                    onClick={() => setSelectedTab('text')}
                    className="flex-1 py-2.5 px-4 rounded-xl bg-white/5 hover:bg-white/10 text-white/80 font-bold text-xs tracking-wider uppercase transition-all"
                  >
                    Enter Text / Link
                  </button>
                </div>
              </div>
            )}
          </div>
        ) : (
          <div className="p-5 flex-1 flex flex-col justify-between space-y-4">
            <div>
              <span className="text-[11px] font-bold tracking-wider text-[#10B981] uppercase">
                DIRECT INSPECTION
              </span>
              <p className="text-xs text-white/60 mt-1 leading-relaxed">
                Paste any suspicious SMS, WhatsApp message, email, or URL directly to analyze without camera.
              </p>

              <textarea
                value={textInput}
                onChange={(e) => setTextInput(e.target.value)}
                placeholder="Paste suspicious message, link, or payment request here..."
                rows={6}
                className="w-full mt-3 p-3.5 bg-[#0A0A0A] border border-white/10 focus:border-[#10B981] rounded-xl text-xs text-[#F5F5F5] placeholder:text-white/30 focus:outline-none transition-colors resize-none font-sans"
              />
            </div>

            <button
              onClick={() => textInput.trim() && analyzeDirectText(textInput)}
              disabled={!textInput.trim()}
              className="w-full py-3.5 bg-[#10B981] hover:bg-[#059669] disabled:opacity-30 disabled:cursor-not-allowed text-[#0A0A0A] font-bold text-xs tracking-wider uppercase rounded-xl flex items-center justify-center gap-2 transition-all shadow-[0_0_20px_rgba(16,185,129,0.2)]"
            >
              <Shield className="w-4 h-4" />
              <span>EVALUATE RISKS</span>
            </button>
          </div>
        )}

        {/* Bottom Import Action Bar */}
        <div className="w-full bg-[#181818] border-t border-white/10 px-4 py-3 flex items-center justify-between">
          <input
            ref={fileInputRef}
            type="file"
            accept="image/*"
            className="hidden"
            onChange={handleImageUpload}
          />

          <button
            onClick={() => fileInputRef.current?.click()}
            className="flex items-center gap-2 px-3.5 py-1.5 rounded-lg bg-[#1E293B] hover:bg-[#2A3B50] text-[#F5F5F5] text-xs font-semibold transition-colors"
          >
            <ImageIcon className="w-4 h-4 text-[#34D399]" />
            <span>Pick Image</span>
          </button>

          <span className="text-[11px] text-white/40 font-mono">
            Privacy: Client-Side OCR
          </span>
        </div>
      </div>
    </div>
  );
};
