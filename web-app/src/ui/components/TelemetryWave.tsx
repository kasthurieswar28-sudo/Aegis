import React, { useEffect, useRef } from 'react';

export const TelemetryWave: React.FC<{
  width?: number;
  height?: number;
  className?: string;
}> = ({ width = 92, height = 18, className = '' }) => {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    let phase = 0;
    let animationId: number;

    const render = () => {
      ctx.clearRect(0, 0, width, height);

      ctx.beginPath();
      ctx.strokeStyle = 'rgba(52, 211, 153, 0.75)';
      ctx.lineWidth = 1.4;
      ctx.lineCap = 'round';

      const steps = 32;
      const midY = height / 2;
      const amplitude = height * 0.38;

      for (let i = 0; i <= steps; i++) {
        const x = (i / steps) * width;
        const progress = i / steps;
        // Window envelope to taper ends smoothly
        const envelope = Math.sin(progress * Math.PI);
        const y = midY + Math.sin(progress * 4 * Math.PI + phase) * amplitude * envelope;

        if (i === 0) {
          ctx.moveTo(x, y);
        } else {
          ctx.lineTo(x, y);
        }
      }

      ctx.stroke();

      phase += 0.05;
      animationId = requestAnimationFrame(render);
    };

    render();

    return () => {
      cancelAnimationFrame(animationId);
    };
  }, [width, height]);

  return (
    <canvas
      ref={canvasRef}
      width={width}
      height={height}
      className={className}
      style={{ width: `${width}px`, height: `${height}px` }}
    />
  );
};
