import { describe, it, expect, vi, afterEach } from 'vitest';
import { fetchRoadNetwork } from './roadNetworkService';

describe('fetchRoadNetwork', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('returns the parsed road paths on a successful response', async () => {
    const paths = [
      [
        [-48.8, -26.3],
        [-48.81, -26.31],
      ],
    ];
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: true, json: async () => paths }));

    const result = await fetchRoadNetwork();

    expect(result).toEqual(paths);
  });

  it('throws when the response is not ok', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: false, status: 404 }));

    await expect(fetchRoadNetwork()).rejects.toThrow(/404/);
  });
});
