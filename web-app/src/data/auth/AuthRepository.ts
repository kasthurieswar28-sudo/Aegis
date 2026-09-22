import { AuthProviderType, AuthUser } from '../../domain/models';

const AUTH_STORAGE_KEY = 'aegis_auth_user';

export class AuthRepository {
  private static instance: AuthRepository;
  private user: AuthUser | null = null;
  private listeners: ((user: AuthUser | null) => void)[] = [];

  private constructor() {
    this.loadSession();
  }

  public static getInstance(): AuthRepository {
    if (!AuthRepository.instance) {
      AuthRepository.instance = new AuthRepository();
    }
    return AuthRepository.instance;
  }

  private loadSession() {
    try {
      const stored = localStorage.getItem(AUTH_STORAGE_KEY);
      if (stored) {
        this.user = JSON.parse(stored);
      }
    } catch {
      this.user = null;
    }
  }

  public getCurrentUser(): AuthUser | null {
    return this.user;
  }

  public subscribe(listener: (user: AuthUser | null) => void): () => void {
    this.listeners.push(listener);
    listener(this.user);
    return () => {
      this.listeners = this.listeners.filter(l => l !== listener);
    };
  }

  private notify() {
    for (const l of this.listeners) {
      l(this.user);
    }
  }

  public async signInAsGuest(): Promise<AuthUser> {
    const guestUser: AuthUser = {
      uid: `guest_${Date.now()}_${Math.random().toString(36).substring(2, 9)}`,
      displayName: 'Authenticated Analyst',
      email: null,
      isAnonymous: true,
      provider: AuthProviderType.GUEST,
      createdAt: Date.now()
    };

    this.user = guestUser;
    try {
      localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(guestUser));
    } catch {}
    this.notify();
    return guestUser;
  }

  public signOut() {
    this.user = null;
    try {
      localStorage.removeItem(AUTH_STORAGE_KEY);
    } catch {}
    this.notify();
  }
}
