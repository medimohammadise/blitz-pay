# Liquibase Best Practices

Authoritative reference for database schema migration conventions in this project.
See `CONTRIBUTING.md` for the link to this document.

---

## Current State

This project now uses **Liquibase** to own schema changes and **Hibernate `ddl-auto: validate`**
to verify mappings at startup.

Use Liquibase for every schema change:
- Add a new versioned changeset, never rely on Hibernate to mutate schema
- Keep rollback instructions with each changeset
- Preserve an auditable, replayable schema history across environments

---

## 1. Migration from `ddl-auto: update` to Liquibase

### Step 1 — Add Liquibase dependency

```kotlin
// build.gradle.kts
dependencies {
    implementation("org.liquibase:liquibase-core")
}
```

### Step 2 — Generate baseline from existing schema

Export the current schema from your running PostgreSQL database as the baseline
changelog. This represents "everything that exists before Liquibase took over."

```bash
# On the server — dump current schema (no data)
pg_dump --schema-only --no-owner --no-acl \
  -U postgres quickpay_db > baseline.sql
```

Convert to a Liquibase-formatted changelog and place it at:
```
src/main/resources/db/changelog/0001-baseline.sql
```

Mark the baseline changeset with `runOnChange: false` and `runAlways: false`.

### Step 3 — Update `application.yml`

```yaml
spring:
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.yaml
    enabled: true
  jpa:
    hibernate:
      ddl-auto: validate   # ← change from 'update' to 'validate'
```

`validate` lets Hibernate check that the schema matches the entity model,
but never modifies the schema. Liquibase owns all schema changes from now on.

---

## 2. Changelog File Structure

Use a master changelog that includes individual versioned files.

```
src/main/resources/db/
└── changelog/
    ├── db.changelog-master.yaml     ← master index, includes all others
    ├── 0001-baseline.sql            ← initial schema snapshot
    ├── 0002-add-invoice-table.sql
    ├── 0003-add-payment-status-index.sql
    └── 0004-rename-order-id-column.sql
```

**`db.changelog-master.yaml`:**

```yaml
databaseChangeLog:
  - includeAll:
      path: db/changelog/
      relativeToChangelogFile: false
      filter: liquibase.resource.DirectoryResourceAccessor
```

Or use explicit includes for deterministic ordering:

```yaml
databaseChangeLog:
  - include:
      file: db/changelog/0001-baseline.sql
  - include:
      file: db/changelog/0002-add-invoice-table.sql
```

---

## 3. Changeset Naming Convention

```
{sequence}-{description}.sql
```

- `{sequence}` — zero-padded 4-digit number: `0001`, `0002`, `0100`
- `{description}` — short kebab-case description of what the migration does

Examples:
```
0001-baseline.sql
0002-add-invoice-table.sql
0003-add-payment-request-status-index.sql
0004-rename-buyer-tax-id-to-vat-id.sql
0050-add-webhook-events-table.sql
```

---

## 4. SQL Changeset Format

Use SQL format for changesets — it's readable, diffable, and works with any database tool.

```sql
-- liquibase formatted sql

-- changeset dev:0002-add-invoice-table
CREATE TABLE invoices (
    id              UUID        NOT NULL PRIMARY KEY,
    invoice_number  VARCHAR(50) NOT NULL UNIQUE,
    issue_date      DATE        NOT NULL,
    due_date        DATE        NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    amount_cents    BIGINT      NOT NULL,
    currency        CHAR(3)     NOT NULL DEFAULT 'EUR',
    buyer_vat_id    VARCHAR(30),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
-- rollback DROP TABLE invoices;

-- changeset dev:0002-add-invoice-table-index
CREATE INDEX idx_invoices_status ON invoices (status);
-- rollback DROP INDEX idx_invoices_status;
```

**Rules:**
- Every `changeset` must have a `-- rollback` comment describing how to undo it
- `id` format: `{author}:{sequence}-{description}` — makes it unique in the `DATABASECHANGELOG` table
- Use `TIMESTAMPTZ` (timestamp with timezone) for all timestamp columns — never `TIMESTAMP`
- One logical change per changeset (table creation, index, column add — each gets its own)

---

## 5. Never Modify an Applied Changeset

Once a changeset has been applied to any environment (dev, staging, prod), never edit it.
Liquibase checksums the changeset content and will fail on startup if it detects a change.

**Instead**, add a new changeset:

```sql
-- Wrong: editing 0002 after it has been applied
-- Right: add a new changeset

-- changeset dev:0004-rename-buyer-tax-id-to-vat-id
ALTER TABLE invoices RENAME COLUMN buyer_tax_id TO buyer_vat_id;
-- rollback ALTER TABLE invoices RENAME COLUMN buyer_vat_id TO buyer_tax_id;
```

---

## 6. Rollback Strategy

Always write rollback instructions in every changeset. They enable:
- Fast recovery from a bad deployment
- Local dev iteration without resetting the database
- CI environment teardown

```bash
# Roll back the last 1 changeset
liquibase rollbackCount 1

# Roll back to a specific tag
liquibase rollback v1.2.3
```

Tag a release before deploying:
```bash
liquibase tag v1.2.3
```

---

## 7. Application Startup Behavior

With Liquibase enabled:
1. On startup, Spring runs all pending changesets in order before the application context
   fully starts
2. If a changeset fails, the application fails to start — schema and app are always in sync
3. The `DATABASECHANGELOG` table tracks what has been applied and when

This replaces the non-deterministic behaviour of `ddl-auto: update`.

---

## 8. Running Liquibase in Tests

For contract tests (which mock the datasource), Liquibase must be disabled:

```yaml
# src/contractTest/resources/application-contract-test.yml
spring:
  liquibase:
    enabled: false
  jpa:
    hibernate:
      ddl-auto: none
```

For integration tests that use a real database (Testcontainers), Liquibase runs normally —
this is the correct behavior since it validates the migration against a real PostgreSQL instance.

---

## References

- Liquibase docs: https://docs.liquibase.com
- Spring Boot Liquibase integration: https://docs.spring.io/spring-boot/reference/data/sql.html#data.sql.liquibase
- SQL changelog format: https://docs.liquibase.com/concepts/changelogs/sql-format.html
