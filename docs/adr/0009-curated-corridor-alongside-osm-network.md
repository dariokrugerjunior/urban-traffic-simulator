# ADR-0009: Keep a curated 5-street corridor alongside the real OSM network

- **Status:** Accepted
- **Date:** 2026-07-27

## Context

The system's central claim is that a jam on one street reroutes the GPS through a Kafka event.
Demonstrating that on the real OSM graph means quoting ids like
`st-e79290809-0 → st-e88150312-0` and paths of 7 to 9 edges — correct, but unreadable. A
reviewer cannot verify at a glance that the detour is the *right* detour.

At the same time, dropping the real network would reduce the project to a toy graph and lose
everything ADR-0008 buys.

## Decision

Run both in the same engine. Alongside the 2,887 real OSM edges, keep a curated corridor of
5 named streets on abstract nodes (`I1`=Centro, `I2`=Estação, `I3`=América, `I5`=Saguaçu):

| Street | Edge | Capacity | Weight |
|--------|------|----------|--------|
| Av. Hermann August Lepper (Beira-Rio) | I1 → I5 | 2000 | 5 |
| Rua João Colin | I1 → I3 | 1800 | 3 |
| Rua Dona Francisca | I3 → I5 | 1400 | 3 |
| Rua Nove de Março | I1 → I2 | 900 | 2 |
| Rua XV de Novembro | I2 → I3 | 1000 | 2 |

Total: 2,892 simulated streets. Both sets are seeded into the same `RoadNetwork`, coloured by
the same congestion maths, and routed by the same Dijkstra. The corridor is drawn thicker on
the map so it is findable.

## Consequences

- The headline walkthrough fits in three commands and arithmetic a reader can check mentally:
  `I1 → I5` costs 5 direct; jam Beira-Rio and its weight becomes 50, so the two-hop path at 6
  wins. Nothing about that requires trusting the implementation.
- The full-city behaviour is still demonstrable on real data, via `/api/routes/between` over
  the OSM graph, for anyone who wants the harder proof.
- Because both live in one engine, the corridor is a real test of the production path, not a
  mock — if the demo works, the mechanism works.

### Trade-offs accepted

- **The network is not purely real.** 5 of 2,892 streets are synthetic, with capacities and
  weights chosen for legibility. Anyone reading the graph as data about Joinville must know
  this; it is stated in the README and here.
- The corridor uses a different weight unit from the OSM edges: abstract weights of 2–5 versus
  real lengths in metres. The two subgraphs are therefore not cost-comparable with each other,
  and they connect only through shared engine behaviour, not through shared nodes.
- Two id conventions coexist (`st-beira-rio` versus `st-e79290809-0`), which any consumer of
  the API has to tolerate.

## Alternatives considered

- **Only the real OSM network** — rejected: the core behaviour would still be correct but no
  longer *demonstrable* in a README, and a reviewer's first impression of the project would be
  a wall of opaque ids.
- **Only the curated corridor** — rejected: it would make the system a 5-edge toy and discard
  the OSM pipeline, the road-class capacities and the 2,900-street live simulation.
- **A separate demo profile with its own graph** — rejected: a demo that runs a different
  configuration from the real one proves nothing about the real one, and would need to be kept
  in sync forever.
