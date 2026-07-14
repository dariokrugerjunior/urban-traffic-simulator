// The ONLY place the simulated city-network geometry is fetched.

/** A simulated street edge: id + name + [lng, lat] polyline. */
export interface SimStreet {
  id: string;
  name: string;
  coords: [number, number][];
}

/**
 * Loads the real central-Joinville road graph (OSM edges between intersections),
 * served as a static asset from /public. Kept out of the JS bundle for size/perf.
 */
export async function fetchCityNetwork(): Promise<SimStreet[]> {
  const response = await fetch('/cityNetwork.json');
  if (!response.ok) {
    throw new Error(`Failed to load city network (${response.status})`);
  }
  return (await response.json()) as SimStreet[];
}
