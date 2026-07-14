import { useTranslation } from 'react-i18next';
import { setLanguage, type Language } from '../i18n';

const LANGUAGES: Language[] = ['pt', 'en'];

/** Compact PT / EN language toggle. */
export function LanguageSwitcher() {
  const { i18n } = useTranslation();
  const current = (i18n.resolvedLanguage ?? i18n.language) as Language;

  return (
    <div className="flex items-center gap-0.5 rounded-full border border-white/10 bg-neutral-900/80 p-0.5 shadow-lg shadow-black/40 backdrop-blur-md">
      {LANGUAGES.map((lng) => (
        <button
          key={lng}
          onClick={() => setLanguage(lng)}
          className={`rounded-full px-2.5 py-1 text-xs font-semibold transition ${
            current === lng ? 'bg-white/15 text-white' : 'text-neutral-400 hover:text-white'
          }`}
        >
          {lng.toUpperCase()}
        </button>
      ))}
    </div>
  );
}
