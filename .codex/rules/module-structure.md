# Baezzal Module Structure

This project is organized into role-based modules with clear responsibilities. The entire system starts from `baezzal-application`, and executable modules are started together within the same application process.

## Modules

### `config`

The `config` modules contain Spring beans for connection and infrastructure setup only.

- `baezzal-config:jpa` provides JPA-related configuration.
- `baezzal-config:mysql` provides MySQL connection configuration.
- `baezzal-config:redis` provides Redis connection configuration.
- `baezzal-config:minio` provides MinIO connection configuration.

These modules are not intended to contain domain logic. Their responsibility is limited to wiring external systems and exposing the related configuration beans.

### `platform`

The `platform` modules provide reusable functionality shared by executable modules.

- `baezzal-platform:cache` contains cache-related shared logic.
- `baezzal-platform:queue` contains queue-related shared logic.
- `baezzal-platform:set` contains shared set or collection-oriented utilities.
- `baezzal-platform:messaging` contains shared messaging support.
- `baezzal-platform:image` contains shared image-processing support.

These modules should stay domain-neutral. If functionality is shared across executable modules, prefer placing it here instead of duplicating it inside a domain module.

### `community`

`baezzal-community` contains the core write-side Baezzal domain logic.

This module owns the original domain data and write-side behavior for members, posts, follows, teams, collections, and related user interactions. It uses `config` modules for infrastructure wiring and `platform` modules for shared technical capabilities.

### `feed`

`baezzal-feed` contains read-side feed queries and display-oriented read models.

This module owns feed APIs, feed query composition, feed caching, and feed-specific read models that mirror source tables for read performance and display use cases.

Unlike the standard feature layering used in `community` and `recommendation`, `feed` uses a read-side package structure:

- `server.feed.presentation`
- `server.feed.application`
- `server.feed.query`
- `server.feed.model`

### `recommendation`

`baezzal-recommendation` contains recommendation signal and ranking-support logic.

This module owns derived recommendation data such as tag relations, user interests, and future recommendation-specific projections. It must not depend on `feed` or `community` executable modules directly. Event payload classes may be duplicated across modules when needed to preserve module independence.

### `media`

`baezzal-media` is a worker module responsible for thumbnail compression.

It owns the execution path related to media processing. In the current architecture, it is started together with other executable modules through the main `application` module.

### `application`

`baezzal-application` is the composition and bootstrap module.

It does not exist to hold core domain logic. Its main responsibility is to assemble executable modules and start them as a single application.

## Dependency Structure

The dependency flow is:

- `application -> community`
- `application -> feed`
- `application -> recommendation`
- `application -> media`
- `community -> config`
- `community -> platform`
- `feed -> config`
- `feed -> platform`
- `recommendation -> config`
- `recommendation -> platform`
- `media -> config`
- `media -> platform`

## Dependency Principles

- Keep `application` focused on composition and startup.
- Keep `config` limited to connection and infrastructure configuration beans.
- Keep `platform` limited to reusable, domain-neutral shared functionality.
- Keep domain-specific logic inside the executable module that owns it.
- Minimize direct coupling between executable modules.
- `community`, `feed`, and `recommendation` should not depend on each other directly.
- When a schema change affects original write-side tables, update `feed` read models and `recommendation` mirrored models explicitly instead of assuming they stay aligned automatically.
- When multiple executable modules need the same technical capability, prefer promoting it into `platform`.
