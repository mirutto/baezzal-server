---
name: layer-implementation
description: >
  Apply the implementation-layer rules for `baezzal-curation`. Use when
  creating or reviewing internal feature collaborators that support
  application-layer flows under `server.<feature>.implementation`.
---

# Implementation Layer

Use this skill with `layer-architecture` when working on
`server.<feature>.implementation`.

## Role

- `implementation` exists so `application` can stay focused on business flow.
- This layer contains concrete feature-level collaborators that perform detailed work.
- It can depend on `infrastructure` and `domain` to complete that work.

## Allowed Responsibilities

- Execute one focused kind of action for the feature.
- Encapsulate detailed read/write/update/remove or side-effect-heavy logic.
- Hide infrastructure usage details from `application`.

## Required Conventions

- One implementation component should represent one action category.
- Use `**er` naming for action-oriented components.
- CRUD defaults:
  - create: `**Writer`
  - read: `**Reader`
  - update: `**Updater`
  - delete: `**Remover`
- Other action names such as `Publisher` or `Uploader` are acceptable when they describe one concrete action clearly.

## Guardrails

- Do not turn this layer into a miscellaneous utility bucket.
- Do not mix multiple unrelated actions into one component.
- Do not leak infrastructure choices back into `application`.
- If the class is really expressing use-case flow rather than detailed execution, it likely belongs in `application`.

## Working Style

- Keep each component narrow and explicit.
- Name components by the action they own.
- Prefer composing several focused implementation components over building one large manager-style class.

## Enforcement Boundary

- Good candidates for ArchUnit:
  - action-oriented `**er` naming once the allowed names are stable
- Keep as documentation unless a stable rule emerges:
  - one component owning one action category
  - whether a class became too broad and should be split
