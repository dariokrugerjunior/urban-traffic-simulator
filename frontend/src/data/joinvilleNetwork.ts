import type { StreetFeatureCollection } from '../types/traffic';

// Approximate real-world coordinates (lng, lat) of the seeded Joinville intersections.
// Node ids match the backend seed: I1=Centro, I2=Estacao, I3=America, I5=Saguacu.
const I1: [number, number] = [-48.846, -26.304]; // Centro
const I2: [number, number] = [-48.853, -26.301]; // Estacao
const I3: [number, number] = [-48.85, -26.293]; // America
const I5: [number, number] = [-48.838, -26.288]; // Saguacu

/** Static geometry of the road network. Congestion levels are updated live from SSE by id. */
export const JOINVILLE_NETWORK: StreetFeatureCollection = {
  type: 'FeatureCollection',
  features: [
    {
      type: 'Feature',
      properties: { id: 'st-beira-rio', name: 'Av. Hermann August Lepper (Beira-Rio)', congestionLevel: 'FREE' },
      geometry: { type: 'LineString', coordinates: [I1, I5] },
    },
    {
      type: 'Feature',
      properties: { id: 'st-joao-colin', name: 'Rua Joao Colin', congestionLevel: 'FREE' },
      geometry: { type: 'LineString', coordinates: [I1, I3] },
    },
    {
      type: 'Feature',
      properties: { id: 'st-dona-francisca', name: 'Rua Dona Francisca', congestionLevel: 'FREE' },
      geometry: { type: 'LineString', coordinates: [I3, I5] },
    },
    {
      type: 'Feature',
      properties: { id: 'st-nove-de-marco', name: 'Rua Nove de Marco', congestionLevel: 'FREE' },
      geometry: { type: 'LineString', coordinates: [I1, I2] },
    },
    {
      type: 'Feature',
      properties: { id: 'st-xv-de-novembro', name: 'Rua XV de Novembro', congestionLevel: 'FREE' },
      geometry: { type: 'LineString', coordinates: [I2, I3] },
    },
  ],
};

/** Initial camera framing the seeded network. */
export const INITIAL_VIEW_STATE = {
  longitude: -48.845,
  latitude: -26.296,
  zoom: 13.4,
  pitch: 0,
  bearing: 0,
};

/** Free CARTO dark basemap style — no API token required. */
export const MAP_STYLE = 'https://basemaps.cartocdn.com/gl/dark-matter-gl-style/style.json';
