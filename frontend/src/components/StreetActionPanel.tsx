import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useTrafficStore } from '../store/trafficStore';
import { addTrafficLight, injectFlow, releaseFlow, setTopology } from '../services/apiService';
import { BLOCKED_HEX, CONGESTION_HEX } from '../types/traffic';
import {
  ArrowBothIcon,
  ArrowRightIcon,
  BlockIcon,
  RemoveSourceIcon,
  ReopenIcon,
  SourceIcon,
  TrafficLightIcon,
} from './icons';

/** Action panel for the selected street. Sends commands and waits for the SSE update. */
export function StreetActionPanel() {
  const { t } = useTranslation();
  const selectedId = useTrafficStore((s) => s.selectedStreetId);
  const street = useTrafficStore((s) => (selectedId ? s.streets[selectedId] : undefined));
  const close = useTrafficStore((s) => s.selectStreet);
  const origin = useTrafficStore((s) => s.origin);
  const destination = useTrafficStore((s) => s.destination);
  const setRouteEndpoint = useTrafficStore((s) => s.setRouteEndpoint);

  const [pending, setPending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!selectedId) {
    return null;
  }

  const level = street?.congestionLevel ?? 'FREE';
  const name = street?.name ?? selectedId;
  const oneway = street?.oneway ?? false;
  const blocked = street?.blocked ?? false;
  const isSource = street?.source ?? false;

  async function run(action: () => Promise<void>) {
    setPending(true);
    setError(null);
    try {
      await action();
    } catch {
      setError(t('panel.error'));
    } finally {
      setPending(false);
    }
  }

  return (
    <div className="w-80 rounded-2xl border border-white/10 bg-neutral-900/85 p-5 shadow-2xl shadow-black/60 backdrop-blur-xl">
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-[11px] font-semibold uppercase tracking-widest text-neutral-500">{t('panel.street')}</p>
          <h2 className="mt-0.5 text-base font-semibold leading-tight text-white">{name}</h2>
        </div>
        <button
          onClick={() => close(null)}
          className="rounded-lg p-1 text-neutral-500 transition hover:bg-white/5 hover:text-white"
          aria-label={t('panel.close')}
        >
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M18 6 6 18M6 6l12 12" strokeLinecap="round" />
          </svg>
        </button>
      </div>

      <div className="mt-4 flex items-center gap-2 rounded-xl border border-white/5 bg-white/5 px-3 py-2.5">
        <span
          className="h-3 w-3 rounded-full"
          style={{
            backgroundColor: blocked ? BLOCKED_HEX : CONGESTION_HEX[level],
            boxShadow: blocked ? 'none' : `0 0 10px ${CONGESTION_HEX[level]}`,
          }}
        />
        <span className="text-sm font-medium text-white">
          {blocked ? t('legend.blocked') : t(`congestion.${level}`)}
        </span>
        {street && !blocked && (
          <span className="ml-auto text-xs text-neutral-400">
            {t('panel.metrics', {
              volume: street.currentVolume,
              capacity: street.effectiveCapacity,
              percent: (street.ratio * 100).toFixed(0),
            })}
          </span>
        )}
      </div>

      <div className="mt-4">
        <p className="mb-1.5 text-[11px] font-semibold uppercase tracking-widest text-neutral-500">
          {t('route.title')}
        </p>
        <div className="grid grid-cols-2 gap-2">
          <button
            onClick={() => setRouteEndpoint('origin', selectedId)}
            className={`rounded-xl border px-3 py-2 text-sm font-medium transition ${
              origin === selectedId
                ? 'border-white/30 bg-white/15 text-white'
                : 'border-white/10 bg-white/5 text-neutral-200 hover:bg-white/10'
            }`}
          >
            {t('route.setOrigin')}
          </button>
          <button
            onClick={() => setRouteEndpoint('destination', selectedId)}
            className={`rounded-xl border px-3 py-2 text-sm font-medium transition ${
              destination === selectedId
                ? 'border-white/30 bg-white/15 text-white'
                : 'border-white/10 bg-white/5 text-neutral-200 hover:bg-white/10'
            }`}
          >
            {t('route.setDestination')}
          </button>
        </div>
      </div>

      <div className="mt-5 flex flex-col gap-2">
        <button
          disabled={pending}
          onClick={() => run(() => addTrafficLight(selectedId, 0.5))}
          className="flex items-center justify-center gap-2 rounded-xl bg-amber-500/90 px-4 py-2.5 text-sm font-semibold text-neutral-950 transition hover:bg-amber-400 disabled:opacity-50"
        >
          <TrafficLightIcon className="h-4 w-4" /> {t('panel.addTrafficLight')}
        </button>

        <div>
          <p className="mb-1.5 text-[11px] font-semibold uppercase tracking-widest text-neutral-500">
            {t('panel.addFlow')}
          </p>
          <div className="grid grid-cols-2 gap-2">
            <button
              disabled={pending}
              onClick={() => run(() => injectFlow(selectedId, 500))}
              className="rounded-xl border border-white/10 bg-white/5 px-3 py-2.5 text-sm font-medium text-neutral-200 transition hover:bg-white/10 disabled:opacity-50"
            >
              {t('panel.injectVehicles', { n: 500 })}
            </button>
            <button
              disabled={pending}
              onClick={() => run(() => injectFlow(selectedId, 1500))}
              className="rounded-xl border border-white/10 bg-white/5 px-3 py-2.5 text-sm font-medium text-neutral-200 transition hover:bg-white/10 disabled:opacity-50"
            >
              {t('panel.injectVehicles', { n: 1500 })}
            </button>
          </div>
        </div>

        <div>
          <p className="mb-1.5 text-[11px] font-semibold uppercase tracking-widest text-neutral-500">
            {t('panel.removeFlow')}
          </p>
          <div className="grid grid-cols-2 gap-2">
            <button
              disabled={pending}
              onClick={() => run(() => releaseFlow(selectedId, 500))}
              className="rounded-xl border border-white/10 bg-white/5 px-3 py-2.5 text-sm font-medium text-neutral-200 transition hover:bg-white/10 disabled:opacity-50"
            >
              {t('panel.releaseVehicles', { n: 500 })}
            </button>
            <button
              disabled={pending}
              onClick={() => run(() => releaseFlow(selectedId, 1500))}
              className="rounded-xl border border-white/10 bg-white/5 px-3 py-2.5 text-sm font-medium text-neutral-200 transition hover:bg-white/10 disabled:opacity-50"
            >
              {t('panel.releaseVehicles', { n: 1500 })}
            </button>
          </div>
        </div>
      </div>

      <div className="mt-5 border-t border-white/5 pt-4">
        <p className="mb-1.5 text-[11px] font-semibold uppercase tracking-widest text-neutral-500">
          {t('panel.topology')}
        </p>
        <div className="flex flex-col gap-2">
          <button
            disabled={pending}
            onClick={() => run(() => setTopology(selectedId, { oneway: !oneway }))}
            className="flex items-center justify-between rounded-xl border border-white/10 bg-white/5 px-3 py-2.5 text-sm font-medium text-neutral-200 transition hover:bg-white/10 disabled:opacity-50"
          >
            <span>{oneway ? t('panel.makeTwoWay') : t('panel.makeOneWay')}</span>
            <span className="text-neutral-500">
              {oneway ? <ArrowRightIcon className="h-4 w-4" /> : <ArrowBothIcon className="h-4 w-4" />}
            </span>
          </button>
          <button
            disabled={pending}
            onClick={() => run(() => setTopology(selectedId, { blocked: !blocked }))}
            className={`flex items-center justify-between rounded-xl border px-3 py-2.5 text-sm font-semibold transition disabled:opacity-50 ${
              blocked
                ? 'border-emerald-500/30 bg-emerald-500/15 text-emerald-300 hover:bg-emerald-500/25'
                : 'border-red-500/30 bg-red-500/15 text-red-300 hover:bg-red-500/25'
            }`}
          >
            <span>{blocked ? t('panel.reopenStreet') : t('panel.blockStreet')}</span>
            {blocked ? <ReopenIcon className="h-4 w-4" /> : <BlockIcon className="h-4 w-4" />}
          </button>
          <button
            disabled={pending}
            onClick={() => run(() => setTopology(selectedId, { source: !isSource }))}
            className="flex items-center justify-between rounded-xl border border-white/10 bg-white/5 px-3 py-2.5 text-sm font-medium text-neutral-200 transition hover:bg-white/10 disabled:opacity-50"
          >
            <span>{isSource ? t('panel.unmarkSource') : t('panel.markSource')}</span>
            {isSource ? <RemoveSourceIcon className="h-4 w-4" /> : <SourceIcon className="h-4 w-4" />}
          </button>
        </div>
      </div>

      <p className="mt-4 text-[11px] leading-relaxed text-neutral-500">{t('panel.hint')}</p>
      {error && <p className="mt-2 text-xs font-medium text-red-400">{error}</p>}
    </div>
  );
}
