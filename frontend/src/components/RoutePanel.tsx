import { useTranslation } from 'react-i18next';
import { ROUTE_END, ROUTE_START, useTrafficStore } from '../store/trafficStore';
import { STREET_NAME_BY_ID } from '../data/joinvilleNetwork';

/** Live GPS route from the routing-service: the demo I1 → I5, or the user's picked streets. */
export function RoutePanel() {
  const { t } = useTranslation();
  const route = useTrafficStore((s) => s.route);
  const origin = useTrafficStore((s) => s.origin);
  const destination = useTrafficStore((s) => s.destination);
  const streets = useTrafficStore((s) => s.streets);
  const clearRoute = useTrafficStore((s) => s.clearRoute);

  if (!route) {
    return null;
  }

  const isUserRoute = Boolean(origin && destination);
  // Street ids may be corridor (local data) or real city edges (backend snapshot).
  const nameOf = (id: string) => STREET_NAME_BY_ID[id] ?? streets[id]?.name ?? id;
  const heading = isUserRoute
    ? `${origin ? nameOf(origin) : '—'} → ${destination ? nameOf(destination) : '—'}`
    : `${ROUTE_START} → ${ROUTE_END}`;

  return (
    <div className="w-64 rounded-2xl border border-white/10 bg-neutral-900/80 px-4 py-3 shadow-xl shadow-black/40 backdrop-blur-md">
      <div className="flex items-center justify-between gap-2">
        <p className="text-[11px] font-semibold uppercase tracking-widest text-neutral-500">
          {t('route.title')}
        </p>
        {isUserRoute && (
          <button
            onClick={clearRoute}
            className="text-[11px] font-medium text-neutral-400 transition hover:text-white"
          >
            {t('route.clear')}
          </button>
        )}
      </div>

      <p className="mt-1 truncate text-xs font-semibold text-white" title={heading}>
        {heading}
      </p>

      {route.found ? (
        <>
          <div className="mt-2 flex max-h-48 flex-col gap-1 overflow-y-auto">
            {route.streets.map((id, i) => (
              <div key={`${id}-${i}`} className="flex items-center gap-2">
                <span className="h-1.5 w-1.5 shrink-0 rounded-full bg-white/80" />
                <span className="truncate text-xs text-neutral-200">{nameOf(id)}</span>
              </div>
            ))}
          </div>
          <p className="mt-2 text-[11px] text-neutral-500">
            {t('route.cost', { cost: Math.round(route.totalCost) })}
          </p>
        </>
      ) : (
        <p className="mt-2 text-xs text-neutral-400">{t('route.none')}</p>
      )}
    </div>
  );
}
