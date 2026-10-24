# Pull request text

Replace the story IDs with your Jira IDs.

## Lab 2 — title: `CSS-XX join Spring Boot`

- [ ] Story IDs in the title
- [ ] Same product as Lab 1
- [ ] `domain` has no `org.springframework` import
- [ ] Two types implement `Rule` (`TransitionRule`, `UnapprovedCannotOrder`)
- [ ] `mvn -q verify` is green
- [ ] `mvn spring-boot:run` starts
- [ ] No secrets, no `.env`, no `target/` in git

## Lab 3 — title: `CSS-XX schema and JDBC`

- [ ] Story IDs in the title
- [ ] Same product as Labs 1 and 2
- [ ] Week 4 tests still green (status table + one mocked port)
- [ ] `CREATE TABLE` has a surrogate id, `UNIQUE` business key, `CHECK` on status
- [ ] `secondStatementRollsBack` → count is 0
- [ ] `secondRequestKeepsTheFirst` → count is 1
- [ ] `DuplicateRequest` is unchecked and lives in `domain`
- [ ] `schema.sql` applied on a clean database; repository uses JDBC and `?`
- [ ] `domain` has no `java.sql` and no `org.springframework`; no Flyway, no JPA
- [ ] `mvn -q verify` is green
- [ ] No REST controller, no cloud passwords, no `target/` in git
