# Baezzal Module Structure

This project is organized into role-based modules with clear responsibilities. The entire system starts from `baezzal-application`, and both executable modules, `curation` and `media`, are started together within the same application process.

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

### `curation`

`baezzal-curation` contains the core Baezzal domain logic.

This module owns the business rules, domain flows, and application behavior specific to the Baezzal curation domain. It uses `config` modules for infrastructure wiring and `platform` modules for shared technical capabilities.

### `media`

`baezzal-media` is a worker module responsible for thumbnail compression.

It owns the execution path related to media processing. In the current architecture, it is started together with `curation` through the main `application` module.

### `application`

`baezzal-application` is the composition and bootstrap module.

It does not exist to hold core domain logic. Its main responsibility is to assemble executable modules and start them as a single application. At the moment, it starts both `curation` and `media` together.

## Dependency Structure

The dependency flow is:

- `application -> curation`
- `application -> media`
- `curation -> config`
- `curation -> platform`
- `media -> config`
- `media -> platform`

## Dependency Principles

- Keep `application` focused on composition and startup.
- Keep `config` limited to connection and infrastructure configuration beans.
- Keep `platform` limited to reusable, domain-neutral shared functionality.
- Keep domain-specific logic inside the executable module that owns it.
- Minimize direct coupling between executable modules.
- When multiple executable modules need the same technical capability, prefer promoting it into `platform`.
