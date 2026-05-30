---
name: layer-domain
description: >
  Apply the domain-layer rules for `baezzal-curation`. Use when creating or
  reviewing JPA entities and domain models under `server.<feature>.domain`.
---

# Domain Layer

Use this skill with `layer-architecture` when working on
`server.<feature>.domain`.

## Role

- `domain` contains the feature's JPA entities.
- It represents the core domain model owned by the feature.

## Allowed Responsibilities

- Define JPA entities.
- Hold domain state and domain-level behavior that naturally belongs to the entity model.

## Guardrails

- Do not expose domain entities directly from `presentation`.
- Do not make `domain` depend on `presentation`, `application`, `implementation`, or `infrastructure`.
- Keep transport concerns and application DTO concerns out of this layer.

## Working Style

- Treat this layer as the model boundary, not as an API contract.
- When data must cross out to upper layers, let `application` translate it into DTOs.
- Keep persistence-backed domain modeling here and move orchestration elsewhere.

## Enforcement Boundary

- Good candidates for ArchUnit:
  - no dependency from `domain` to upper layers
- Keep as documentation unless a stable rule emerges:
  - what behavior naturally belongs on the entity
  - how much domain logic should remain inside the model
