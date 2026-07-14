import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { LandingScreen } from './LandingScreen';
import i18n from '../i18n';

// The globe draws to <canvas>, which jsdom doesn't implement — stub it out.
vi.mock('./StreetGlobe', () => ({ StreetGlobe: () => null }));

describe('LandingScreen', () => {
  beforeEach(async () => {
    await i18n.changeLanguage('en');
  });

  it('shows the tagline, the active city and locked cities', () => {
    render(<LandingScreen onEnter={() => {}} />);
    expect(screen.getByText('A living simulation of urban mobility')).toBeInTheDocument();
    expect(screen.getByText('Joinville, SC')).toBeInTheDocument();
    expect(screen.getByText('São Paulo, SP')).toBeInTheDocument();
    // locked cities are marked "coming soon"
    expect(screen.getAllByText('coming soon').length).toBeGreaterThan(0);
  });

  it('calls onEnter when the active city is clicked', () => {
    const onEnter = vi.fn();
    render(<LandingScreen onEnter={onEnter} />);
    fireEvent.click(screen.getByText('Joinville, SC'));
    expect(onEnter).toHaveBeenCalledTimes(1);
  });

  it('calls onEnter from the Enter button', () => {
    const onEnter = vi.fn();
    render(<LandingScreen onEnter={onEnter} />);
    fireEvent.click(screen.getByRole('button', { name: /Enter/i }));
    expect(onEnter).toHaveBeenCalledTimes(1);
  });

  it('filters cities by the search query', () => {
    render(<LandingScreen onEnter={() => {}} />);
    fireEvent.change(screen.getByLabelText('Type your city…'), { target: { value: 'curit' } });
    expect(screen.getByText('Curitiba, PR')).toBeInTheDocument();
    expect(screen.queryByText('Joinville, SC')).not.toBeInTheDocument();
  });
});
