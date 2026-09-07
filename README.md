# Aegis — AI Cybersecurity & Anti-Quishing Shield for Android

<p align="center">
  <img src="app/src/main/res/drawable/ic_launcher_foreground.xml" alt="Aegis Logo" width="120" height="120" />
</p>

<p align="center">
  <strong>Phone-first AI cybersecurity assistant detecting digital scams, deceptive QR codes, and social engineering before you act.</strong>
</p>

<p align="center">
  <a href="#key-features">Features</a> •
  <a href="#how-it-works">How It Works</a> •
  <a href="#tech-stack">Tech Stack</a> •
  <a href="#architecture">Architecture</a> •
  <a href="#getting-started">Getting Started</a> •
  <a href="#configuration">Configuration</a> •
  <a href="#license">License</a>
</p>

---

## 🛡️ Overview

**Aegis** is an autonomous, on-device mobile security application built with Kotlin and Jetpack Compose. It serves as a personal deception firewall, protecting users against modern social engineering attacks, deceptive QR codes (**quishing**), malicious payment redirects (UPI/wire scams), homograph attacks, and urgent coercive language.

Designed with a **privacy-first zero-trust philosophy**, Aegis analyzes URLs and messages locally without requiring cloud account registrations or collecting telemetry.

---

## ✨ Key Features

### 🔍 Optical Scam & Quishing Scanner
- **Live Camera & Photo Picker Analysis**: Real-time barcode and text scanning powered by Google ML Kit and CameraX.
- **Quishing Defense**: Detects malicious redirects embedded inside innocuous-looking QR codes.
- **Deceptive Domain Detection**: Flags homograph attacks (Cyrillic/Latin lookalikes), punycode (`xn--`), suspicious top-level domains (`.top`, `.xyz`, `.icu`, `.click`, `.buzz`), and IP-based URLs.

### 🧠 Multi-Vector Risk Matrix Engine (0–100 Score)
- **Heuristic Psychological Scoring**: Analyzes language for artificial urgency, coercive threats, fake legal action, and authority impersonation (e.g., tax departments, banks, utility providers).
- **Payment Payload Dissection**: Identifies rogue UPI schemes (`upi://pay`), money requests masquerading as refunds, and unverified merchant handles.
- **Verified Platform Whitelisting**: Accurately recognizes genuine links (Instagram profiles, WhatsApp, LinkedIn, GitHub, YouTube, verified banks) with high-confidence green safety badges to eliminate false positives.

### 🤖 Gemini-Powered Cognitive Reasoning (Optional)
- **Psychological Scam Breakdown**: Synthesizes human-readable explanations of deceptive psychological tricks.
- **Ask Aegis (Interactive Q&A)**: Context-aware interactive assistant answering questions about scanned content directly in the scan result screen.
- **Deterministic Offline Fallback**: Functions 100% offline using deterministic rule engines if network access is disabled.

### 🎮 Interactive Scam Simulator (Demo Mode)
- Test your instincts against realistic simulated attack scenarios:
  - **Electricity Disconnection Scam**: Urgency + payment manipulation.
  - **Bank KYC Phishing**: Authority impersonation + deceptive domain.
  - **Lottery / Reward Trap**: Greed exploitation + advance fee fraud.
  - **Courier Delivery Issue**: Package delivery link with deceptive credential harvester.

### 🔒 100% Local & Zero-Knowledge
- **Guest-First Access**: Immediate entry without account creation, sign-in forms, or cloud lock-in.
- **Local Persistence**: Full scan history and threat logs stored safely on-device via Room SQLite with export and clearing controls.
- **Zero Firebase / Cloud Tracking**: No remote database tracking or analytics logging.

---

## 🏗️ How It Works

