// Domain contracts mirroring the traffic-state-service backend. No `any` allowed.

export type CongestionLevel = 'FREE' | 'HEAVY' | 'JAMMED';

/** Payload pushed by the backend SSE stream (`street-update` events) and REST snapshot. */
export interface StreetStateView {
  id: string;
  name: string;
  currentVolume: number;
  effectiveCapacity: number;
  ratio: number;
  congestionLevel: CongestionLevel;
  color: string;
}

/** Static properties carried by each street feature in the local GeoJSON. */
export interface StreetFeatureProperties {
  id: string;
  name: string;
  congestionLevel: CongestionLevel;
}

export type StreetFeature = GeoJSON.Feature<GeoJSON.LineString, StreetFeatureProperties>;
export type StreetFeatureCollection = GeoJSON.FeatureCollection<GeoJSON.LineString, StreetFeatureProperties>;

/** RGBA color per congestion level, used by the deck.gl line layer. */
export const CONGESTION_COLORS: Record<CongestionLevel, [number, number, number, number]> = {
  FREE: [34, 197, 94, 255],
  HEAVY: [234, 179, 8, 255],
  JAMMED: [239, 68, 68, 255],
};

/** Tailwind-friendly hex per level for UI chips/legend. */
export const CONGESTION_HEX: Record<CongestionLevel, string> = {
  FREE: '#22c55e',
  HEAVY: '#eab308',
  JAMMED: '#ef4444',
};
