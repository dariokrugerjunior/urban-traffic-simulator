import { useTranslation } from 'react-i18next';
import { useTrafficStore } from '../store/trafficStore';
import { CONGESTION_HEX } from '../types/traffic';

export interface HoverInfo {
  id: string;
  name: string;
  x: number;
  y: number;
}

/** Custom floating tooltip following the cursor while hovering a street. */
export function StreetTooltip({ hover }: { hover: HoverInfo }) {
  const { t } = useTranslation();
  const state = useTrafficStore((s) => s.streets[hover.id]);
  const level = state?.congestionLevel ?? 'FREE';

  return (
    <div
      className="pointer-events-none absolute z-20 -translate-y-full -translate-x-1/2 transition-transform duration-75 ease-out"
      style={{ left: hover.x, top: hover.y - 12 }}
    >
      <div className="rounded-xl border border-white/10 bg-neutral-900/90 px-4 py-3 shadow-2xl shadow-black/60 backdrop-blur-md">
        <p className="text-sm font-semibold text-white">{hover.name}</p>
        <div className="mt-1.5 flex items-center gap-2">
          <span
            className="h-2.5 w-2.5 rounded-full"
            style={{ backgroundColor: CONGESTION_HEX[level], boxShadow: `0 0 8px ${CONGESTION_HEX[level]}` }}
          />
          <span className="text-xs font-medium text-neutral-300">{t(`congestion.${level}`)}</span>
          {state && (
            <span className="text-xs text-neutral-500">
              {t('tooltip.metrics', { volume: state.currentVolume, capacity: state.effectiveCapacity })}
            </span>
          )}
        </div>
      </div>
      <div className="mx-auto h-2 w-2 -translate-y-1 rotate-45 border-b border-r border-white/10 bg-neutral-900/90" />
    </div>
  );
}
