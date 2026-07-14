import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import { en } from './locales/en';
import { pt } from './locales/pt';

export type Language = 'pt' | 'en';

const STORAGE_KEY = 'uts-lang';

function initialLanguage(): Language {
  try {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved === 'pt' || saved === 'en') {
      return saved;
    }
  } catch {
    // localStorage unavailable — fall back to default
  }
  return 'pt';
}

i18n.use(initReactI18next).init({
  resources: {
    en: { translation: en },
    pt: { translation: pt },
  },
  lng: initialLanguage(),
  fallbackLng: 'en',
  interpolation: { escapeValue: false },
});

/** Switches the active language and persists the choice. */
export function setLanguage(language: Language): void {
  void i18n.changeLanguage(language);
  try {
    localStorage.setItem(STORAGE_KEY, language);
  } catch {
    // ignore persistence errors
  }
}

export default i18n;
