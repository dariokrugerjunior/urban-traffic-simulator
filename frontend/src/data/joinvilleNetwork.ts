import type { StreetFeatureCollection } from '../types/traffic';

// Multi-point traces (lng, lat) approximating the real path of each Joinville street,
// so the rendered lines curve with the road instead of cutting straight across blocks.
// Shared endpoints keep the network connected at the intersections
// I1=Centro, I2=Estacao, I3=America, I5=Saguacu.

const BEIRA_RIO: [number, number][] = [
  [-48.846, -26.304],
  [-48.8452, -26.3008],
  [-48.8443, -26.2976],
  [-48.8428, -26.2948],
  [-48.841, -26.292],
  [-48.8394, -26.2899],
  [-48.838, -26.288],
];

const JOAO_COLIN: [number, number][] = [
  [-48.846, -26.304],
  [-48.847, -26.3018],
  [-48.848, -26.2995],
  [-48.8488, -26.2972],
  [-48.8494, -26.295],
  [-48.85, -26.293],
];

const DONA_FRANCISCA: [number, number][] = [
  [-48.85, -26.293],
  [-48.8476, -26.2923],
  [-48.8451, -26.2913],
  [-48.8426, -26.2901],
  [-48.8402, -26.289],
  [-48.838, -26.288],
];

const NOVE_DE_MARCO: [number, number][] = [
  [-48.846, -26.304],
  [-48.8478, -26.3036],
  [-48.8497, -26.3028],
  [-48.8515, -26.3018],
  [-48.853, -26.301],
];

const XV_DE_NOVEMBRO: [number, number][] = [
  [-48.853, -26.301],
  [-48.8524, -26.2987],
  [-48.8517, -26.2965],
  [-48.851, -26.2948],
  [-48.85, -26.293],
];

/** Static geometry of the road network. Congestion levels are updated live from SSE by id. */
export const JOINVILLE_NETWORK: StreetFeatureCollection = {
  type: 'FeatureCollection',
  features: [
    {
      type: 'Feature',
      properties: { id: 'st-beira-rio', name: 'Av. Hermann August Lepper (Beira-Rio)', congestionLevel: 'FREE' },
      geometry: { type: 'LineString', coordinates: BEIRA_RIO },
    },
    {
      type: 'Feature',
      properties: { id: 'st-joao-colin', name: 'Rua Joao Colin', congestionLevel: 'FREE' },
      geometry: { type: 'LineString', coordinates: JOAO_COLIN },
    },
    {
      type: 'Feature',
      properties: { id: 'st-dona-francisca', name: 'Rua Dona Francisca', congestionLevel: 'FREE' },
      geometry: { type: 'LineString', coordinates: DONA_FRANCISCA },
    },
    {
      type: 'Feature',
      properties: { id: 'st-nove-de-marco', name: 'Rua Nove de Marco', congestionLevel: 'FREE' },
      geometry: { type: 'LineString', coordinates: NOVE_DE_MARCO },
    },
    {
      type: 'Feature',
      properties: { id: 'st-xv-de-novembro', name: 'Rua XV de Novembro', congestionLevel: 'FREE' },
      geometry: { type: 'LineString', coordinates: XV_DE_NOVEMBRO },
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
