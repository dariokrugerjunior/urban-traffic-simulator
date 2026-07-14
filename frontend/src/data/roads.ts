import data from './joinvilleRoads.json';

// Compact array of polylines ([lng, lat][]) for Joinville's full drivable road network,
// sourced from OpenStreetMap and simplified. Rendered as a neutral base layer under the
// simulated streets. Kept as raw paths (not GeoJSON) for a smaller payload + PathLayer.
export const JOINVILLE_ROADS = data as unknown as [number, number][][];
