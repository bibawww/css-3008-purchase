# CSS-3008 · Lab 4 — REST contract and the Spring Boot platform

**Course:** Corporate Development (CSS-3008)  
**Weeks:** 9–10 · **Due:** week 10 · **Points:** 20  
**Language:** English (instruction) · Java 21 (code) · PostgreSQL

This file is the assignment. Upload it to the course platform. There is **no Java zip** for Lab 4. You work in the Git repository from Labs 1–3, on the **theme your team already chose**.

Week 8 was the midterm. This lab starts Rating 2. Security is Lab 5. Docker and the real outbound call are Lab 6.

---

## Your product

Keep the product from Labs 1–3. Use the nouns, statuses, and business key you already defend.

Do **not** start a second theme. The path, the JSON, and the error text use your names.

---

## What this lab is

Two parts, one hand-in.

| Week | You add | You do not add |
| --- | --- | --- |
| 9 | `POST` creates, `GET` reads. 201, 400, 404, 409. An error body. springdoc. | Roles, 401/403, Docker |
| 10 | URLs and timeouts in yaml. A `dev` profile. `/actuator/health` is 200. | A real HTTP call to another team, a Dockerfile |

The domain you defended stays Java. HTTP is an adapter. Another team never opens your IDE: they get a URL, a JSON body, and a status code.

---

## Hand in

1. The same semester Git repository and the same product as Labs 1–3.
2. `POST /api/v1/<your-resource>` creates one row. `GET /api/v1/<your-resource>/{id}` reads it. The id in the path is the UUID from Lab 3, not the business key.
3. Outcomes: **201** with `Location`, **400** `VALIDATION` that names the field, **404** `NOT_FOUND`, **409** `DUPLICATE`. A second create of the same business key leaves the first row (count is 1).
4. Every failure body is `code`, `message`, `correlationId`. No stack trace, no exception class name, no entity on the wire.
5. springdoc lists the operation: 201, 400, and 409 on create; 200 and 404 on get.
6. Domain has no `org.springframework`, no `ResponseEntity`, no `jakarta.validation`.
7. Datasource URL and one downstream base URL plus timeout live in yaml, bound by `@ConfigurationProperties`. The `dev` profile runs on localhost. Java has no URL string literal.
8. `GET /actuator/health` is **200** and the status is UP. `env`, `beans`, `heapdump`, and `shutdown` are not exposed.
9. `mvn -q verify` is green. A web slice test locks 201, 400, 404, and 409.

---

## Week 9 — the contract

### 1. Dependencies

Parent stays Spring Boot **3.4.x** (the one you already run). Add `spring-boot-starter-web`, `spring-boot-starter-validation`, and `springdoc-openapi-starter-webmvc-ui` **2.8.x**.

`starter-web` does not bring the validator. Without `starter-validation`, `@Valid` does nothing. springdoc 2.8.x matches Boot 3.4. The 3.x line is Boot 4.

### 2. One resource, two verbs

| Call | Meaning | Not this |
| --- | --- | --- |
| `POST /api/v1/<your-resource>` | Create one item | A path that hides the verb (`/create…`) |
| `GET /api/v1/<your-resource>/{id}` | Read that item | A GET that also changes status |
| `PUT`, `PATCH`, `DELETE` | Not this lab | Closing the item by deleting the row |

The version sits in the path: `/api/v1`. A query `?v=2` is a version one client will forget.

The resource name follows your table. The path id is the UUID. The business key stays in the body.

### 3. The controller chooses the status. The service still inserts.

Create goes through the Lab 3 register. The controller does not touch `EntityManager`.

A good create returns **201**, a `Location` header whose path is the GET for that UUID, and a response record: id, business key, status token, title. Status is the token your `CHECK` already stores.

GET loads by that UUID. It does not register, move, or flush. A missing row throws your domain “not found” exception. The advice turns that into 404.

### 4. JSON types live next to the controller

The request record carries the business key and the title. `@NotBlank` on both. `@Size(max = 120)` on the title. Those annotations stay in the web package.

The response record is id, business key, status, and title. Jackson must not bind the JPA entity. A persistence field on the wire makes the table shape public.

The error record is `code`, `message`, `correlationId`.

Domain has no `jakarta.validation` import.

### 5. Pick the code before you write the message

