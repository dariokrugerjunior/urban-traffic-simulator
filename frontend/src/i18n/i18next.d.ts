import 'i18next';
import type { en } from './locales/en';

// Enables type-checked translation keys in t('...') calls.
declare module 'i18next' {
  interface CustomTypeOptions {
    defaultNS: 'translation';
    resources: {
      translation: typeof en;
    };
  }
}