```
                       ┌───────────────────────┐
                       │  CameraX / Gallery    │
                       └──────────┬────────────┘
                                  │
                                  ▼
                       ┌───────────────────────┐
                       │  Google ML Kit        │
                       │  (Barcode & OCR)      │
                       └──────────┬────────────┘
                                  │
                                  ▼
                       ┌───────────────────────┐
                       │   Signal Extractor    │
                       │   & URL Analyzer      │
                       └──────────┬────────────┘
                                  │
             ┌────────────────────┴────────────────────┐
             ▼                                         ▼
┌─────────────────────────┐               ┌─────────────────────────┐
│ Verified Safe Whitelist │               │  Risk Engine (0 - 100)  │
│ (Instagram, YouTube,    │               │  Urgency • Authority    │
│  GitHub, LinkedIn...)   │               │  Threats • Homoglyphs   │
└────────────┬────────────┘               └────────────┬────────────┘
             │                                         │
             └────────────────────┬────────────────────┘
                                  │
                                  ▼
                       ┌───────────────────────┐
                       │   Aegis Result &      │
                       │ Counter-Measures UI   │
                       └──────────┬────────────┘
                                  │
                     (Optional Gemini AI Insights)
```

---

## 💻 Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/) (100%)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material Design 3
- **Architecture**: MVVM (Model-View-ViewModel) + Clean Architecture Principles
- **Computer Vision**: [Google ML Kit](https://developers.google.com/ml-kit) (Barcode Scanning & Latin Text Recognition)
- **Camera Pipeline**: Android [CameraX](https://developer.android.com/training/camerax)
- **Local Persistence**: Android [Room Database](https://developer.android.com/training/data-storage/room) (SQLite)
- **Asynchronous Operations**: Kotlin Coroutines & `StateFlow`
- **Generative AI**: Google [Gemini API](https://ai.google.dev/) (HTTP client / REST reasoning)
- **Testing**: JUnit 4, Robolectric, Roborazzi

---

## 📁 Project Structure

```
app/src/main/java/com/example/
├── ai/                      # AI reasoning & Gemini REST integration
│   └── AIReasoningEngine.kt
├── camera/                  # CameraX analyzer & ML Kit optical pipeline
│   └── MLKitScanner.kt
├── data/
│   ├── auth/                # Local offline Guest session management
│   ├── local/               # Room Database, DAO, and Type Converters
│   └── repository/          # Scan history repository
├── domain/model/            # Core domain models (ScanResult, RiskSignals, etc.)
├── security/                # Core security heuristics & risk engines
│   ├── RiskEngine.kt        # Multi-factor score evaluation
│   ├── SignalExtractor.kt   # NLP psychological indicator extraction
│   └── URLAnalyzer.kt       # Domain, TLD, Punycode & Whitelist verification
└── ui/
    ├── auth/                # Zero-trust landing & guest entry screen
    ├── demo/                # Interactive scam simulation laboratory
    ├── history/             # Scan records, filters & details
    ├── navigation/          # Compose type-safe navigation graph
    ├── privacy/             # Transparent data policy & permission center
    ├── result/              # Risk gauge, threat badges & action controls
    ├── scanner/             # Live viewfinder & gallery scanner
    ├── settings/            # Sensitivity slider & AI reasoning toggles
    └── theme/               # Material 3 cyberpunk aesthetic theme
```

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio**: Ladybug (2024.2.1+) or newer
- **JDK**: Version 17 or higher
- **Android SDK**: Minimum API 26 (Android 8.0 Oreo), Target API 35 (Android 15)

### Clone & Build

```bash
# Clone the repository
git clone https://github.com/your-username/aegis-android.git
cd aegis-android

# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew testDebugUnitTest
```

---

## ⚙️ Configuration

Aegis works **out of the box** in offline heuristic mode. To optionally enable Gemini-powered cognitive reasoning:

1. Obtain an API key from [Google AI Studio](https://aistudio.google.com/).
2. Create a `.env` file in the project root:
   ```properties
   GEMINI_API_KEY=your_actual_api_key_here
   ```
3. Rebuild the project. The key is automatically injected via the `secrets-gradle-plugin` into `BuildConfig.GEMINI_API_KEY`.

---

## 🧪 Testing

Aegis includes automated test suites covering core security mechanics:
- **Heuristic Engine Tests**: Validates detection of fake electricity bills, bank KYC phishing, urgency traps, and safe appointment messages (`SecurityEngineTest.kt`).
- **Domain & URL Tests**: Validates homograph attacks, malicious TLD recognition, and whitelist verification for legitimate platforms.

Run tests via command line:
```bash
./gradlew testDebugUnitTest
```

---

## 📄 License

This project is licensed under the [Apache 2.0 License](LICENSE).
