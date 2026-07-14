// The ONLY place the SSE EventSource connection is created.

import type { StreetStateView } from '../types/traffic';
import { SSE_STREAM } from '../config';

const STREAM_URL = SSE_STREAM;

export interface SseHandlers {
  onUpdate: (state: StreetStateView) => void;
  onOpen?: () => void;
  onError?: () => void;
}

// EventSource.readyState values (0/1/2). Hardcoded so this module can be imported in
// environments without a global EventSource (e.g. jsdom under tests).
const READY_STATE_LABELS: Record<number, string> = {
  0: 'CONNECTING',
  1: 'OPEN',
  2: 'CLOSED',
};

/**
 * Opens a single SSE connection to the backend. Returns a disposer that closes it.
 * `street-update` messages carry a {@link StreetStateView} JSON payload.
 */
export function connectTrafficStream(handlers: SseHandlers): () => void {
  console.info('[SSE] connecting to', STREAM_URL);
  const source = new EventSource(STREAM_URL);

  source.onopen = () => {
    console.info('[SSE] connection open');
    handlers.onOpen?.();
  };

  source.addEventListener('street-update', (event: MessageEvent<string>) => {
    try {
      const state = JSON.parse(event.data) as StreetStateView;
      handlers.onUpdate(state);
    } catch (error) {
      console.error('[SSE] failed to parse street-update payload:', event.data, error);
    }
  });

  source.onerror = (event) => {
    const state = READY_STATE_LABELS[source.readyState] ?? String(source.readyState);
    console.error(
      `[SSE] connection error (readyState=${state}). ` +
        'Check that the backend is running on http://localhost:8081 and that CORS allows this origin.',
      event,
    );
    handlers.onError?.();
  };

  return () => {
    console.info('[SSE] closing connection');
    source.close();
  };
}
