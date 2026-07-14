import { useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { MapView } from './components/MapView';
import { Legend } from './components/Legend';
import { ConnectionStatus } from './components/ConnectionStatus';
import { StreetActionPanel } from './components/StreetActionPanel';
import { LanguageSwitcher } from './components/LanguageSwitcher';
import { RoutePanel } from './components/RoutePanel';
import { useTrafficStore } from './store/trafficStore';

export default function App() {
  const { t } = useTranslation();
  const connect = useTrafficStore((s) => s.connect);

  // Open the SSE stream exactly once for the app lifetime.
  useEffect(() => connect(), [connect]);

  return (
    <div className="relative h-screen w-screen overflow-hidden">
      <MapView />

      {/* Header */}
      <header className="pointer-events-none absolute inset-x-0 top-0 z-10 flex items-start justify-between p-5">
        <div className="pointer-events-auto rounded-2xl border border-white/10 bg-neutral-900/70 px-4 py-3 shadow-xl shadow-black/40 backdrop-blur-md">
          <h1 className="text-sm font-semibold tracking-tight text-white">{t('app.title')}</h1>
          <p className="text-[11px] text-neutral-400">{t('app.subtitle')}</p>
        </div>
        <div className="pointer-events-auto flex items-center gap-2">
          <LanguageSwitcher />
          <ConnectionStatus />
        </div>
      </header>

      {/* Live GPS route */}
      <div className="absolute left-5 top-24 z-10">
        <RoutePanel />
      </div>

      {/* Legend */}
      <div className="absolute bottom-5 left-5 z-10">
        <Legend />
      </div>

      {/* Action panel */}
      <div className="absolute bottom-5 right-5 z-10">
        <StreetActionPanel />
      </div>
    </div>
  );
}
