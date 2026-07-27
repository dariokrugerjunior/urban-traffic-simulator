# Architecture Decision Records

Short records of the decisions that shaped this system — what was decided, why, what it
costs, and what was rejected. Each record is immutable: if a decision changes, a new ADR
supersedes the old one rather than editing history.

Format: a trimmed [MADR](https://adr.github.io/madr/) — Context, Decision, Consequences,
Alternatives considered.

| # | Decision | Status |
|---|----------|--------|
| [0001](0001-record-architecture-decisions.md) | Record architecture decisions | Accepted |
| [0002](0002-macroscopic-traffic-model.md) | Model traffic macroscopically, not as individual vehicles | Accepted |
| [0003](0003-event-driven-integration-over-kafka.md) | Integrate services through Kafka events, not synchronous REST | Accepted |
| [0004](0004-sse-instead-of-websockets.md) | Push live updates over SSE instead of WebSockets | Accepted |
| [0005](0005-framework-free-domain-layer.md) | Keep the domain layer free of framework annotations | Accepted |
| [0006](0006-congestion-penalizes-weight-closure-removes-edge.md) | Congestion penalizes routing weight; closure removes the edge | Accepted |
| [0007](0007-in-memory-routing-graph.md) | Hold the routing graph in memory, seeded at boot | Accepted |
| [0008](0008-street-capacity-from-osm-road-class.md) | Derive street capacity from the OSM road class at build time | Accepted |
| [0009](0009-curated-corridor-alongside-osm-network.md) | Keep a curated 5-street corridor alongside the real OSM network | Accepted |
