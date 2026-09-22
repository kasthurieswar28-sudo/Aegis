/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        obsidian: {
          bg: '#0A0A0A',
          surface: '#141414',
          card: '#141414',
          hover: '#1E1E1E',
          nav: '#0F0F0F',
        },
        slate: {
          card: '#141414',
          cardHover: '#1E1E1E',
        },
        safe: {
          emerald: '#10B981',
          light: '#34D399',
          dark: '#059669',
        },
        threat: {
          red: '#EF4444',
          dark: '#DC2626',
        },
        electric: {
          amber: '#F59E0B',
        },
        critical: {
          violet: '#A855F7',
        },
        cyber: {
          cyan: '#10B981',
          cyanDim: '#059669',
          sky: '#34D399',
        },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'BlinkMacSystemFont', 'Segoe UI', 'Roboto', 'sans-serif'],
        mono: ['JetBrains Mono', 'Menlo', 'Monaco', 'Courier New', 'monospace'],
      },
      animation: {
        'pulse-slow': 'pulse 3s cubic-bezier(0.4, 0, 0.6, 1) infinite',
        'spin-slow': 'spin 5s linear infinite',
      }
    },
  },
  plugins: [],
}
