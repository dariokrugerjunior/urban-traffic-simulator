// The ONLY place REST calls to the backend are made.

import type { StreetStateView } from '../types/traffic';
import { TRAFFIC_API } from '../config';

const BASE_URL = TRAFFIC_API;

async function sendCommand(method: 'POST' | 'PATCH', path: string, body: unknown): Promise<void> {
  const response = await fetch(`${BASE_URL}${path}`, {
    method,
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  if (!response.ok) {
    throw new Error(`Request failed (${response.status}): ${path}`);
  }
}

function postCommand(path: string, body: unknown): Promise<void> {
  return sendCommand('POST', path, body);
}

/** A partial topology edit — only the given fields are changed. */
export interface TopologyPatch {
  oneway?: boolean;
  blocked?: boolean;
  source?: boolean;
}

/** Edits a street's topology (one-way/two-way, closed/open, source on/off). */
export function setTopology(streetId: string, patch: TopologyPatch): Promise<void> {
  return sendCommand('PATCH', `/streets/${streetId}/topology`, patch);
}

/** Adds a traffic light to a street (reduces its effective capacity). */
export function addTrafficLight(streetId: string, greenRatio = 0.5): Promise<void> {
  return postCommand(`/streets/${streetId}/traffic-light`, { greenRatio });
}

/** Injects a burst of vehicles into a street. */
export function injectFlow(streetId: string, vehicles: number): Promise<void> {
  return postCommand(`/streets/${streetId}/flow`, { vehicles });
}

/** Removes vehicles from a street (drains traffic). */
export function releaseFlow(streetId: string, vehicles: number): Promise<void> {
  return postCommand(`/streets/${streetId}/release`, { vehicles });
}

/** Fetches the current state of every street (used as the initial snapshot). */
export async function fetchStreets(): Promise<StreetStateView[]> {
  const response = await fetch(`${BASE_URL}/streets`);
  if (!response.ok) {
    throw new Error(`Failed to fetch streets (${response.status})`);
  }
  return (await response.json()) as StreetStateView[];
}
