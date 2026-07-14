import { useTranslation } from 'react-i18next';
import { useTrafficStore } from '../store/trafficStore';

const CONFIG = {
  connecting: { key: 'status.connecting', color: '#eab308' },
  live: { key: 'status.live', color: '#22c55e' },
  offline: { key: 'status.offline', color: '#ef4444' },
} as const;

/** Shows the SSE connection status with a pulsing indicator. */
export function ConnectionStatus() {
  const { t } = useTranslation();
  const status = useTrafficStore((s) => s.status);
  const { key, color } = CONFIG[status];
  const label = t(key);

  return (
    <div className="flex items-center gap-2 rounded-full border border-white/10 bg-neutral-900/80 px-3 py-1.5 shadow-lg shadow-black/40 backdrop-blur-md">
      <span className="relative flex h-2.5 w-2.5">
        {status === 'live' && (
          <span className="absolute inline-flex h-full w-full animate-ping rounded-full opacity-60" style={{ backgroundColor: color }} />
        )}
        <span className="relative inline-flex h-2.5 w-2.5 rounded-full" style={{ backgroundColor: color }} />
      </span>
      <span className="text-xs font-medium text-neutral-300">{label}</span>
    </div>
  );
}
