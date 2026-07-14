import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Legend } from './Legend';
import i18n from '../i18n';

describe('Legend', () => {
  it('renders the title and all congestion levels in English', async () => {
    await i18n.changeLanguage('en');
    render(<Legend />);
    expect(screen.getByText('Congestion')).toBeInTheDocument();
    expect(screen.getByText('Free flow')).toBeInTheDocument();
    expect(screen.getByText('Heavy')).toBeInTheDocument();
    expect(screen.getByText('Jammed')).toBeInTheDocument();
  });

  it('renders the labels in Portuguese', async () => {
    await i18n.changeLanguage('pt');
    render(<Legend />);
    expect(screen.getByText('Congestionamento')).toBeInTheDocument();
    expect(screen.getByText('Fluxo livre')).toBeInTheDocument();
    expect(screen.getByText('Congestionado')).toBeInTheDocument();
  });
});
