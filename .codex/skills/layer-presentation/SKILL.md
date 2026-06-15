---
name: layer-presentation
description: >
  Apply the standard presentation-layer rules for `baezzal-community`,
  `baezzal-recommendation`, `baezzal-notification`, and `baezzal-media`. Use
  when creating or reviewing controllers, event handlers, request/response
  shapes, and other entrypoint code under `server.<feature>.presentation`. Do
  not use for `baezzal-feed`; use the feed-layer skill there instead.
---

# Presentation Layer

Use this skill with `layer-architecture` when working on
`server.<feature>.presentation`.

## Role

- `presentation` is the entrypoint layer.
- Typical entrypoints are REST APIs and event handlers.
- This layer receives external input, delegates to `application`, and returns application-facing results.

## Allowed Responsibilities

- Define controllers and event handler entrypoints.
- Validate and normalize transport-level input when needed.
- Call the appropriate `application` service.
- Return application DTOs or simple transport objects derived from application DTOs.

## Required Conventions

- Controller classes should end with `Controller`.
- Event handler classes should end with `EventHandler`.
- Use `application` DTOs as input and return types by default.

## Guardrails

- Do not use `domain` classes directly.
- Do not expose JPA entities from request bodies, response bodies, or handler contracts.
- Do not place business flow orchestration here.
- Do not let this layer decide which repository, cache, or external client is used.
- If mapping becomes business-oriented instead of transport-oriented, move it down to `application`.

## Working Style

- Keep entrypoint code thin.
- Make the handoff to `application` obvious.
- Favor explicit request handling over hidden side effects.
- When a handler grows beyond simple validation and delegation, extract the flow into `application`.

## Enforcement Boundary

- Good candidates for ArchUnit:
  - `Controller` and `EventHandler` class naming
  - no direct dependency on `domain`
- Keep as documentation unless a stable rule emerges:
  - how thin is thin enough
  - what should remain transport-level mapping
