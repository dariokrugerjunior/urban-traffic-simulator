export const pt = {
  app: {
    title: 'Simulador de Tráfego Urbano',
    subtitle: 'Joinville · congestionamento macroscópico',
  },
  status: {
    connecting: 'Conectando',
    live: 'Ao vivo',
    offline: 'Offline',
  },
  legend: {
    title: 'Congestionamento',
  },
  congestion: {
    FREE: 'Fluxo livre',
    HEAVY: 'Intenso',
    JAMMED: 'Congestionado',
  },
  panel: {
    street: 'Rua',
    addTrafficLight: 'Adicionar semáforo',
    injectVehicles: '+ {{n}} veíc.',
    metrics: '{{volume}}/{{capacity}} veíc/h · {{percent}}%',
    hint: 'Os comandos são publicados no Kafka. O mapa recolore quando o backend envia o novo estado via SSE.',
    error: 'Falha no comando — o backend está rodando na :8081?',
    close: 'Fechar',
  },
  tooltip: {
    metrics: '{{volume}}/{{capacity}} veíc/h',
  },
  route: {
    title: 'Rota GPS',
    cost: 'custo {{cost}}',
    none: 'Nenhuma rota disponível',
    loading: 'Calculando rota…',
  },
} as const;
