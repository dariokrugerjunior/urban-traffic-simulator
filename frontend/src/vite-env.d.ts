/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_TRAFFIC_API_URL?: string;
  readonly VITE_ROUTING_API_URL?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
