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
    blocked: 'Closed',
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
    releaseVehicles: '− {{n}} veh',
    addFlow: 'Add traffic',
    removeFlow: 'Remove traffic',
    metrics: '{{volume}}/{{capacity}} veh/h · {{percent}}%',
    topology: 'Topology',
    makeOneWay: 'Make one-way',
    makeTwoWay: 'Make two-way',
    blockStreet: 'Close street',
    reopenStreet: 'Reopen street',
    markSource: 'Mark as source',
    unmarkSource: 'Remove source',
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
    setOrigin: 'Origin',
    setDestination: 'Destination',
    clear: 'clear',
  },
  map: {
    loadingNetwork: 'Loading road network…',
  },
} as const;
