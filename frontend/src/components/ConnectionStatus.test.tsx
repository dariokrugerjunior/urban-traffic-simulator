import { describe, it, expect, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { ConnectionStatus } from './ConnectionStatus';
import { useTrafficStore } from '../store/trafficStore';
import i18n from '../i18n';

describe('ConnectionStatus', () => {
  beforeEach(async () => {
    await i18n.changeLanguage('en');
  });

  it('shows the Live label when the stream is connected', () => {
    useTrafficStore.setState({ status: 'live' });
    render(<ConnectionStatus />);
    expect(screen.getByText('Live')).toBeInTheDocument();
  });

  it('shows Offline when disconnected', () => {
    useTrafficStore.setState({ status: 'offline' });
    render(<ConnectionStatus />);
    expect(screen.getByText('Offline')).toBeInTheDocument();
  });
});
