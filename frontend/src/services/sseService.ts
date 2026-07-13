// The ONLY place the SSE EventSource connection is created.

import type { StreetStateView } from '../types/traffic';

const STREAM_URL = 'http://localhost:8081/api/traffic/stream';

export interface SseHandlers {
  onUpdate: (state: StreetStateView) => void;
  onOpen?: () => void;
  onError?: () => void;
}

const READY_STATE_LABELS: Record<number, string> = {
  [EventSource.CONNECTING]: 'CONNECTING',
  [EventSource.OPEN]: 'OPEN',
  [EventSource.CLOSED]: 'CLOSED',
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
