import { describe, it, expect, vi, afterEach } from 'vitest';
import { fetchSignals } from './signalsService';

describe('fetchSignals', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('returns the parsed signal coordinates', async () => {
    const signals = [
      [-48.9, -26.3],
      [-48.85, -26.28],
    ];
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: true, json: async () => signals }));

    const result = await fetchSignals();

    expect(result).toEqual(signals);
  });

  it('throws when the response is not ok', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: false, status: 404 }));
    await expect(fetchSignals()).rejects.toThrow(/404/);
  });
});
