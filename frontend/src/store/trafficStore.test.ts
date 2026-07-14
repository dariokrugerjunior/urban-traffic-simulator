import { describe, it, expect } from 'vitest';
import { useTrafficStore } from './trafficStore';
import type { StreetStateView } from '../types/traffic';

const jammed: StreetStateView = {
  id: 'st-x',
  name: 'X',
  currentVolume: 100,
  effectiveCapacity: 100,
  ratio: 1,
  congestionLevel: 'JAMMED',
  color: 'Red',
};

describe('trafficStore', () => {
  it('levelOf defaults to FREE for an unknown street', () => {
    expect(useTrafficStore.getState().levelOf('does-not-exist')).toBe('FREE');
  });

  it('levelOf returns the stored level for a known street', () => {
    useTrafficStore.setState({ streets: { 'st-x': jammed } });
    expect(useTrafficStore.getState().levelOf('st-x')).toBe('JAMMED');
  });
});
