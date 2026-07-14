// The ONLY place the road-network file is fetched.

/** A polyline of [lng, lat] pairs. */
export type RoadPath = [number, number][];

/**
 * Loads Joinville's full drivable road network (served as a static asset from /public).
 * Kept as raw paths for a compact payload and a deck.gl PathLayer.
 */
export async function fetchRoadNetwork(): Promise<RoadPath[]> {
  const response = await fetch('/joinvilleRoads.json');
  if (!response.ok) {
    throw new Error(`Failed to load road network (${response.status})`);
  }
  return (await response.json()) as RoadPath[];
}
