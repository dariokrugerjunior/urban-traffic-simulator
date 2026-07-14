import { describe, it, expect, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { LanguageSwitcher } from './LanguageSwitcher';
import i18n from '../i18n';

describe('LanguageSwitcher', () => {
  beforeEach(async () => {
    await i18n.changeLanguage('pt');
  });

  it('switches the active language when a button is clicked', async () => {
    render(<LanguageSwitcher />);

    await userEvent.click(screen.getByText('EN'));
    expect(i18n.language).toBe('en');

    await userEvent.click(screen.getByText('PT'));
    expect(i18n.language).toBe('pt');
  });
});
