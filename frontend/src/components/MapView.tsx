import { useCallback, useMemo, useState } from 'react';
import DeckGL from '@deck.gl/react';
import { GeoJsonLayer } from '@deck.gl/layers';
import type { PickingInfo } from '@deck.gl/core';
import Map from 'react-map-gl/maplibre';
import type { Feature, Geometry } from 'geojson';
import { INITIAL_VIEW_STATE, JOINVILLE_NETWORK, MAP_STYLE } from '../data/joinvilleNetwork';
import {
  CONGESTION_COLORS,
  type StreetFeature,
  type StreetFeatureCollection,
  type StreetFeatureProperties,
} from '../types/traffic';
import { useTrafficStore } from '../store/trafficStore';
import { StreetTooltip, type HoverInfo } from './StreetTooltip';

/** Deck.gl road-network layer over a MapLibre dark basemap, with hover picking + tooltip. */
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

  const layers = [
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

  return (
    <div className="relative h-full w-full">
      <DeckGL
        initialViewState={INITIAL_VIEW_STATE}
        controller
        layers={layers}
        onHover={onHover}
        onClick={onClick}
        getCursor={({ isDragging, isHovering }) =>
          isDragging ? 'grabbing' : isHovering ? 'pointer' : 'grab'
        }
      >
        <Map mapStyle={MAP_STYLE} />
      </DeckGL>
      {hover && <StreetTooltip hover={hover} />}
    </div>
  );
}
