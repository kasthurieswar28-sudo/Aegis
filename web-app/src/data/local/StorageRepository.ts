import { ScanResult } from '../../domain/models';

const HISTORY_KEY = 'aegis_scan_history';

export class StorageRepository {
  private static instance: StorageRepository;
  private listeners: ((history: ScanResult[]) => void)[] = [];

  public static getInstance(): StorageRepository {
    if (!StorageRepository.instance) {
      StorageRepository.instance = new StorageRepository();
    }
    return StorageRepository.instance;
  }

  public getAllHistory(): ScanResult[] {
    try {
      const data = localStorage.getItem(HISTORY_KEY);
      if (!data) return [];
      const parsed: ScanResult[] = JSON.parse(data);
      return Array.isArray(parsed) ? parsed.sort((a, b) => b.timestamp - a.timestamp) : [];
    } catch {
      return [];
    }
  }

  public getRecentHistory(limit: number = 5): ScanResult[] {
    return this.getAllHistory().slice(0, limit);
  }

  public saveScan(scan: ScanResult): ScanResult {
    const list = this.getAllHistory();
    const existingIndex = list.findIndex(item => item.id === scan.id);
    let updated: ScanResult[];

    if (existingIndex >= 0) {
      list[existingIndex] = scan;
      updated = [...list];
    } else {
      const scanWithId = {
        ...scan,
        id: scan.id || `scan_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`
      };
      updated = [scanWithId, ...list];
    }

    try {
      localStorage.setItem(HISTORY_KEY, JSON.stringify(updated));
    } catch (e) {
      console.error('Failed to save scan to localStorage:', e);
    }
    this.notify(updated);
    return scan;
  }

  public deleteScan(id: string | number): void {
    const list = this.getAllHistory();
    const filtered = list.filter(item => item.id !== id);
    try {
      localStorage.setItem(HISTORY_KEY, JSON.stringify(filtered));
    } catch {}
    this.notify(filtered);
  }

  public clearHistory(): void {
    try {
      localStorage.removeItem(HISTORY_KEY);
    } catch {}
    this.notify([]);
  }

  public subscribe(listener: (history: ScanResult[]) => void): () => void {
    this.listeners.push(listener);
    listener(this.getAllHistory());
    return () => {
      this.listeners = this.listeners.filter(l => l !== listener);
    };
  }

  private notify(history: ScanResult[]) {
    for (const l of this.listeners) {
      l(history);
    }
  }
}
