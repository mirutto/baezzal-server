---
name: layer-application
description: >
  Apply the standard application-layer rules for `baezzal-community`,
  `baezzal-recommendation`, `baezzal-notification`, and `baezzal-media`. Use
  when creating or reviewing business flow orchestration, services, DTO
  files, and application events under `server.<feature>.application`. Do not
  use for `baezzal-feed`; use the feed-layer skill there instead.
---

# Application Layer

Use this skill with `layer-architecture` when working on
`server.<feature>.application`.

## Role

- `application` is where the business flow should be visible.
- This layer orchestrates use cases and coordinates lower-level collaborators.
- It is the boundary that translates between `presentation` contracts and `domain` models.

## Allowed Responsibilities

- Define application services.
- Express the business flow in readable order.
- Coordinate `implementation` collaborators.
- Convert between `domain` and DTO shapes.
- Publish or assemble application-level events.

## Required Conventions

- Service classes should end with `Service`.
- Group DTOs in `**Dtos` files.
- Group application events in `**Events` files.
- Follow `.codex/rules/dto-naming.md` for DTO suffix naming.
- Prefer secondary constructors and factory methods for DTO mapping.

## Guardrails

- Do not let this layer know which concrete infrastructure is used.
- Do not pull repository, cache, or external client details into application flow.
- If `infrastructure` depends on an application DTO, keep that DTO as a contract type only and avoid leaking infrastructure execution concerns back into the application service.
- Do not hide business flow behind overly generic helper abstractions.
- When detailed implementation logic starts to dominate a service, move it to `implementation`.

## Working Style

- Optimize for readability of the use-case flow.
- Keep each service method understandable from top to bottom.
- Use DTO mapping as a deliberate boundary, not as incidental conversion.
- If a conversion is repeated or non-trivial, centralize it in DTO constructors or factory methods instead of scattering field-by-field mapping.

## Enforcement Boundary

- Good candidates for ArchUnit:
  - `Service` class naming
  - `**Dtos` and `**Events` file grouping conventions when they are stable enough to encode
- Keep as documentation unless a stable rule emerges:
  - whether the business flow is readable enough
  - whether a service knows too much about implementation details
  - when DTO mapping is sufficiently centralized
