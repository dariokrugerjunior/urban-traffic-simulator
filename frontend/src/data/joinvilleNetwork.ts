import type { StreetFeature, StreetFeatureCollection } from '../types/traffic';
import { ARTERIALS } from './arterials';

// Real street geometry from OpenStreetMap (same source as the CARTO basemap), so each
// colored line traces the actual road. Coordinates are [lng, lat], simplified
// (Douglas-Peucker) to keep the payload small while following the real path.
// Street ids match the backend seed.

const BEIRA_RIO: [number, number][] = [
  [-48.840905, -26.301732], [-48.841081, -26.301424], [-48.841648, -26.300881],
  [-48.841893, -26.300002], [-48.842088, -26.295728], [-48.84252, -26.294694],
  [-48.842566, -26.294057], [-48.84282, -26.285647], [-48.843384, -26.284772],
];

const JOAO_COLIN: [number, number][] = [
  [-48.847978, -26.301478], [-48.847669, -26.29756], [-48.847668, -26.297187],
  [-48.849144, -26.284197], [-48.850108, -26.274594],
];

const DONA_FRANCISCA: [number, number][] = [
  [-48.857868, -26.267893], [-48.854484, -26.26937], [-48.852999, -26.270127],
  [-48.852452, -26.270524], [-48.850506, -26.272465], [-48.849421, -26.272818],
  [-48.848895, -26.273261], [-48.848675, -26.274465], [-48.84654, -26.277103],
  [-48.846295, -26.277243], [-48.846338, -26.277345], [-48.845644, -26.279182],
  [-48.845126, -26.280155], [-48.843856, -26.281456], [-48.841468, -26.282831],
  [-48.841375, -26.283213], [-48.840983, -26.283322], [-48.840896, -26.283439],
  [-48.839844, -26.285745], [-48.839743, -26.286136], [-48.839864, -26.286571],
  [-48.840412, -26.287076],
];

const NOVE_DE_MARCO: [number, number][] = [
  [-48.850749, -26.301333], [-48.841078, -26.301836], [-48.840905, -26.301732],
];

const XV_DE_NOVEMBRO: [number, number][] = [
  [-48.842245, -26.300673], [-48.842371, -26.300866], [-48.844088, -26.3008],
  [-48.844166, -26.30071], [-48.844274, -26.300787], [-48.850683, -26.300431],
  [-48.851606, -26.299847], [-48.852199, -26.299585], [-48.853073, -26.298923],
  [-48.85688, -26.298499], [-48.858387, -26.298744], [-48.858704, -26.298653],
  [-48.859404, -26.298208], [-48.860325, -26.297878],
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
      properties: { id: 'st-joao-colin', name: 'Rua Doutor Joao Colin', congestionLevel: 'FREE' },
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
    // Additional arterials (real OSM geometry) — also simulated and clickable.
    ...ARTERIALS.map(
      (arterial): StreetFeature => ({
        type: 'Feature',
        properties: { id: arterial.id, name: arterial.name, congestionLevel: 'FREE' },
        geometry: { type: 'LineString', coordinates: arterial.coords },
      }),
    ),
  ],
};

/** Lookup of each simulated street's geometry by id, used to draw the computed route. */
export const STREET_GEOMETRY_BY_ID: Record<string, [number, number][]> = Object.fromEntries(
  JOINVILLE_NETWORK.features.map((feature) => [
    feature.properties.id,
    feature.geometry.coordinates as [number, number][],
  ]),
);

/** Lookup of each simulated street's display name by id. */
export const STREET_NAME_BY_ID: Record<string, string> = Object.fromEntries(
  JOINVILLE_NETWORK.features.map((feature) => [feature.properties.id, feature.properties.name]),
);

/** Initial camera framing the city network, with the simulated streets still prominent. */
export const INITIAL_VIEW_STATE = {
  longitude: -48.846,
  latitude: -26.287,
  zoom: 12.6,
  pitch: 0,
  bearing: 0,
};

/** Free CARTO dark basemap style — no API token required. */
export const MAP_STYLE = 'https://basemaps.cartocdn.com/gl/dark-matter-gl-style/style.json';
