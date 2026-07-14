import { create } from 'zustand';
import type { CongestionLevel, StreetStateView } from '../types/traffic';
import { connectTrafficStream } from '../services/sseService';
import { fetchStreets } from '../services/apiService';
import { fetchRoute, type RouteView } from '../services/routeService';

type ConnectionStatus = 'connecting' | 'live' | 'offline';

/** Demo route continuously recomputed by the routing-service as congestion changes. */
export const ROUTE_START = 'I1';
export const ROUTE_END = 'I5';

interface TrafficState {
  /** Latest backend state per street id (drives colors and the tooltip). */
  streets: Record<string, StreetStateView>;
  status: ConnectionStatus;
  selectedStreetId: string | null;
  /** Current shortest path from the routing-service (null until first loaded). */
  route: RouteView | null;
  /** Opens the SSE stream once and loads the initial snapshot. Returns a disposer. */
  connect: () => () => void;
  selectStreet: (id: string | null) => void;
  /** Re-queries the routing-service for the demo route (I1 → I5). */
  refreshRoute: () => void;
  levelOf: (id: string) => CongestionLevel;
}

let routeTimer: ReturnType<typeof setTimeout> | undefined;

export const useTrafficStore = create<TrafficState>((set, get) => ({
  streets: {},
  status: 'connecting',
  selectedStreetId: null,
  route: null,

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

    get().refreshRoute();

    const disconnect = connectTrafficStream({
      onOpen: () => set({ status: 'live' }),
      onError: () => set({ status: 'offline' }),
      onUpdate: (state) => {
        set((prev) => ({
          status: 'live',
          streets: { ...prev.streets, [state.id]: state },
        }));
        // A congestion change may have re-routed the GPS engine — refresh the route
        // (debounced to coalesce bursts of updates).
        clearTimeout(routeTimer);
        routeTimer = setTimeout(() => get().refreshRoute(), 500);
      },
    });

    return disconnect;
  },

  selectStreet: (id) => set({ selectedStreetId: id }),

  refreshRoute: () => {
    fetchRoute(ROUTE_START, ROUTE_END)
      .then((route) => set({ route }))
      .catch(() => {
        // routing-service unavailable — leave the previous route as-is
      });
  },

  levelOf: (id) => get().streets[id]?.congestionLevel ?? 'FREE',
}));
