---
name: feed-layer
description: >
  Apply the package and responsibility rules for `baezzal-feed`. Use when
  creating, moving, or reviewing code under `baezzal-feed/src/main/kotlin/server/feed`.
  This skill covers the feed-specific read-side structure:
  `presentation`, `application`, `query`, and `model`.
---

# Feed Layer

Use this skill when working inside `baezzal-feed`.

`feed` is a read-side module. It does not follow the standard
`presentation/application/implementation/infrastructure/domain` feature layout.

## Package Shape

- Feed code lives under `server.feed`.
- Classes must live inside one of these packages:
  - `server.feed.presentation`
  - `server.feed.application`
  - `server.feed.query`
  - `server.feed.model`
- `application` may have focused subpackages such as `application.event`.
- Do not place production classes directly under `server.feed`.

## Layer Responsibilities

- `presentation`
  - controllers and entrypoints only
  - delegates to `application`
- `application`
  - use-case orchestration
  - response/request DTOs
  - feed-scoped events
  - event publishers used by feed services
- `query`
  - DB reads
  - cursor paging
  - search/filter conditions
  - cache-backed read helpers
  - query composition primitives
- `model`
  - feed read-model entities mirrored from source tables
  - no API contracts here

## Dependency Rules

- `presentation` may access `application`.
- `application` may access `query`.
- `query` may access `model`.
- `model` must not depend on upper layers.
- `presentation` must not access `query` or `model` directly.
- `application` must not use source-module entities from `community`; use feed-owned read models only.

## Guardrails

- Keep feed as a read-side module. Do not move recommendation ranking logic here.
- Keep recommendation-derived tables and scoring logic out of `feed`; those belong in `baezzal-recommendation`.
- When source table shape changes, update matching `feed.model` entities explicitly.
- Keep cache and query optimization logic in `query`, not in `application`.
- Keep final response assembly in `application`, not in `query`.

## Naming Guidance

- Services end with `Service`.
- Query collaborators should use explicit names such as `FeedPostQuery`, `FeedTeamQuery`, or `FeedPostViewCache`.
- Events belong in `**Events` files.
- DTO grouping follows the existing `**Dtos` rule.