| Situation | Status | `code` |
| --- | --- | --- |
| Row inserted | 201 | — body is the item |
| Title or business key blank, or title longer than 120 | 400 | `VALIDATION` |
| Path id is not a UUID, or the body is not JSON | 400 | `VALIDATION` |
| UUID parses and is not in the table | 404 | `NOT_FOUND` |
| Business key already stored | 409 | `DUPLICATE` |
| Wrong `Content-Type` | 415 | `UNSUPPORTED` |
| A bug you did not map | 500 | `INTERNAL` |

A 200 whose body hides an error is a bug. The status is the outcome.

409 is the duplicate exception from Lab 3, now visible to the caller. The first committed row stays. Count for that key is still 1.

| After this call | Rows for that business key |
| --- | --- |
| 201 with a real title | 1 |
| 400 title blank | 1, if a row already existed; 0 if this was the first attempt |
| 200 GET by UUID | unchanged |
| 409 same key again | 1 |
| 201 on a different key | 1 for the first key, 1 for the new key |

### 6. One advice class maps exceptions to the error record

Domain exceptions stay in `domain`, unchecked, with no Spring import. Only the advice knows 400, 404, and 409.

Map these, or the client sees Boot's error shape instead of your error record:

- Bean validation failure → 400, `VALIDATION`. The message names the field, then the constraint text (`title must not be blank`).
- Your missing-item exception → 404, `NOT_FOUND`.
- Your duplicate exception → 409, `DUPLICATE`.
- A path id that is not a UUID → 400, `VALIDATION`, message `id must be a UUID`. 404 stays reserved for a UUID that parses and is missing.
- A body that is not JSON → 400, `VALIDATION`, message `body must be JSON`.
- Unsupported media type → 415, `UNSUPPORTED`.
- Anything else → 500, `INTERNAL`, message `unexpected error`. Log the exception with the correlation id. The exception text does not go in the body.

Read `X-Correlation-Id`. If the caller sent one, the error body repeats it. If the header is missing or blank, mint a new id. Week 13 moves this into a filter. This week the advice is enough.

Turn Boot's default leak off. Week 10 puts this in yaml: do not include the stack trace, the exception message, or binding errors in the default error payload.

### 7. Five bodies. Only one of them is a row.

| POST body | Status | message |
| --- | --- | --- |
| title is empty | 400 | title must not be blank |
| title is only spaces | 400 | title must not be blank |
| business key is empty | 400 | business key must not be blank |
| title is 121 characters | 400 | size must be between 0 and 120 |
| title is 120 characters | 201 | your first status, Location, full title |

Validation runs before the service method. The service does not run.

### 8. springdoc lists what you shipped

Name the statuses on the create operation: 201, 400, and 409. Name 200 and 404 on get.

Boot, then open `/swagger-ui.html`. The spec is `/v3/api-docs`. If 409 is missing here, it is missing from the contract.

The spec has to show:

- POST and GET on your resource.
- Business key and title, both required.
- 201, 400, and 409 on create. 200 and 404 on get.
- Response fields: id, business key, status, title. The JPA entity is not a schema.

### 9. An optional field stays on v1. A rename is v2.

You do not have to ship a v2 in this lab. You do have to leave `/api/v1` compatible.

| Still `/api/v1` | Ship `/api/v2` and leave v1 alone |
| --- | --- |
| Add an optional field old clients can ignore | Rename `title` |
| Clearer message text | 201 becomes 200 with no `Location` |
| | A status token becomes a number |

### 10. Lock it with a slice test

`@WebMvcTest` on the controller. Mock the service. Do not boot PostgreSQL for this test. Do not call a real HTTP server.

Assert:

- A valid body → **201**, `Location` ends with the id, body has the business key and the first status.
- A blank title → **400**, `code` is `VALIDATION`, `message` contains the field name. The service mock was not called.
- Service throws your missing type → **404**, `code` is `NOT_FOUND`.
- Service throws your duplicate type → **409**, `code` is `DUPLICATE`.
- GET with the business key in the path → **400**, message says the id must be a UUID.
- `X-Correlation-Id` on a 400 comes back as `correlationId`.

The Lab 3 tests still prove the row: a second register of the same key leaves count 1. On a running app, the second POST of that key is 409 and the count for that key is still 1. Say so in the pull request.

### Where the code lives

| Package | Holds |
| --- | --- |
| domain | Id, status, policy, duplicate and missing exceptions. No Spring, no `ResponseEntity`, no `jakarta.validation`. |
| handler | Controller, request record, response record, error record, advice. Chooses 201, 400, 404, 409, 415, 500. |
| persistence | The entity. Jackson never sees it. |

---

## Week 10 — config, profiles, health

Config is not a second hello-world. The process from Lab 2 now reads its URLs from yaml.

