# Balcão

Cash management for small shops: everything that comes in and everything that goes out.
Record sales and expenses in reais, see the month's balance, and fix mistakes the way accountants do,
with reversals instead of edits.

**Stack:** Java 21 · Spring Boot 4 · Spring Modulith · PostgreSQL · Angular 21 · Testcontainers · ArchUnit

[![CI](https://github.com/assis-freitas/balcao/actions/workflows/ci.yml/badge.svg)](https://github.com/assis-freitas/balcao/actions/workflows/ci.yml)

<!-- TODO: screenshot -->

## Features

- **Income and expenses** with category, payment method (Pix, cash, debit, credit, boleto) and date.
- **Monthly balance:** income, expenses and net, month by month.
- **Reversals, not edits.** Entries are immutable. A mistake is cancelled by a reversal entry, so
  the history always adds up. An entry can be reversed once, and a reversal cannot be reversed.
- **Exact money.** Amounts are whole cents; "10,005" is rejected, never rounded.

### Roadmap

- [ ] **Receivables:** card sales become receivables net of fees, paid on D+1 / D+30
- [ ] **Till:** daily cash closing (opening float, counted cash, difference)
- [ ] **Reports:** monthly income statement (DRE simplificado), CSV/PDF export
- [ ] **Access:** shops, users and roles (owner sees reports, staff only records)

## Architecture

Domain-Driven Design with **hexagonal architecture** inside each bounded context, and each context
as a **Spring Modulith** module. See [ADR 0001](docs/adr/0001-ddd-and-hexagonal-architecture.md).

```
backend/src/main/java/dev/assisfreitas/balcao/
  shared/                  shared kernel: Money, BusinessId, PaymentMethod
  ledger/                  bounded context (Spring Modulith module)
    EntryRecorded …        public API: domain events other contexts listen to
    domain/                Entry (aggregate), Category, rules. Plain Java, no Spring/JPA
    application/
      port/in/             RecordEntryUseCase, ReverseEntryUseCase, LedgerQueries
      port/out/            EntryRepository, LedgerReadModel, CurrentBusiness, …
      *Service             use cases
    adapter/
      in/web/              REST API
      out/persistence/     JPA (write side) + JDBC (read side), Flyway schema
      config/              wiring
frontend/src/app/
  ledger/                  same context on the client: API, models, components
  shared/                  money parsing and formatting
```

- **The domain is plain Java.** `Entry` has no setters; `reverse()` returns a new entry.
  JPA entities live in the persistence adapter, with explicit mappers.
- **Ports and adapters.** Use cases depend only on ports, so they are tested with in-memory fakes.
- **Read and write sides are separate** (CQRS-lite): writes go through the aggregate, the
  statement screen reads from SQL shaped for it.
- **Rules enforced by tests:**
  [`ModularityTest`](backend/src/test/java/dev/assisfreitas/balcao/ModularityTest.java)
  (no module reaches into another's internals) and
  [`HexagonalArchitectureTest`](backend/src/test/java/dev/assisfreitas/balcao/HexagonalArchitectureTest.java)
  (the domain does not import Spring or JPA, use cases do not know adapters, driving adapters
  never touch driven ones).

## Running locally

Requires Java 21, Node 22+ and Docker (or Podman, see below).

```bash
cd backend && ./gradlew bootTestRun     # API on :8080, Postgres in a container
cd frontend && npm install && npm start # http://localhost:4200
```

`bootTestRun` starts PostgreSQL with Testcontainers, so there is nothing to install or configure.
Data lives as long as the container: stop the API and you start fresh.

**Podman:** point Testcontainers at the Podman socket first.

```bash
export DOCKER_HOST="unix://$(podman machine inspect --format '{{.ConnectionInfo.PodmanSocket.Path}}')"
export TESTCONTAINERS_RYUK_DISABLED=true
```

## Tests

```bash
cd backend && ./gradlew test             # domain, use cases, architecture, API against Postgres
cd frontend && npx ng test --watch=false
```

## License

[MIT](LICENSE)
