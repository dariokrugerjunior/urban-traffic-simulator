// The ONLY place the traffic-signal markers are fetched.

/** A traffic signal position as [lng, lat]. */
export type Signal = [number, number];

/** Loads the real OSM traffic-signal coordinates, served as a static asset from /public. */
export async function fetchSignals(): Promise<Signal[]> {
  const response = await fetch('/signals.json');
  if (!response.ok) {
    throw new Error(`Failed to load traffic signals (${response.status})`);
  }
  return (await response.json()) as Signal[];
}
