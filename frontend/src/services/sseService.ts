// The ONLY place the SSE EventSource connection is created.

import type { StreetStateView } from '../types/traffic';

const STREAM_URL = 'http://localhost:8081/api/traffic/stream';

export interface SseHandlers {
  onUpdate: (state: StreetStateView) => void;
  onOpen?: () => void;
  onError?: () => void;
}

/**
 * Opens a single SSE connection to the backend. Returns a disposer that closes it.
 * `street-update` messages carry a {@link StreetStateView} JSON payload.
 */
export function connectTrafficStream(handlers: SseHandlers): () => void {
  const source = new EventSource(STREAM_URL);

  source.addEventListener('open', () => handlers.onOpen?.());

  source.addEventListener('street-update', (event: MessageEvent<string>) => {
    const state = JSON.parse(event.data) as StreetStateView;
    handlers.onUpdate(state);
  });

  source.addEventListener('error', () => handlers.onError?.());

  return () => source.close();
}
