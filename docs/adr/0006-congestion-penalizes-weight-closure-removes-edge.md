# ADR-0006: Congestion penalizes routing weight; closure removes the edge

- **Status:** Accepted
- **Date:** 2026-07-27

## Context

Two different things can make a street undesirable to a router, and they are not the same
thing:

- **It is jammed.** Slow, but passable. A real driver stuck with no alternative still takes it.
- **It is closed.** Impassable. No driver takes it, whatever the alternative costs.

Collapsing both into one mechanism forces a wrong answer at one end: treat a jam as removal
and the router reports "no route" for a city that is merely congested; treat a closure as a
penalty and the router happily sends traffic down a blocked street when the detour is long
enough.

## Decision

Model them as two distinct mechanisms on `RouteStreet`:

- **Congestion → multiplicative penalty.** `StreetCongestedEvent` sets `penaltyFactor`
  (`PenalizeStreetUseCase.DEFAULT_PENALTY = 10.0`) and the effective cost becomes
  `weight() = baseWeight × penaltyFactor`. The edge stays in the graph. `StreetClearedEvent`
  resets the factor to `1.0`.
- **Closure → the edge is skipped.** `StreetTopologyChangedEvent` sets `blocked`, and the path
  finder does not traverse it at all.

Both apply to *every* direction of a street: a two-way street is two directed edges sharing an
id, and `RoadNetwork.penalize` / `setBlocked` iterate over all of them.

## Consequences

- A jammed street remains a valid last resort. If it is the only link to a node, routes
  through it still exist — they just cost ten times more.
- A closed street produces a genuine detour, or a genuine absence of route, which is the
  honest answer.
- The demo shows the arithmetic plainly: `I1 → I5` costs `5` direct; once Beira-Rio is jammed
  its weight becomes `50`, so the two-hop alternative at `6` wins and the GPS reroutes.
- The two mechanisms compose without special cases — a street can be penalised *and* closed,
  and closure simply dominates.

### Trade-offs accepted

- **The penalty is a single step, not a curve.** `JAMMED` applies `10.0` regardless of whether
  the street is at `0.81` or `0.99`. A continuous function of the congestion ratio would be
  more faithful; the step keeps the reroute legible and matches the discrete levels of
  [ADR-0002](0002-macroscopic-traffic-model.md).
- The factor is calibrated for this network's weight scale (corridor weights 2–5, OSM weights
  in metres). It is a constant that would need revisiting if the weight unit changed.
- Penalising all directions of a two-way street means a jam is assumed symmetric, which real
  rush-hour traffic is not.

## Alternatives considered

- **Remove the edge when jammed** — rejected: produces "route not found" for a passable city
  and cannot express "slow but available", which is the entire behaviour being demonstrated.
- **Additive penalty (`weight + K`)** — rejected: a fixed increment is negligible for a long
  street and decisive for a short one, so the same jam would mean different things depending
  on street length. Multiplying preserves the relative cost of the network.
- **Recompute weights from the congestion ratio on every route request** — rejected: it would
  make routing read live state from another service on the hot path, reintroducing exactly the
  synchronous coupling [ADR-0003](0003-event-driven-integration-over-kafka.md) removes.
