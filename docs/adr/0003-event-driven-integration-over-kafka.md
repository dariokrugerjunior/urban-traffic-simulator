# ADR-0003: Integrate services through Kafka events, not synchronous REST

- **Status:** Accepted
- **Date:** 2026-07-27

## Context

`traffic-state-service` owns congestion state. `routing-service` owns the GPS graph and must
react when a street becomes jammed, is closed, or clears up.

The naive integration is a synchronous call: when a street changes state, traffic-state
calls routing over HTTP. That makes the writer responsible for the reader's availability —
a `POST /flow` would fail, or block, because a *downstream* service is down, even though the
congestion state itself was computed correctly.

## Decision

The two services never call each other. State changes are published as Kafka events and
consumed asynchronously:

| Event | Producer | Consumer | Effect |
|-------|----------|----------|--------|
| `StreetCongestedEvent` | traffic-state | routing | penalise the street's routing weight |
| `StreetClearedEvent` | traffic-state | routing | reset the penalty |
| `StreetTopologyChangedEvent` | traffic-state | routing | close/open the edge, flip one-way |
| `FlowInjectedEvent`, `TrafficLightAddedEvent` | REST edge | traffic-state | mutate street state |

Kafka runs in **KRaft mode**, without ZooKeeper.

The REST APIs that remain (`/api/traffic/...`, `/api/routes/...`) are the system's *edge* —
they serve the browser and `curl`. They are not how services talk to each other.

## Consequences

- Either service can be restarted independently; events queue and are consumed on recovery.
- Adding a third consumer of congestion (an analytics sink, an alerting service) requires no
  change to the producer.
- The reroute is provable end-to-end without either service knowing the other exists: flood
  `st-beira-rio` via REST on `:8081`, then query `:8082` and watch the route change. The
  README walkthrough is exactly this.
- KRaft removes a whole container and its failure modes from `docker-compose.yml`, and drops
  the ZooKeeper-to-broker version-compatibility question entirely.

### Trade-offs accepted

- **Eventual consistency is visible.** Between the jam and the consumer processing the event
  there is a window in which routing still returns the congested street. The system does not
  hide this window; there is no read-your-writes guarantee across services.
- A broker is now a hard runtime dependency — `docker compose up` is the minimum bar for
  running the system, not a lone `mvn spring-boot:run`.
- Testing the integration needs a broker too. Handled with `spring-kafka-test`
  (EmbeddedKafka) round-trip tests rather than mocked listeners, so the serialisation
  contract is actually exercised.

## Alternatives considered

- **Synchronous REST between services** — rejected: it couples availability in the wrong
  direction, making the write path fail for reasons unrelated to the write.
- **Shared database** — rejected: it would make the routing graph and the congestion state a
  single schema owned by nobody, and any change to one service's storage would silently break
  the other.
- **Kafka with ZooKeeper** — rejected: an extra coordination service with its own operational
  surface, for a capability KRaft now covers in supported Kafka releases.
