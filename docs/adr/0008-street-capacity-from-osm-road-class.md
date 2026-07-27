# ADR-0008: Derive street capacity from the OSM road class at build time

- **Status:** Accepted
- **Date:** 2026-07-27

## Context

Every street needs an hourly capacity, because congestion is `volume / effectiveCapacity`
([ADR-0002](0002-macroscopic-traffic-model.md)). With ~2,900 streets, hand-assigning capacity
is not an option, and a single constant for all of them would make a residential lane and a
motorway behave identically — which would destroy the point of the simulation.

OpenStreetMap already classifies every road with a `highway` tag, and that classification
correlates with real throughput.

## Decision

Capacity is derived from the OSM `highway` class, at **data-build time**, by
`scripts/build-osm-enrichment.py`. The resulting values are baked into the seed data consumed
by the services:

| Class | `motorway` | `trunk` | `primary` | `secondary` | `tertiary` | `unclassified` | `residential` | `living_street` |
|-------|-----------|---------|-----------|-------------|------------|----------------|---------------|-----------------|
| **veh/h** | 2600 | 2200 | 1600 | 1200 | 900 | 700 | 600 | 400 |

The same pipeline decides two related things from real data rather than from guesses:

- **Where traffic enters.** Only arterial classes (`motorway`, `trunk`, `primary` and their
  links) are injection candidates; the engine picks the `simulation.source-count` (default 14)
  highest-capacity ones, so the city is fed by its highways instead of uniformly.
- **Where signals slow traffic down.** Of Joinville's 129 real OSM signals in the bounding box,
  97 sit on a node of the simulated graph (matched by id, or snapped within 55 m); streets
  arriving at those nodes get a light at `simulation.signal-green-ratio` (default 0.6),
  penalising 159 edges.

## Consequences

- The network behaves like the actual city: of the 2,887 OSM edges, 1,664 are `residential`
  and only 47 are `motorway`, so jams concentrate where a real city concentrates them.
- Services boot with capacities already resolved — no classification logic, no OSM parsing and
  no geospatial matching inside the runtime.
- The mapping is a small table in one script, so recalibrating the whole city is a one-line
  change plus a data rebuild, reviewable as a diff.

### Trade-offs accepted

- **Capacity ignores lane count, width and grade.** Two `primary` streets get the same 2600
  veh/h even if one has four lanes and the other two. The road class is a proxy, chosen because
  it is complete across the dataset — lane tags in OSM are not.
- The numbers are plausible engineering estimates, not measured counts for Joinville. They are
  calibrated for legibility of the simulation, and should not be read as traffic-study output.
- Regenerating the seed is a manual step outside the application build; a stale seed and new
  script parameters can drift apart without anything failing loudly.

## Alternatives considered

- **Uniform capacity for all streets** — rejected: congestion would be a function of where
  traffic was injected and nothing else, so the map would carry no information about the city.
- **Classify at runtime, on startup** — rejected: it puts OSM parsing and 55 m spatial snapping
  into service boot, for a result that is identical on every run.
- **Hand-tuned capacities per street** — rejected: unmaintainable at ~2,900 edges, and it would
  encode the author's assumptions instead of the map's data.
