---
name: layer-infrastructure
description: >
  Apply the standard infrastructure-layer rules for `baezzal-community`,
  `baezzal-recommendation`, `baezzal-notification`, and `baezzal-media`. Use
  when creating or reviewing repositories, caches, and external adapters
  under `server.<feature>.infrastructure`. Do not use for `baezzal-feed`; use
  the feed-layer skill there instead.
---

# Infrastructure Layer

Use this skill with `layer-architecture` when working on
`server.<feature>.infrastructure`.

## Role

- `infrastructure` is the feature-owned wrapper around lower-level infrastructure concerns.
- It adapts persistence, cache, and external systems for the feature.
- It exists so upper layers do not talk to raw infrastructure details directly.

## Allowed Responsibilities

- Define repositories, caches, and external client adapters owned by the feature.
- Encapsulate feature-specific persistence or integration access.
- Translate low-level infrastructure behavior into a form usable by `implementation`.
- Depend on `application` DTOs when the adapter boundary intentionally uses those DTOs as its contract shape.

## Guardrails

- Do not push business flow here.
- Do not let upper layers depend on raw framework APIs when a feature adapter should exist.
- Keep infrastructure concerns explicit instead of smearing them across `application`.
- Do not depend on application services or use-case orchestration from this layer. The allowed upward dependency is DTOs only.
- If logic is about use-case sequencing rather than integration access, move it up out of this layer.

## Working Style

- Wrap infrastructure with feature-oriented interfaces or components where that improves clarity.
- Keep adapters thin but intentional.
- Favor names that describe the infrastructure role clearly, such as repository, cache, or a concrete external adapter.

## Enforcement Boundary

- Good candidates for ArchUnit:
  - repository and cache naming rules when the naming set is stable
- Keep as documentation unless a stable rule emerges:
  - how much wrapping is appropriate
  - where adapter logic stops and business logic starts
