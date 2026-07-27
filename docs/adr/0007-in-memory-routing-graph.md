# ADR-0007: Hold the routing graph in memory, seeded at boot

- **Status:** Accepted
- **Date:** 2026-07-27

## Context

`routing-service` runs Dijkstra over the Joinville network on every route request. The graph
is large enough to matter (~2,892 directed edges, one per one-way street and two per two-way)
and is read on every hop of every search.

`traffic-state-service` persists street state in PostgreSQL because that state is authoritative
and mutated by users. The routing graph is a different kind of data: it is *derived*, and the
only mutations it accepts arrive as Kafka events.

## Decision

`routing-service` has no database. `RoadNetwork` is an in-memory directed graph —
`LinkedHashMap` of intersections, adjacency lists keyed by node id, plus an index of every
directed edge sharing a street id — seeded at startup in `RoutingBeansConfig` from the bundled
`routing-graph.json` resource, produced by the OSM pipeline in `scripts/`.

Runtime mutations (`penalize`, `setBlocked`, `clear`) apply to that in-memory state and are
deliberately **not** persisted. A restart rebuilds the graph from the seed and replays nothing.

## Consequences

- Dijkstra touches only heap structures; no query, no connection pool, no N+1 on the hot path.
- The service starts with no external storage dependency, so it can be run and tested on its
  own against nothing but a broker.
- The seed file is a build artefact of the OSM pipeline, so the graph is reproducible: the
  same input produces the same network, and the file is reviewable in a diff.

### Trade-offs accepted

- **Penalties and closures are lost on restart.** After a restart, routing believes every
  street is clear until traffic-state emits the events again. This is accepted because the
  authoritative state lives in traffic-state-service; routing holds a projection.
- **The graph must fit in memory, and it does not scale horizontally by itself.** Two
  instances of routing-service would each hold a full copy and would converge only because
  they consume the same events.
- **Ids are a cross-service contract.** Node and street ids in the routing seed must match the
  traffic-state seed, or congestion events reference edges that do not exist. The constraint is
  called out in `RoutingBeansConfig`'s javadoc; it is currently enforced by convention, not by
  a test spanning both services.

## Alternatives considered

- **Load the graph from PostgreSQL** — rejected: it adds a datastore for data that never
  changes at runtime except through events, and puts I/O inside the search loop.
- **Query traffic-state-service for weights at request time** — rejected: same synchronous
  coupling removed in [ADR-0003](0003-event-driven-integration-over-kafka.md), now on the
  latency-critical path.
- **Persist penalties to survive restarts** — rejected for now: it would make routing a second
  source of truth for congestion. The correct fix, if restart resilience is ever needed, is for
  traffic-state to republish current state on demand, which is a new ADR, not an edit to this one.
