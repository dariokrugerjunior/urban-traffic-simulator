import { create } from 'zustand';
import type { CongestionLevel, StreetStateView } from '../types/traffic';
import { connectTrafficStream } from '../services/sseService';
import { fetchStreets } from '../services/apiService';

type ConnectionStatus = 'connecting' | 'live' | 'offline';

interface TrafficState {
  /** Latest backend state per street id (drives colors and the tooltip). */
  streets: Record<string, StreetStateView>;
  status: ConnectionStatus;
  selectedStreetId: string | null;
  /** Opens the SSE stream once and loads the initial snapshot. Returns a disposer. */
  connect: () => () => void;
  selectStreet: (id: string | null) => void;
  levelOf: (id: string) => CongestionLevel;
}

export const useTrafficStore = create<TrafficState>((set, get) => ({
  streets: {},
  status: 'connecting',
  selectedStreetId: null,

  connect: () => {
    // Load the initial snapshot (best-effort), then subscribe to live updates.
    fetchStreets()
      .then((snapshot) => {
        set((prev) => {
          const streets = { ...prev.streets };
          for (const s of snapshot) {
            streets[s.id] = s;
          }
          return { streets };
        });
      })
      .catch(() => set({ status: 'offline' }));

    const disconnect = connectTrafficStream({
      onOpen: () => set({ status: 'live' }),
      onError: () => set({ status: 'offline' }),
      onUpdate: (state) =>
        set((prev) => ({
          status: 'live',
          streets: { ...prev.streets, [state.id]: state },
        })),
    });

    return disconnect;
  },

  selectStreet: (id) => set({ selectedStreetId: id }),

  levelOf: (id) => get().streets[id]?.congestionLevel ?? 'FREE',
}));
