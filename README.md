# Urban Traffic Simulator

[![CI](https://github.com/dariokrugerjunior/urban-traffic-simulator/actions/workflows/ci.yml/badge.svg)](https://github.com/dariokrugerjunior/urban-traffic-simulator/actions/workflows/ci.yml)

A **macroscopic** urban traffic simulator built as an **event-driven microservices** system.
Instead of simulating individual vehicles, it models traffic as a *fluid* — comparing the
**volume** of vehicles against a street's **hourly capacity** — and shows how congestion in
one street dynamically **reroutes** a Dijkstra-based GPS engine through Apache Kafka events.

> Built with Java 21, Spring Boot 3.4, Apache Kafka, PostgreSQL, and a strict
> **Hexagonal Architecture (Ports & Adapters)** with a pure, framework-free domain.

---

## Architecture

```
                 REST (curl)                      Kafka topics                        REST (curl)
                     │                                                                     │
                     ▼                                                                     ▼
        ┌──────────────────────────┐   flow-injected                          ┌──────────────────────┐
        │  traffic-state-service   │◄──────────────┐                          │   routing-service    │
        │        (:8081)           │   traffic-light-added                    │       (:8082)        │
        │                          │───────────────┘                          │                      │
        │  • congestion math       │                                          │  • Dijkstra routing  │
        │  • Street state (JPA)     │        street-congested                  │  • in-memory graph   │
        │  • SSE live updates      │─────────────────────────────────────────►│  • weight penalties  │
        └──────────────────────────┘                                          └──────────────────────┘
                     │                                                                     │
              PostgreSQL / H2                                                    (in-memory graph)
```

Each service is split into three layers:

| Layer | Responsibility | Framework annotations? |
|-------|----------------|------------------------|
| `domain` | Pure business logic (entities, value objects, ports, algorithms) | **Never** |
| `application` | Use cases orchestrating the domain via ports | No |
| `infrastructure` | Adapters: REST, Kafka, JPA, SSE, configuration | Yes |

The **domain layer contains zero framework annotations** — no `@Entity`, no Spring, no Jackson.
Persistence and messaging map to/from the domain in the infrastructure layer only.

---

## Core concepts (macroscopic model)

- The city is a **directed graph**: intersections are nodes, streets are edges.
- **Hourly capacity** — how many vehicles a street can handle per hour.
- **Current volume** — vehicles currently flowing through it.
- **Congestion level** = `volume / effectiveCapacity`:

| Ratio | Level | Color |
|-------|-------|-------|
| `< 0.50` | `FREE` | 🟢 Green |
| `0.50 – 0.80` | `HEAVY` | 🟡 Yellow |
| `> 0.80` | `JAMMED` | 🔴 Red |

- **Traffic lights** reduce a street's *effective* capacity (green-time fraction), pushing it
  toward congestion.
- When a street becomes `JAMMED`, traffic-state-service emits a `StreetCongestedEvent`.
  routing-service consumes it and **multiplies that street's routing weight (×10)**, so future
  routes automatically avoid the jam.

---

## Tech stack

- **Java 21**, **Spring Boot 3.4**
- **Apache Kafka** (KRaft mode — no Zookeeper) for inter-service, event-driven communication
- **Spring Data JPA** with **PostgreSQL** (docker) / **H2** (local dev)
- **Server-Sent Events (SSE)** for real-time state updates (no WebSockets)
- **JUnit 5**, **spring-kafka-test** (EmbeddedKafka) for testing
- **Docker Compose** for orchestration
- **Frontend:** React 18 + TypeScript (strict) + Vite, **Zustand**, **Tailwind CSS**,
  **MapLibre GL + deck.gl** for a token-free real-time map, **react-i18next** (pt-BR/EN), and
  **Vitest + Testing Library**

---

## The seeded network (Joinville, SC)

Real streets from Joinville, with capacities proportional to their real-world size:

| Street | Edge | Capacity (veh/h) | Routing weight |
|--------|------|------------------|----------------|
| Av. Hermann August Lepper (Beira-Rio) | I1 → I5 | 2000 | 5 |
| Rua João Colin (one-way) | I1 → I3 | 1800 | 3 |
| Rua Dona Francisca | I3 → I5 | 1400 | 3 |
| Rua Nove de Março | I1 → I2 | 900 | 2 |
| Rua XV de Novembro | I2 → I3 | 1000 | 2 |

Nodes: `I1=Centro`, `I2=Estação`, `I3=América`, `I5=Saguaçu`.

---

## Running the system

Requires Docker. From the repository root:

```bash
docker compose up --build -d
```

This starts Kafka, PostgreSQL, and both services. Wait until they are healthy
(`docker compose ps`). traffic-state-service boots with the Joinville network already seeded.

### End-to-end demo: watch a jam reroute the GPS

**1. Check the seeded streets and the baseline route (I1 → I5):**

```bash
curl -s http://localhost:8081/api/traffic/streets
curl -s "http://localhost:8082/api/routes?start=I1&end=I5"
# → route uses ["st-beira-rio"], totalCost 5 (the direct avenue)
```

**2. Flood Beira-Rio with traffic until it is JAMMED:**

```bash
curl -s -X POST http://localhost:8081/api/traffic/streets/st-beira-rio/flow \
  -H "Content-Type: application/json" -d '{"vehicles":1900}'
```

`1900 / 2000 = 0.95` → the street becomes `JAMMED`, and a `StreetCongestedEvent` is published.

**3. Ask for the same route again — it now avoids the jam:**

```bash
curl -s "http://localhost:8082/api/routes?start=I1&end=I5"
# → route now returns ["st-joao-colin","st-dona-francisca"], totalCost 6

curl -s http://localhost:8082/api/routes/state
# → st-beira-rio now has penaltyFactor 10 / weight 50
```

The GPS engine rerouted **purely by reacting to a Kafka event** — the two services never call
each other directly.

**4. (Optional) Watch live updates over SSE:**

```bash
curl -N http://localhost:8081/api/traffic/stream
```

You can also add a traffic light to shrink a street's capacity:

```bash
curl -s -X POST http://localhost:8081/api/traffic/streets/st-nove-de-marco/traffic-light \
  -H "Content-Type: application/json" -d '{"greenRatio":0.5}'
```

Tear everything down with:

```bash
docker compose down -v
```

---

## API reference

### traffic-state-service (`:8081`)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/traffic/streets/{id}/flow` | Publish a `FlowInjectedEvent` (`{"vehicles": N}`) |
| `POST` | `/api/traffic/streets/{id}/traffic-light` | Publish a `TrafficLightAddedEvent` (`{"greenRatio": 0.5}`) |
| `GET`  | `/api/traffic/streets` | Current state of every street |
| `GET`  | `/api/traffic/stream` | SSE stream of live street-state updates |

### routing-service (`:8082`)

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/routes?start={id}&end={id}` | Shortest path (Dijkstra) over current weights |
| `GET` | `/api/routes/state` | Every street with its base weight, penalty, and effective weight |

---

## Frontend (live map)

A React + TypeScript single-page app renders Joinville on a dark MapLibre map (no API token
required) and recolors the simulated streets in real time as SSE events arrive.

```bash
cd frontend
npm install
npm run dev        # http://localhost:5173
```

With the backend running (`docker compose up`), the map connects to the SSE stream and:

- **Full road network** — Joinville's ~7,100 drivable roads (real OpenStreetMap geometry,
  bundled as a static asset and drawn with a deck.gl `PathLayer`) form the base layer.
- The **5 simulated streets** are drawn on top with real OSM geometry, thicker and colored by
  congestion; **hover** shows a custom deck.gl-picked tooltip, **click** opens an action panel to
  *Add Traffic Light* or inject vehicles.
- Commands are sent to the backend REST API; the street recolors 🟢→🟡→🔴 when the backend
  pushes the new state back over SSE — the UI never computes congestion itself.
- **i18n** — the interface is available in **pt-BR (default)** and **English**, with a language
  switcher (react-i18next); the choice is persisted in `localStorage`.

The frontend follows a strict separation: `services/` (all `fetch`/`EventSource`), `store/`
(Zustand), `components/` (pure UI), `types/` (backend contracts, zero `any`), `i18n/`
(translations).

---

## Running the tests

Each service is fully unit- and integration-tested (domain logic, use cases with fake ports,
JPA slice tests, web slice tests, and EmbeddedKafka round-trips — including the congestion reroute).

```bash
cd traffic-state-service && mvn test
cd routing-service && mvn test
cd frontend && npm test          # Vitest + Testing Library (components, store, services)
```

> Note: on JDK 25+, tests pass `-Dnet.bytebuddy.experimental=true` (configured in each `pom.xml`)
> so Mockito's Byte Buddy backend can run.

CI (GitHub Actions) runs the backend test matrix plus the frontend type-check, tests, and build
on every push.

---

## Project structure

```
urban-traffic-simulator/
├── .github/workflows/ci.yml      # backend (matrix) + frontend build/lint
├── docker-compose.yml            # Kafka (KRaft) + Postgres + both services
├── traffic-state-service/        # congestion state, JPA persistence, SSE, Kafka
│   └── src/main/java/.../{domain,application,infrastructure}
├── routing-service/              # Dijkstra GPS engine reacting to congestion
│   └── src/main/java/.../{domain,application,infrastructure}
└── frontend/                     # React + deck.gl live map (full OSM network + i18n)
    └── src/{types,services,store,components,data,i18n}
```
