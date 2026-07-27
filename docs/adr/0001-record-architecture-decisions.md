# ADR-0001: Record architecture decisions

- **Status:** Accepted
- **Date:** 2026-07-27

## Context

The README explains *what* the system does and `PROJECT_CONTEXT.md` states the rules the
implementation must follow ("the domain layer has no framework annotations", "do not use
WebSockets"). Neither explains *why* those rules exist.

That gap is expensive. A rule without a rationale is either followed blindly or discarded
the first time it becomes inconvenient — and a reader cannot tell a deliberate constraint
from an accident.

## Decision

Record every significant architecture decision as a short, numbered, immutable file in
`docs/adr/`, using a trimmed MADR format: Context, Decision, Consequences, Alternatives
considered.

A decision is significant when it is expensive to reverse, when it constrains future work,
or when a reasonable engineer would ask "why not the obvious alternative?".

Records are never edited to reflect a new direction. A changed decision gets a new ADR that
supersedes the previous one, so the reasoning trail survives.

## Consequences

- Rules in `PROJECT_CONTEXT.md` become traceable to a justification.
- Reviewers can challenge the reasoning instead of guessing at intent.
- Cost: every significant decision now carries a few minutes of writing.

## Alternatives considered

- **Rationale in code comments** — rejected: comments describe a single file, while these
  decisions cut across services, and comments are lost when the code they annotate is
  rewritten.
- **A single ARCHITECTURE.md** — rejected: one growing document loses the chronology, and
  editing it in place destroys the record of what was previously believed and why.
