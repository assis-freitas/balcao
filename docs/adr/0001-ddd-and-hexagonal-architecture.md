# 1. DDD with hexagonal architecture, one Spring Modulith module per context

- Status: accepted
- Date: 2026-11-14

## Context

Balcão keeps a small shop's money: what came in, what went out, what the
card machine will pay and when, and whether the till closes right. Mistakes
here cost real money, so the rules (exact amounts, no edits to the past,
reversals instead of deletions) must live in one place, be easy to read, and
be tested without a server or a database.

The project is also a showcase for strict DDD, as a counterpart to
[Estante](https://github.com/assis-freitas/estante), which uses pragmatic DDD
in Laravel.

## Decision

**Bounded contexts are Spring Modulith modules**, one top-level package each:
`ledger` (now), then `receivables`, `till` (cash closing), `reports` and
`access`. Modulith verifies that no module reaches into another's internals and
that there are no cycles. Modules talk through **domain events** published in
each module's base package (its public API), for example `EntryRecorded`.

**Inside each module, hexagonal architecture:**

```
ledger/
  EntryRecorded, EntryReversed    public API (events)
  domain/                         aggregates, value objects, rules. Plain Java.
  application/
    port/in/                      use case interfaces (driving ports)
    port/out/                     what the use cases need (driven ports)
    *Service                      use case implementations
  adapter/
    in/web/                       REST controller, request/response DTOs
    out/persistence/              JPA entities + mappers (write), JDBC (read)
    out/events/, out/business/    other driven adapters
    config/                       wires use cases to adapters
```

- The **domain is plain Java**: no Spring, JPA or Hibernate. Aggregates are
  immutable where the business allows it (`Entry` has no setters at all).
- **JPA entities are separate from the domain model**, with explicit mappers.
  It costs a few classes; in return the domain is free of persistence concerns.
- **Use cases are plain classes**, wired in `adapter/config`. The only thing
  they borrow from Spring is `@Transactional`.
- **CQRS-lite**: writes go through aggregates; screens read from a JDBC read
  model shaped for them.
- **Money is `long` cents** in a `Money` value object, never `double`. Fractions
  of a cent are rejected, not rounded.
- **Time comes from an injected `Clock`** in the shops' time zone
  (America/Sao_Paulo), so "today" and "no future dates" are testable.
- Broken rules throw `BusinessRuleViolation` with a message for the shop owner;
  the web layer returns it as `422` ProblemDetail.

The rules are enforced by tests: `ModularityTest` (Spring Modulith) and
`HexagonalArchitectureTest` (ArchUnit).

## Consequences

- Domain and use case tests run in milliseconds with in-memory fakes.
  Integration tests run against real Postgres via Testcontainers.
- More classes than a layered CRUD (ports, mappers, DTOs). Accepted: it is the
  point of the exercise, and it keeps each context replaceable.
- Until the `access` context exists, the current shop comes from configuration
  (`ConfiguredBusiness`). Every table already carries `business_id`, so adding
  login changes one adapter, not the ledger.
