import { useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { StreetGlobe } from './StreetGlobe';
import { LanguageSwitcher } from './LanguageSwitcher';

interface City {
  name: string;
  active: boolean;
}

const CITIES: City[] = [
  { name: 'Joinville, SC', active: true },
  { name: 'São Paulo, SP', active: false },
  { name: 'Curitiba, PR', active: false },
  { name: 'Blumenau, SC', active: false },
];

/** Futuristic entry screen: a rotating street-globe backdrop and a "pick your city" panel. */
export function LandingScreen({ onEnter }: { onEnter: () => void }) {
  const { t } = useTranslation();
  const [query, setQuery] = useState('');

  const matches = useMemo(() => {
    const q = query.trim().toLowerCase();
    return q ? CITIES.filter((c) => c.name.toLowerCase().includes(q)) : CITIES;
  }, [query]);

  const joinvilleVisible = matches.some((c) => c.active);

  return (
    <div className="relative h-screen w-screen overflow-hidden bg-[#0a0a0a] text-white">
      {/* Rotating street globe */}
      <div className="absolute inset-0">
        <StreetGlobe />
      </div>
      {/* Vignette + green wash */}
      <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(ellipse_at_center,transparent_35%,rgba(0,0,0,0.85)_100%)]" />
      <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_50%_45%,rgba(34,197,94,0.10),transparent_55%)]" />

      {/* Top bar */}
      <header className="absolute inset-x-0 top-0 z-20 flex items-center justify-between p-5">
        <div className="flex items-center gap-2 rounded-full border border-white/10 bg-white/5 px-3 py-1.5 backdrop-blur-md">
          <span className="relative flex h-2 w-2">
            <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-emerald-400 opacity-75" />
            <span className="relative inline-flex h-2 w-2 rounded-full bg-emerald-500" />
          </span>
          <span className="text-[11px] font-semibold uppercase tracking-widest text-neutral-300">
            {t('status.live')}
          </span>
        </div>
        <LanguageSwitcher />
      </header>

      {/* Centre content */}
      <main className="relative z-20 flex h-full flex-col items-center justify-center px-6">
        <div className="w-full max-w-lg text-center">
          <h1 className="bg-gradient-to-b from-white to-neutral-400 bg-clip-text text-3xl font-bold uppercase leading-tight tracking-[0.2em] text-transparent sm:text-4xl">
            {t('app.title')}
          </h1>
          <p className="mt-3 text-sm text-neutral-400 sm:text-base">{t('landing.tagline')}</p>

          {/* City search */}
          <div className="mt-8">
            <div className="flex items-center gap-3 rounded-2xl border border-white/10 bg-neutral-900/60 px-4 py-3.5 shadow-2xl shadow-black/50 backdrop-blur-xl focus-within:border-emerald-500/50">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="shrink-0 text-emerald-400">
                <path d="M12 21s-6-5.686-6-10a6 6 0 0 1 12 0c0 4.314-6 10-6 10Z" strokeLinejoin="round" />
                <circle cx="12" cy="11" r="2" />
              </svg>
              <input
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && joinvilleVisible && onEnter()}
                placeholder={t('landing.searchPlaceholder')}
                aria-label={t('landing.searchPlaceholder')}
                className="w-full bg-transparent text-sm text-white placeholder:text-neutral-500 focus:outline-none"
              />
            </div>

            {/* Suggestions */}
            <div className="mt-2 flex flex-col gap-1.5">
              {matches.map((city) =>
                city.active ? (
                  <button
                    key={city.name}
                    onClick={onEnter}
                    className="group flex items-center justify-between rounded-xl border border-emerald-500/30 bg-emerald-500/10 px-4 py-2.5 text-left transition hover:border-emerald-500/60 hover:bg-emerald-500/20"
                  >
                    <span className="flex items-center gap-2.5">
                      <span className="h-2 w-2 rounded-full bg-emerald-400 shadow-[0_0_8px_#22c55e]" />
                      <span className="text-sm font-medium text-white">{city.name}</span>
                    </span>
                    <span className="text-[10px] font-semibold uppercase tracking-widest text-emerald-400">
                      {t('landing.active')}
                    </span>
                  </button>
                ) : (
                  <div
                    key={city.name}
                    className="flex cursor-not-allowed items-center justify-between rounded-xl border border-white/5 bg-white/5 px-4 py-2.5 opacity-50"
                  >
                    <span className="flex items-center gap-2.5">
                      <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="text-neutral-500">
                        <rect x="5" y="11" width="14" height="9" rx="1.5" />
                        <path d="M8 11V7a4 4 0 0 1 8 0v4" />
                      </svg>
                      <span className="text-sm text-neutral-400">{city.name}</span>
                    </span>
                    <span className="text-[10px] font-semibold uppercase tracking-widest text-neutral-500">
                      {t('landing.comingSoon')}
                    </span>
                  </div>
                ),
              )}
              {!joinvilleVisible && (
                <p className="px-1 pt-1 text-xs text-neutral-500">{t('landing.onlyJoinville')}</p>
              )}
            </div>

            {/* CTA */}
            <button
              onClick={onEnter}
              className="mt-6 inline-flex w-full items-center justify-center gap-2 rounded-2xl bg-emerald-500 px-6 py-3.5 text-sm font-bold uppercase tracking-widest text-neutral-950 shadow-[0_0_30px_-4px_#22c55e] transition hover:bg-emerald-400 hover:shadow-[0_0_40px_0px_#22c55e]"
            >
              {t('landing.enter')}
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                <path d="M5 12h14M13 6l6 6-6 6" strokeLinecap="round" strokeLinejoin="round" />
              </svg>
            </button>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="absolute inset-x-0 bottom-0 z-20 flex justify-center p-5">
        <p className="text-[11px] tracking-wide text-neutral-600">{t('landing.footer')}</p>
      </footer>
    </div>
  );
}
