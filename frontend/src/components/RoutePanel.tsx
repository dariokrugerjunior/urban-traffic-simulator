import { useTranslation } from 'react-i18next';
import { ROUTE_END, ROUTE_START, useTrafficStore } from '../store/trafficStore';
import { STREET_NAME_BY_ID } from '../data/joinvilleNetwork';

/** Shows the live GPS route (I1 → I5) computed by the routing-service. */
export function RoutePanel() {
  const { t } = useTranslation();
  const route = useTrafficStore((s) => s.route);

  if (!route) {
    return null;
  }

  return (
    <div className="w-64 rounded-2xl border border-white/10 bg-neutral-900/80 px-4 py-3 shadow-xl shadow-black/40 backdrop-blur-md">
      <div className="flex items-center justify-between">
        <p className="text-[11px] font-semibold uppercase tracking-widest text-neutral-500">
          {t('route.title')}
        </p>
        <span className="text-xs font-semibold text-white">
          {ROUTE_START} → {ROUTE_END}
        </span>
      </div>

      {route.found ? (
        <>
          <div className="mt-2 flex flex-col gap-1">
            {route.streets.map((id) => (
              <div key={id} className="flex items-center gap-2">
                <span className="h-1.5 w-1.5 rounded-full bg-white/80" />
                <span className="text-xs text-neutral-200">{STREET_NAME_BY_ID[id] ?? id}</span>
              </div>
            ))}
          </div>
          <p className="mt-2 text-[11px] text-neutral-500">
            {t('route.cost', { cost: route.totalCost })}
          </p>
        </>
      ) : (
        <p className="mt-2 text-xs text-neutral-400">{t('route.none')}</p>
      )}
    </div>
  );
}
