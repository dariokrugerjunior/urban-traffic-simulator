// The ONLY place the routing-service REST API is fetched.

const BASE_URL = 'http://localhost:8082/api/routes';

/** Shortest path returned by the routing-service (mirrors its RouteView). */
export interface RouteView {
  found: boolean;
  streets: string[];
  nodes: string[];
  totalCost: number;
}

/**
 * Fetches the current shortest path between two intersections. The routing-service
 * penalizes congested streets, so the same query returns a different path once a
 * street on it becomes JAMMED.
 */
export async function fetchRoute(start: string, end: string): Promise<RouteView> {
  const response = await fetch(`${BASE_URL}?start=${encodeURIComponent(start)}&end=${encodeURIComponent(end)}`);
  if (!response.ok) {
    throw new Error(`Failed to fetch route (${response.status})`);
  }
  return (await response.json()) as RouteView;
}
