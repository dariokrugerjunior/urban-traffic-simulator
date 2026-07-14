import { useCallback, useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { GeoJsonLayer, PathLayer } from '@deck.gl/layers';
import { MapboxOverlay, type MapboxOverlayProps } from '@deck.gl/mapbox';
import type { PickingInfo } from '@deck.gl/core';
import Map, { useControl, type ErrorEvent } from 'react-map-gl/maplibre';
import type { Feature, Geometry } from 'geojson';
import {
  INITIAL_VIEW_STATE,
  JOINVILLE_NETWORK,
  MAP_STYLE,
  STREET_GEOMETRY_BY_ID,
} from '../data/joinvilleNetwork';
import { fetchRoadNetwork, type RoadPath } from '../services/roadNetworkService';
import {
  CONGESTION_COLORS,
  type StreetFeature,
  type StreetFeatureCollection,
  type StreetFeatureProperties,
} from '../types/traffic';
import { useTrafficStore } from '../store/trafficStore';
import { StreetTooltip, type HoverInfo } from './StreetTooltip';

/** Adds a deck.gl overlay on top of the MapLibre base map (overlaid mode keeps the map visible). */
function DeckGLOverlay(props: MapboxOverlayProps) {
  const overlay = useControl<MapboxOverlay>(() => new MapboxOverlay(props));
  overlay.setProps(props);
  return null;
}

/** MapLibre dark basemap with a deck.gl road-network layer, hover picking + custom tooltip. */
export function MapView() {
  const streets = useTrafficStore((s) => s.streets);
  const selectStreet = useTrafficStore((s) => s.selectStreet);
  const route = useTrafficStore((s) => s.route);
  const { t } = useTranslation();
  const [hover, setHover] = useState<HoverInfo | null>(null);
  const [roads, setRoads] = useState<RoadPath[]>([]);
  const [networkLoading, setNetworkLoading] = useState(true);

  // Geometry of the streets that make up the current computed route.
  const routePaths = useMemo<RoadPath[]>(
    () => (route?.streets ?? []).map((id) => STREET_GEOMETRY_BY_ID[id]).filter((p): p is RoadPath => Boolean(p)),
    [route],
  );

  // Load the full road network once from the static asset in /public.
  useEffect(() => {
    fetchRoadNetwork()
      .then(setRoads)
      .catch((error) => console.error('[MAP]', error))
      .finally(() => setNetworkLoading(false));
  }, []);

  // Merge live congestion levels into the static geometry.
  const geojson = useMemo<StreetFeatureCollection>(
    () => ({
      type: 'FeatureCollection',
      features: JOINVILLE_NETWORK.features.map((feature) => ({
        ...feature,
        properties: {
          ...feature.properties,
          congestionLevel: streets[feature.properties.id]?.congestionLevel ?? 'FREE',
        },
      })),
    }),
    [streets],
  );

  const colorKey = geojson.features.map((f) => f.properties.congestionLevel).join('|');

  const onHover = useCallback((info: PickingInfo<StreetFeature>) => {
    if (info.object) {
      setHover({
        id: info.object.properties.id,
        name: info.object.properties.name,
        x: info.x,
        y: info.y,
      });
    } else {
      setHover(null);
    }
  }, []);

  const onClick = useCallback(
    (info: PickingInfo<StreetFeature>) => {
      if (info.object) {
        selectStreet(info.object.properties.id);
      }
    },
    [selectStreet],
  );

  const layers = [
    // Full Joinville road network as a neutral base layer, under the simulated streets.
    new PathLayer<RoadPath>({
      id: 'road-network-base',
      data: roads,
      getPath: (d) => d,
      getColor: [124, 156, 201, 165],
      getWidth: 1.2,
      widthUnits: 'pixels',
      widthMinPixels: 0.7,
      capRounded: true,
      jointRounded: true,
      pickable: false,
    }),
    // Highlight halo under the streets that form the current GPS route (I1 → I5).
    new PathLayer<RoadPath>({
      id: 'route-highlight',
      data: routePaths,
      getPath: (d) => d,
      getColor: [255, 255, 255, 150],
      getWidth: 12,
      widthUnits: 'pixels',
      widthMinPixels: 8,
      capRounded: true,
      jointRounded: true,
      pickable: false,
    }),
    new GeoJsonLayer<StreetFeatureProperties>({
      id: 'road-network',
      data: geojson,
      pickable: true,
      stroked: true,
      filled: false,
      lineWidthUnits: 'pixels',
      getLineWidth: 5,
      lineWidthMinPixels: 4,
      lineCapRounded: true,
      lineJointRounded: true,
      getLineColor: (f: Feature<Geometry, StreetFeatureProperties>) =>
        CONGESTION_COLORS[f.properties.congestionLevel],
      updateTriggers: { getLineColor: colorKey },
    }),
  ];

  return (
    <div className="absolute inset-0">
      <Map
        initialViewState={INITIAL_VIEW_STATE}
        mapStyle={MAP_STYLE}
        style={{ width: '100%', height: '100%' }}
        onLoad={() => console.info('[MAP] base map loaded and rendering tiles')}
        onError={(e: ErrorEvent) => console.error('[MAP] base map error:', e.error)}
      >
        <DeckGLOverlay
          layers={layers}
          onHover={onHover}
          onClick={onClick}
          getCursor={({ isDragging, isHovering }) =>
            isDragging ? 'grabbing' : isHovering ? 'pointer' : 'grab'
          }
        />
      </Map>
      {hover && <StreetTooltip hover={hover} />}
      {networkLoading && (
        <div className="pointer-events-none absolute bottom-5 left-1/2 -translate-x-1/2 rounded-full border border-white/10 bg-neutral-900/80 px-4 py-2 shadow-lg shadow-black/40 backdrop-blur-md">
          <div className="flex items-center gap-2">
            <span className="h-3 w-3 animate-spin rounded-full border-2 border-white/20 border-t-white/80" />
            <span className="text-xs font-medium text-neutral-300">{t('map.loadingNetwork')}</span>
          </div>
        </div>
      )}
    </div>
  );
}
