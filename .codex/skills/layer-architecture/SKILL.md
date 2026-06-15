---
name: layer-architecture
description: >
  Apply the standard high-level layer package and dependency rules for
  `baezzal-community`, `baezzal-recommendation`, `baezzal-notification`, and
  `baezzal-media`. Use when adding, moving, or reviewing server-side feature
  code under those modules, or when updating related ArchUnit tests, package
  layout, and architecture constraints. Do not use for `baezzal-feed`; use
  the feed-layer skill there instead.
---

# Layer Architecture

Use this skill when working on feature code inside `baezzal-community`,
`baezzal-recommendation`, `baezzal-notification`, or `baezzal-media`.
For layer-specific implementation guidance, pair this skill with the relevant
`layer-presentation`, `layer-application`, `layer-implementation`,
`layer-infrastructure`, or `layer-domain` skill.

This skill defines only the large architectural rules. Do not infer extra naming
or style conventions beyond what is written here.

## Package Shape

- Feature code lives under `server.<feature>`.
- Classes must live inside one of these layer packages:
  - `server.<feature>.presentation`
  - `server.<feature>.application`
  - `server.<feature>.implementation`
  - `server.<feature>.infrastructure`
  - `server.<feature>.domain`
- Do not place production classes directly under `server.<feature>`.
- Treat these package rules as default constraints for all new feature code.

## Layer Responsibilities

- `presentation`: external entrypoints such as controllers.
- `application`: use-case orchestration and DTO-facing application services.
- `implementation`: internal feature collaborators that perform concrete work for application flows.
- `infrastructure`: persistence, cache, and external system adapters owned by the feature.
- `domain`: core domain models and domain logic.

## Dependency Rules

- `presentation` may access `application`.
- `application` may access `implementation` and `domain`.
- `implementation` may access `infrastructure` and `domain`.
- `infrastructure` may access `domain`.
- `infrastructure` may also access DTO classes in `application` when a feature adapter needs to consume or produce the application contract shape.
- Do not treat that DTO exception as permission for `infrastructure` to depend on application services or use-case flow.
- `domain` must not depend on other feature layers.
- Do not bypass intermediate layers just because a dependency is technically available.

Read the rules above as strict direction constraints, not as suggestions.

## Exposure Rules

- `presentation` must not depend on `domain` directly.
- Domain types must not be exposed from presentation-layer APIs.
- If a controller needs to return or accept data, introduce an application-facing DTO instead of exposing domain objects.
- DTO flow is the default public contract between `presentation` and `application`.

## Enforcement Boundary

- Good candidates for ArchUnit:
  - allowed layer packages
  - layer dependency direction
  - no production classes at `server.<feature>` root
  - no `presentation -> domain` dependency
- Keep as documentation unless a stable rule emerges:
  - nuanced responsibility boundaries
  - feature-specific exceptions

## Change Guidance

- When introducing a new feature, create the feature package first and place classes inside the correct layer from the start.
- When an existing class sits at `server.<feature>` root, move it into the appropriate layer before extending it.
- When adding architecture tests, prefer enforcing these structural rules before adding fine-grained naming rules.
- When a class starts carrying responsibilities from another layer, move the responsibility instead of normalizing the leak.

## Out of Scope For This Skill

- Detailed naming conventions for each layer
- Per-feature exceptions
- Transaction, persistence, or entity-specific modeling guidance
