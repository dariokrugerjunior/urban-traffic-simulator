# ADR-0004: Push live updates over SSE instead of WebSockets

- **Status:** Accepted
- **Date:** 2026-07-27

## Context

The map must recolour ~2,892 streets as congestion changes, without the browser polling for
state. `PROJECT_CONTEXT.md` mandates SSE and forbids WebSockets; this record captures why.

The traffic in this system is asymmetric:

- **Server → client:** a continuous stream of street-state updates.
- **Client → server:** discrete, infrequent commands — inject vehicles, add a traffic light,
  close a street — each of which is a natural REST call with a status code and a body.

## Decision

Live updates are pushed over **Server-Sent Events** (`GET /api/traffic/stream`, Spring's
`SseEmitter`). Commands travel over the REST API. Updates are batched server-side so the map
stays smooth under bursts.

The browser never computes congestion. It renders what the backend pushed.

## Consequences

- `EventSource` reconnects on its own. No reconnect/backoff loop to write, test, or get
  subtly wrong.
- The stream is plain HTTP/1.1, so proxies and load balancers handle it without an upgrade
  path, and it can be consumed from a terminal: `curl -N http://localhost:8081/api/traffic/stream`.
  Being demonstrable without a special client is a property of this project worth keeping.
- The command path keeps HTTP semantics — status codes, idempotency, and `curl`-based
  documentation — instead of becoming untyped messages over a socket.

### Trade-offs accepted

- **The channel is one-way.** If the client ever needs to stream data back (say, a dragged
  cursor position at high frequency), SSE cannot carry it and this decision must be revisited.
- SSE is text-only (UTF-8); binary framing would require encoding. Not a constraint today —
  the payload is JSON.
- Browsers cap concurrent SSE connections per origin under HTTP/1.1 (~6). Irrelevant for a
  single-tab simulator, relevant if the UI ever opens multiple independent streams.

## Alternatives considered

- **WebSockets** — rejected: it buys a bidirectional channel this system never uses, and
  charges for it with a protocol upgrade, manual reconnection, and a second transport
  vocabulary alongside REST.
- **Polling `GET /api/traffic/streets`** — rejected: the whole point is that congestion
  evolves continuously; polling either wastes requests or lags visibly behind the simulation.
- **gRPC streaming** — rejected: not natively reachable from a browser without a proxy layer,
  which reintroduces exactly the infrastructure this decision avoids.
