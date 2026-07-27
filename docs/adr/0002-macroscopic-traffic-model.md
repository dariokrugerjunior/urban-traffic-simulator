# ADR-0002: Model traffic macroscopically, not as individual vehicles

- **Status:** Accepted
- **Date:** 2026-07-27

## Context

The system must show congestion building, spreading and dissipating across central
Joinville — 2,892 simulated streets — and it must stay responsive while a user injects
traffic, adds signals and closes streets from the browser.

There are two standard ways to simulate road traffic:

- **Microscopic** — every vehicle is an entity with position, speed and a car-following
  rule. High fidelity, and the cost of a tick grows with the number of *vehicles*.
- **Macroscopic** — traffic is a fluid; a street holds a *volume* measured against its
  *capacity*. Lower fidelity, and the cost of a tick grows with the number of *streets*.

## Decision

Model traffic macroscopically. A street's state is `volume / effectiveCapacity`, discretised
into three levels:

| Ratio | Level |
|-------|-------|
| `< 0.50` | `FREE` |
| `0.50 – 0.80` | `HEAVY` |
| `> 0.80` | `JAMMED` |

Individual vehicles are never represented. Traffic lights do not stop anything — they shrink
a street's *effective* capacity by a green-time fraction
(`effectiveCapacity = floor(capacity × greenRatio^lights)`), and congestion is always measured
against that effective value, never the raw one.

## Consequences

- A simulation tick is bounded by edge count (~2,892), not by vehicle count, so the whole
  city can be simulated live and streamed to the browser at interactive rates.
- The domain stays small enough to be pure arithmetic: no physics, no collision handling, no
  per-vehicle state to persist.
- Congestion is a *property of a street*, which maps directly onto both the SSE payload and
  the routing weight — the same number drives the map colour and the GPS penalty.

### Trade-offs accepted

- No queue dynamics, no shockwaves, no intersection turn conflicts. The model cannot answer
  "how long is the queue at this light?" — only "how saturated is this street?".
- Congestion levels are step functions. A street at `0.79` and one at `0.51` render
  identically, which is a deliberate simplification for legibility.
- Traffic-light modelling is an approximation of throughput, not a signal-timing simulation.

## Alternatives considered

- **Microscopic (agent-based)** — rejected: simulating enough vehicles to congest ~2,900
  streets would dominate the runtime and shift the project's centre of gravity from
  distributed-systems design to simulation physics, which is not what this system is for.
- **Mesoscopic (platoon-based)** — rejected: it carries much of the complexity of the
  microscopic model without removing the need for per-group state, for fidelity this system
  never consumes.
