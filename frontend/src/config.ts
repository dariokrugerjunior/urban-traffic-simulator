// Backend endpoints. Overridable at build time via Vite env vars so the same build
// can point at localhost (dev) or the deployed services (prod).
const trafficBase = import.meta.env.VITE_TRAFFIC_API_URL ?? 'http://localhost:8081';
const routingBase = import.meta.env.VITE_ROUTING_API_URL ?? 'http://localhost:8082';

export const TRAFFIC_API = `${trafficBase}/api/traffic`;
export const ROUTING_API = `${routingBase}/api/routes`;
export const SSE_STREAM = `${trafficBase}/api/traffic/stream`;
