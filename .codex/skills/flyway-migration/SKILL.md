---
name: flyway-migration
description: >
  Create or update Flyway migrations for this repository. Use when a JPA
  entity changes, when table columns are added, removed, or constrained, when
  feed read models must stay in sync with source tables, or when destructive
  schema changes require cleanup DML before DDL.
---

# Flyway Migration

Use this skill whenever a schema-affecting change is made.

## Required Workflow

1. Identify every source-table change implied by the entity or code change.
2. Identify every mirrored read model affected in `baezzal-feed` and `baezzal-recommendation`.
3. Add or update the Flyway migration so schema and mirrored models stay aligned.
4. For destructive changes, add cleanup DML before destructive DDL.
5. Run the required project verification commands.

## Required Checks

- If a `community` JPA entity changes, verify whether `feed.model.*` or recommendation-side mirrored entities map the same table.
- If a `feed` or `recommendation` mirrored entity changes, verify the underlying source table still matches.
- Do not treat mirrored read models as incidental copies. They are schema consumers and must be updated deliberately.

## Destructive Change Rule

When a migration removes a table column, also include the necessary `DELETE` statements before the `ALTER TABLE ... DROP COLUMN` step when existing rows would violate the new schema or assumptions.

Examples of when cleanup DML is required:

- dropping a column that participates in a uniqueness or deduplication strategy and stale rows must be removed first
- removing rows that only existed to satisfy the old shape
- deleting invalid legacy rows before tightening nullability or constraints near the dropped column

Do not leave cleanup to manual operations when it can be encoded safely in the migration.

## Guardrails

- Keep migration order safe for production data.
- Prefer explicit forward-only migrations.
- Do not change entities without the matching migration.
- Do not change source-table schema and forget mirrored `feed` or `recommendation` entities.

## Verification

- Run `rtk ./gradlew lintKotlin --continue`
- Run `rtk ./gradlew test`
- Confirm the migration and all affected entity mappings describe the same final shape
