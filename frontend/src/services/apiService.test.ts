import { describe, it, expect, vi, afterEach } from 'vitest';
import { setTopology } from './apiService';

describe('setTopology', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('PATCHes the topology endpoint with only the changed fields', async () => {
    const fetchMock = vi.fn().mockResolvedValue({ ok: true });
    vi.stubGlobal('fetch', fetchMock);

    await setTopology('st-42', { blocked: true });

    expect(fetchMock).toHaveBeenCalledTimes(1);
    const call = fetchMock.mock.calls[0]!;
    const url = call[0] as string;
    const init = call[1] as { method: string; body: string };
    expect(url).toMatch(/\/streets\/st-42\/topology$/);
    expect(init.method).toBe('PATCH');
    expect(JSON.parse(init.body)).toEqual({ blocked: true });
  });

  it('throws when the response is not ok', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: false, status: 500 }));

    await expect(setTopology('st-1', { oneway: false })).rejects.toThrow(/500/);
  });
});
