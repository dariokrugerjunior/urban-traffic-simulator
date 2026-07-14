export const en = {
  app: {
    title: 'Urban Traffic Simulator',
    subtitle: 'Joinville · macroscopic congestion',
  },
  status: {
    connecting: 'Connecting',
    live: 'Live',
    offline: 'Offline',
  },
  legend: {
    title: 'Congestion',
  },
  congestion: {
    FREE: 'Free flow',
    HEAVY: 'Heavy',
    JAMMED: 'Jammed',
  },
  panel: {
    street: 'Street',
    addTrafficLight: 'Add Traffic Light',
    injectVehicles: '+ {{n}} veh',
    metrics: '{{volume}}/{{capacity}} veh/h · {{percent}}%',
    hint: 'Commands are published to Kafka. The map recolors when the backend pushes the new state over SSE.',
    error: 'Command failed — is the backend running on :8081?',
    close: 'Close',
  },
  tooltip: {
    metrics: '{{volume}}/{{capacity}} veh/h',
  },
  route: {
    title: 'GPS route',
    cost: 'cost {{cost}}',
    none: 'No route available',
    loading: 'Computing route…',
  },
} as const;
