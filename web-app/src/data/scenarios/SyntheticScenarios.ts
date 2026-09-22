import { RiskClassification, ScanType, ThreatCategory } from '../../domain/models';

export interface DemoScenario {
  id: string;
  title: string;
  category: ThreatCategory;
  expectedClassification: RiskClassification;
  scanType: ScanType;
  iconName: string;
  description: string;
  syntheticContent: string;
  destinationUrl?: string | null;
}

export const SYNTHETIC_SCENARIOS: DemoScenario[] = [
  {
    id: "bank_qr_kyc",
    title: "Bank KYC QR Attack",
    category: ThreatCategory.PAYMENT_FRAUD,
    expectedClassification: RiskClassification.CRITICAL,
    scanType: ScanType.QR_CODE,
    iconName: "QrCode",
    description: "Deceptive QR code claiming State Bank KYC suspension with ₹50 reactivation fee.",
    syntheticContent: "upi://pay?pa=reactivate.kyc.sbi@fakebank&pn=State%20Bank%20KYC&am=50.00&cu=INR&tn=Immediate%20KYC%20Reactivation",
    destinationUrl: "upi://pay?pa=reactivate.kyc.sbi@fakebank&pn=State%20Bank%20KYC&am=50.00&cu=INR"
  },
  {
    id: "delivery_payment",
    title: "Fake Courier Delivery Fee",
    category: ThreatCategory.FINANCIAL_SCAM,
    expectedClassification: RiskClassification.HIGH_RISK,
    scanType: ScanType.MESSAGE_TEXT,
    iconName: "CreditCard",
    description: "Urgent parcel hold notice demanding ₹49 redelivery fee via untrusted link.",
    syntheticContent: "India Post: Your package #IN984102 cannot be delivered due to incomplete address. Pay ₹49 pending redelivery fee immediately at https://indiapost-parcel-redelivery.top/pay or item will be returned.",
    destinationUrl: "https://indiapost-parcel-redelivery.top/pay"
  },
  {
    id: "otp_social_eng",
    title: "Telecom SIM Block OTP Coercion",
    category: ThreatCategory.OTP_SOCIAL_ENGINEERING,
    expectedClassification: RiskClassification.CRITICAL,
    scanType: ScanType.MESSAGE_TEXT,
    iconName: "Smartphone",
    description: "Aggressive threat claiming SIM deactivation today unless 6-digit OTP is verified.",
    syntheticContent: "URGENT: Telecom Regulatory Authority. Your SIM card will be permanently disconnected within 2 hours due to unverified Aadhaar. Share the 6-digit verification code OTP received on your mobile immediately to prevent cutoff."
  },
  {
    id: "fake_customer_support",
    title: "Fake Support Remote Desk",
    category: ThreatCategory.FAKE_SUPPORT,
    expectedClassification: RiskClassification.CRITICAL,
    scanType: ScanType.MESSAGE_TEXT,
    iconName: "Headphones",
    description: "Pretends to offer payment refund by pushing AnyDesk remote desktop APK.",
    syntheticContent: "Paytm Customer Support: Your refund of ₹4,999 is on hold. Please install AnyDesk or QuickSupport APK immediately from http://192.168.1.45/paytm-helper.apk and provide your 9-digit session code to clear the transaction."
  },
  {
    id: "crypto_investment",
    title: "Guaranteed Returns Crypto Scheme",
    category: ThreatCategory.FAKE_INVESTMENT,
    expectedClassification: RiskClassification.HIGH_RISK,
    scanType: ScanType.MESSAGE_TEXT,
    iconName: "TrendingUp",
    description: "Unsolicited high-yield investment scheme promising 300% daily returns.",
    syntheticContent: "VIP Invitation: Official Binance Cloud Mining Pool. Invest ₹10,000 today and receive guaranteed ₹30,000 daily returns credited to your wallet. Limited slots available! Claim bonus at https://binance-earn-crypto.xyz/invest"
  },
  {
    id: "credential_phishing",
    title: "Lookalike Banking Phishing Portal",
    category: ThreatCategory.PHISHING,
    expectedClassification: RiskClassification.CRITICAL,
    scanType: ScanType.URL_DESTINATION,
    iconName: "Globe",
    description: "Homoglyph domain imitating HDFC Bank login with high-risk .top extension.",
    syntheticContent: "https://hdfcbank-netbanking-secure-auth.top/login.php?session=urgent",
    destinationUrl: "https://hdfcbank-netbanking-secure-auth.top/login.php?session=urgent"
  },
  {
    id: "safe_notification",
    title: "Legitimate Clinic Appointment",
    category: ThreatCategory.UNKNOWN,
    expectedClassification: RiskClassification.SAFE,
    scanType: ScanType.MESSAGE_TEXT,
    iconName: "CheckCircle",
    description: "Normal transactional confirmation with zero financial hooks or threats.",
    syntheticContent: "City Health Clinic: Your dental check-up with Dr. Sharma is scheduled for tomorrow at 4:30 PM. Please arrive 10 minutes early. To reschedule, call clinic reception directly at 011-23456789."
  },
  {
    id: "clean_website",
    title: "Normal Institutional Portal",
    category: ThreatCategory.UNKNOWN,
    expectedClassification: RiskClassification.SAFE,
    scanType: ScanType.URL_DESTINATION,
    iconName: "Shield",
    description: "Standard verified institutional website with clean domain structure.",
    syntheticContent: "https://www.who.int/news-room/fact-sheets",
    destinationUrl: "https://www.who.int/news-room/fact-sheets"
  }
];
