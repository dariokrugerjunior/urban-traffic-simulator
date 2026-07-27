# ADR-0005: Keep the domain layer free of framework annotations

- **Status:** Accepted
- **Date:** 2026-07-27

## Context

The interesting logic in this system is arithmetic and graph theory: congestion ratios,
effective capacity under traffic lights, Dijkstra over a weighted directed graph. None of it
has anything to do with HTTP, JPA, or Kafka.

The default Spring Boot layout invites the opposite: one class annotated `@Entity`,
`@Service` and serialised by Jackson, so persistence mapping, transport shape and business
rules end up in the same file. Then the business rule cannot be tested without a context, and
a database column rename becomes an API change.

## Decision

Each service is split into three layers, with a hard rule on the innermost one:

| Layer | Responsibility | Framework annotations |
|-------|----------------|-----------------------|
| `domain` | entities, value objects, ports, algorithms | **never** |
| `application` | use cases orchestrating the domain through ports | no |
| `infrastructure` | REST, Kafka, JPA, SSE, configuration | yes |

The domain is plain Java — no `@Entity`, no `@Component`, no Jackson. Persistence and
messaging map *to and from* the domain in the infrastructure layer only.

Because use cases carry no stereotype annotations, they are wired explicitly as `@Bean`
methods in an infrastructure `@Configuration` (see `RoutingBeansConfig`), not discovered by
component scanning.

## Consequences

- Domain and use-case tests are constructor calls with fake ports — no Spring context, no
  database, no broker. That is why the suite runs in seconds and why the congestion maths is
  tested directly rather than through a controller.
- The persistence model can change shape without touching a business rule, and vice versa.
- `RouteStreet` can hold mutable routing state (`penaltyFactor`, `blocked`) with invariants
  enforced in its own constructor and setters, instead of being a bag of getters that JPA and
  Jackson both need to satisfy.

### Trade-offs accepted

- **Mapping code exists and must be maintained.** Every domain object crossing a boundary has
  a JPA entity or a DTO next to it, and a mapper between them. This is duplication, accepted
  knowingly.
- **Wiring is manual.** `RoutingBeansConfig` grows by one `@Bean` method per use case. A new
  use case that someone forgets to register fails at startup, not at compile time.
- For a system this size, the pure-domain rule costs more lines than it saves. It is kept
  because the boundary is the point — the cost is the price of the property, not an oversight.

## Alternatives considered

- **Annotate domain classes with `@Entity`/`@Service`** — rejected: it makes the innermost
  layer depend on the outermost, which is the coupling this architecture exists to prevent.
- **Two layers (service + repository)** — rejected: with no explicit ports, the graph
  algorithms would end up reachable only through a Spring context, and their tests would
  inherit its startup cost.
- **`@Component` on use cases with a pure domain** — rejected as a half-measure: it keeps the
  entities clean but makes the application layer non-instantiable outside Spring, for the sole
  benefit of deleting a handful of `@Bean` methods.