### 1. Nothing that varies is a Java string

Datasource URL, username, password, and one downstream base URL plus timeout live in yaml. A reviewer who greps `src/main/java` for `jdbc:postgresql` or `http://` finds nothing.

Bind the downstream base URL and timeout with `@ConfigurationProperties` (prefix `app.downstream`). Register that type and inject it where the outbound port is configured. Do not call the network. Lab 6 makes the call and enforces the timeout. This week the value is bound, and a test shows it is not hardcoded.

Timeout is a duration (`2s`), not an int you multiply by 1000 in Java.

### 2. Two profiles

`application.yaml` holds what every run shares: JPA `ddl-auto: validate`, `open-in-view: false`, the error leak settings, actuator exposure, Flyway. It does not hold a password.

`application-dev.yaml` is the laptop: PostgreSQL on localhost, database `css`, user `css`, password `css`, downstream base URL on localhost port 9090, timeout 2s. That password is the classroom default from Lab 3. Do not commit a cloud password.

`application-prod.yaml` reads `CSS_DB_URL`, `CSS_DB_USER`, and `CSS_DB_PASSWORD`. No default secret.

`spring.profiles.default: dev` so a local boot with no extra flag uses the laptop database. Production sets `SPRING_PROFILES_ACTIVE=prod` and the three env vars. Do not set the active profile to prod in the committed file.

### 3. Actuator

Add `spring-boot-starter-actuator`.

Expose only `health` and `info`. Health details stay hidden, so a health payload does not list datasource URLs. Info carries your product name.

| Call | Result |
| --- | --- |
| `GET /actuator/health` | **200**, status UP |
| `GET /actuator/info` | **200**, your app name |
| `GET /actuator/env` | **404** (not exposed) |
| `GET /actuator/heapdump` | **404** |

### 4. Prove the profile

A `@SpringBootTest` on the `dev` profile loads the context and reads the downstream properties: base URL is the dev value, timeout is 2 seconds. The test does not open a socket to port 9090.

`mvn -q verify` stays green against PostgreSQL. The Lab 3 counts are unchanged.

---

## Do not

- Return the JPA entity. Jackson must not walk persistence fields.
- A 200 whose body hides an error. Use 4xx and a `code`.
- A create path that hides the verb, or a GET that changes the row.
- `ResponseEntity`, `HttpStatus`, or `jakarta.validation` inside `domain`.
- A stack trace, exception class name, or binding dump in the JSON.
- Hardcoded JDBC URLs or `http://` literals in `src/main/java`.
- Exposing `env`, `beans`, `heapdump`, or `shutdown`.
- Spring Security, roles, 401, or 403. That is Lab 5.
- A Docker image of the app, or a real call to port 9090. That is Lab 6.
- `PUT`, `PATCH`, `DELETE`, or a list endpoint. One create and one read.
- A new product or a second Git repo.
- Editing Flyway `V1`. The schema from Lab 3 stays.

---

## Done when

- springdoc lists create and get. 400 names the field. 404 and 409 use your error record.
- No URL is hardcoded in Java. The `dev` profile runs on localhost. `prod` reads env vars.
- `/actuator/health` is 200 on a running app. Clients never see a stack trace.
- The web slice test is green, and the Lab 3 duplicate still leaves one row.
- `mvn -q verify` is green.

---

## PR checklist (paste into the pull request)

- [ ] Story IDs in the title
- [ ] Same product as Labs 1–3
- [ ] `POST` and `GET` under `/api/v1/<your-resource>`. Path id is a UUID.
- [ ] 201 + `Location`, 400 `VALIDATION` (field name), 404 `NOT_FOUND`, 409 `DUPLICATE`
- [ ] Second POST of the same business key is 409 and the row count stays 1
- [ ] Error JSON is only `code`, `message`, `correlationId`. Stack traces stay off the wire
- [ ] Response type is not the entity. Domain has no Spring and no `jakarta.validation`
- [ ] `/swagger-ui.html` shows 201, 400, 409 on create and 200, 404 on get
- [ ] `@WebMvcTest` covers 201, 400, 404, 409, and the correlation header
- [ ] Datasource and `app.downstream` (base URL + timeout) come from yaml
- [ ] `dev` profile is localhost `css`/`css`. `prod` uses env vars. No cloud password in git
- [ ] `GET /actuator/health` is 200. `env` and `heapdump` are 404
- [ ] Lab 3 tests still green. `mvn -q verify` is green
- [ ] No Spring Security, no Dockerfile, no `target/` in git
