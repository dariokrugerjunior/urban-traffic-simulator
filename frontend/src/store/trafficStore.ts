import { create } from 'zustand';
import type { CongestionLevel, StreetStateView } from '../types/traffic';
import { connectTrafficStream } from '../services/sseService';
import { fetchStreets } from '../services/apiService';
import { fetchRoute, fetchRouteBetweenStreets, type RouteView } from '../services/routeService';

type ConnectionStatus = 'connecting' | 'live' | 'offline';
type RouteEndpoint = 'origin' | 'destination';

/** Demo route (I1 → I5) shown until the user picks their own origin/destination streets. */
export const ROUTE_START = 'I1';
export const ROUTE_END = 'I5';

interface TrafficState {
  /** Latest backend state per street id (drives colors and the tooltip). */
  streets: Record<string, StreetStateView>;
  status: ConnectionStatus;
  selectedStreetId: string | null;
  /** User-chosen route endpoints (street ids); null → fall back to the demo route. */
  origin: string | null;
  destination: string | null;
  /** Current shortest path from the routing-service (null until first loaded). */
  route: RouteView | null;
  /** Opens the SSE stream once and loads the initial snapshot. Returns a disposer. */
  connect: () => () => void;
  selectStreet: (id: string | null) => void;
  /** Sets the origin/destination street and recomputes the route once both are set. */
  setRouteEndpoint: (kind: RouteEndpoint, id: string) => void;
  /** Clears the user route, returning to the demo I1 → I5. */
  clearRoute: () => void;
  /** Re-queries the routing-service (user route if set, otherwise the demo I1 → I5). */
  refreshRoute: () => void;
  levelOf: (id: string) => CongestionLevel;
}

let routeTimer: ReturnType<typeof setTimeout> | undefined;

export const useTrafficStore = create<TrafficState>((set, get) => ({
  streets: {},
  status: 'connecting',
  selectedStreetId: null,
  origin: null,
  destination: null,
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
      // A consolidated simulation tick — apply the whole batch in ONE store update.
      onBatch: (states) => {
        if (states.length === 0) {
          return;
        }
        set((prev) => {
          const streets = { ...prev.streets };
          for (const s of states) {
            streets[s.id] = s;
          }
          return { status: 'live', streets };
        });
        clearTimeout(routeTimer);
        routeTimer = setTimeout(() => get().refreshRoute(), 500);
      },
    });

    return disconnect;
  },

  selectStreet: (id) => set({ selectedStreetId: id }),

  setRouteEndpoint: (kind, id) => {
    set(kind === 'origin' ? { origin: id } : { destination: id });
    get().refreshRoute();
  },

  clearRoute: () => {
    set({ origin: null, destination: null });
    get().refreshRoute();
  },

  refreshRoute: () => {
    const { origin, destination } = get();
    // User route once both endpoints are picked; otherwise the demo I1 → I5.
    const request =
      origin && destination
        ? fetchRouteBetweenStreets(origin, destination)
        : fetchRoute(ROUTE_START, ROUTE_END);
    request
      .then((route) => set({ route }))
      .catch(() => {
        // routing-service unavailable — leave the previous route as-is
      });
  },

  levelOf: (id) => get().streets[id]?.congestionLevel ?? 'FREE',
}));
