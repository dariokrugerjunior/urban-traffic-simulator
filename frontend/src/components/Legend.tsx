import { useTranslation } from 'react-i18next';
import { BLOCKED_HEX, CONGESTION_HEX, type CongestionLevel } from '../types/traffic';

const LEVELS: CongestionLevel[] = ['FREE', 'HEAVY', 'JAMMED'];

/** Color legend mapping congestion levels to the map colors. */
export function Legend() {
  const { t } = useTranslation();

  return (
    <div className="rounded-2xl border border-white/10 bg-neutral-900/80 px-4 py-3 shadow-xl shadow-black/40 backdrop-blur-md">
      <p className="mb-2 text-[11px] font-semibold uppercase tracking-widest text-neutral-500">
        {t('legend.title')}
      </p>
      <div className="flex flex-col gap-1.5">
        {LEVELS.map((level) => (
          <div key={level} className="flex items-center gap-2.5">
            <span
              className="h-2.5 w-6 rounded-full"
              style={{ backgroundColor: CONGESTION_HEX[level], boxShadow: `0 0 10px ${CONGESTION_HEX[level]}55` }}
            />
            <span className="text-xs font-medium text-neutral-300">{t(`congestion.${level}`)}</span>
          </div>
        ))}
        <div className="flex items-center gap-2.5">
          <span
            className="h-2.5 w-6 rounded-full"
            style={{ backgroundColor: BLOCKED_HEX }}
          />
          <span className="text-xs font-medium text-neutral-300">{t('legend.blocked')}</span>
        </div>
        <div className="flex items-center gap-2.5">
          <span className="flex h-2.5 w-6 items-center justify-center">
            <span className="h-2 w-2 rounded-full bg-amber-500 ring-1 ring-black/60" />
          </span>
          <span className="text-xs font-medium text-neutral-300">{t('legend.signal')}</span>
        </div>
      </div>
    </div>
  );
}
