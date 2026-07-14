import { describe, it, expect, vi } from 'vitest';
import { useTrafficStore } from './trafficStore';
import type { StreetStateView } from '../types/traffic';

// Keep refreshRoute from hitting the network in these state-only tests.
vi.mock('../services/routeService', () => ({
  fetchRoute: vi.fn().mockResolvedValue({ found: false, streets: [], nodes: [], totalCost: 0 }),
  fetchRouteBetweenStreets: vi.fn().mockResolvedValue({ found: false, streets: [], nodes: [], totalCost: 0 }),
}));

const jammed: StreetStateView = {
  id: 'st-x',
  name: 'X',
  currentVolume: 100,
  effectiveCapacity: 100,
  ratio: 1,
  congestionLevel: 'JAMMED',
  color: 'Red',
  oneway: false,
  blocked: false,
  source: false,
};

describe('trafficStore', () => {
  it('levelOf defaults to FREE for an unknown street', () => {
    expect(useTrafficStore.getState().levelOf('does-not-exist')).toBe('FREE');
  });

  it('levelOf returns the stored level for a known street', () => {
    useTrafficStore.setState({ streets: { 'st-x': jammed } });
    expect(useTrafficStore.getState().levelOf('st-x')).toBe('JAMMED');
  });

  it('setRouteEndpoint records origin and destination; clearRoute resets them', () => {
    const s = useTrafficStore.getState();
    s.setRouteEndpoint('origin', 'st-a');
    s.setRouteEndpoint('destination', 'st-b');
    expect(useTrafficStore.getState().origin).toBe('st-a');
    expect(useTrafficStore.getState().destination).toBe('st-b');

    useTrafficStore.getState().clearRoute();
    expect(useTrafficStore.getState().origin).toBeNull();
    expect(useTrafficStore.getState().destination).toBeNull();
  });
});
