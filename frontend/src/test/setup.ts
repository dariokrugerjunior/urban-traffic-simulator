import '@testing-library/jest-dom/vitest';
import { afterEach } from 'vitest';
import { cleanup } from '@testing-library/react';
import '../i18n';

// Unmount React trees after each test to avoid cross-test leakage.
afterEach(() => {
  cleanup();
});
