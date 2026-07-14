import { useCallback, useMemo, useState } from 'react';
import { GeoJsonLayer, PathLayer } from '@deck.gl/layers';
import { MapboxOverlay, type MapboxOverlayProps } from '@deck.gl/mapbox';
import type { PickingInfo } from '@deck.gl/core';
import Map, { useControl, type ErrorEvent } from 'react-map-gl/maplibre';
import type { Feature, Geometry } from 'geojson';
import { INITIAL_VIEW_STATE, JOINVILLE_NETWORK, MAP_STYLE } from '../data/joinvilleNetwork';
import { JOINVILLE_ROADS } from '../data/roads';
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
  const [hover, setHover] = useState<HoverInfo | null>(null);

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
    new PathLayer<[number, number][]>({
      id: 'road-network-base',
      data: JOINVILLE_ROADS,
      getPath: (d) => d,
      getColor: [124, 156, 201, 165],
      getWidth: 1.2,
      widthUnits: 'pixels',
      widthMinPixels: 0.7,
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
    </div>
  );
}
