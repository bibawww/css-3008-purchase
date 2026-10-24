# css-3008-purchase

## Product

We build a purchase-request tracker. People track **purchase requests** (a request to buy something for the company) from draft to order.

## Core item

A **purchase request**. It has an id (`RequestId`), a status (`RequestStatus`) and a policy that decides which status changes are allowed (`RequestPolicy`).

## Status table

Statuses: `DRAFT`, `APPROVED`, `ORDERED`.

| # | From | To | Result |
| --- | --- | --- | --- |
| 1 | DRAFT | APPROVED | allowed |
| 2 | APPROVED | ORDERED | allowed |
| 3 | DRAFT | ORDERED | forbidden |
| 4 | ORDERED | DRAFT | forbidden |

## Forbidden — why

- **DRAFT → ORDERED**: skipped approval. Nobody checked the budget, yet money would be spent.
- **ORDERED → DRAFT**: reopen after pay. The order is already placed; editing it as a draft would break the accounting.

Forbidden moves throw `IllegalStateException`.

## Run

```
java -version     # must print 21
mvn -q test
```

## Spring Boot (Lab 2)

`Application` starts the process. `RequestService` (`@Service`) gets a `Rule` injected. The rule chain is wired in `config/RuleConfig`.

Two `Rule` implementations in `domain`:

- `TransitionRule` — the status table above.
- `UnapprovedCannotOrder` — stop-factor: only an `APPROVED` request can be `ORDERED`.

`RequestPolicy` and both rules are plain Java: no `org.springframework` import.

### Package diagram

```
  dto          client        handler         config
  (JSON later) (HTTP later)  (HTTP week 9)   Application
                                             RuleConfig
                                             RequestService (@Service)
       \            \            /                |
        \            \          /           injects Rule
         \            \        /
                     domain
              RequestId  RequestStatus  RequestPolicy
              Rule  + TransitionRule + UnapprovedCannotOrder
                     (no Spring)
```

Arrows point inward. Domain never imports `org.springframework`.

### Run

```
mvn -q verify
mvn spring-boot:run
```

## Tests, SQL, JDBC (Lab 3)

PostgreSQL, `JdbcTemplate`, no JPA / Flyway / REST.

- `src/main/resources/db/schema.sql` — one table `purchase_request`: `id uuid` (surrogate key, from `RequestId`), `business_key` (`PR-19`, `UNIQUE NOT NULL`), `status` with `CHECK (status IN ('DRAFT','APPROVED','ORDERED'))`, `title`, `created_at`.
- `persistence/RequestJdbc` — `INSERT` and `count` with `?` placeholders (plus `findStatus`; unknown text throws).
- `config/RequestService` — `@Transactional`. `register` = one transaction per call. `insertTwice` = one transaction, two INSERTs.
- `domain/DuplicateRequest` — unchecked, so the transaction rolls back.
- `domain/ApprovalNotifier` — outbound port, mocked in `RequestServiceMockTest`.

| Test | Expected count |
| --- | --- |
| `secondStatementRollsBack` (`insertTwice("PR-19")`) | 0 |
| `secondRequestKeepsTheFirst` (`register` twice) | 1 |

The tests have no `@Transactional`, so they see committed data. Every test starts with `DROP TABLE` + `schema.sql` on an empty database.

### Database for local run

```
docker compose up -d      # only PostgreSQL: db css, user css, password css, port 5432
mvn -q verify
```

Or a local PostgreSQL install with the same db/user/password (classroom values, not secrets).

### Prove rollback in psql first

```sql
BEGIN;
INSERT INTO purchase_request (id, business_key, status, title)
VALUES ('11111111-1111-1111-1111-111111111111', 'PR-19', 'DRAFT', 'Laptops');
INSERT INTO purchase_request (id, business_key, status, title)
VALUES ('22222222-2222-2222-2222-222222222222', 'PR-19', 'DRAFT', 'Laptops again');
-- ERROR: duplicate key value violates unique constraint
ROLLBACK;
SELECT count(*) FROM purchase_request WHERE business_key = 'PR-19';  -- 0
```
