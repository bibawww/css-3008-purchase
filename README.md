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
