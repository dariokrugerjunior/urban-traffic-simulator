import { describe, it, expect, vi, afterEach } from 'vitest';
import { fetchRouteBetweenStreets } from './routeService';

describe('fetchRouteBetweenStreets', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('queries the /between endpoint with both street ids', async () => {
    const view = { found: true, streets: ['st-mid'], nodes: ['nB', 'nC'], totalCost: 120 };
    const fetchMock = vi.fn().mockResolvedValue({ ok: true, json: async () => view });
    vi.stubGlobal('fetch', fetchMock);

    const result = await fetchRouteBetweenStreets('st-a', 'st-b');

    const url = fetchMock.mock.calls[0]![0] as string;
    expect(url).toMatch(/\/between\?fromStreet=st-a&toStreet=st-b$/);
    expect(result).toEqual(view);
  });

  it('throws when the response is not ok', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: false, status: 404 }));
    await expect(fetchRouteBetweenStreets('a', 'b')).rejects.toThrow(/404/);
  });
});
