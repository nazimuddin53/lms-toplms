# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Context

**toplms** is a Learning Management System (LMS) being built as a **multi-tenant, multi-database SaaS** product. The owner is new to Spring and is using this project as a learning vehicle, so prefer explanations alongside changes — surface the *why* (Spring lifecycle, JPA semantics, transactional boundaries, tenant isolation trade-offs) rather than just producing code. When introducing a new Spring concept (e.g. `@Transactional`, `AbstractRoutingDataSource`, `@ConfigurationProperties`), briefly explain it the first time it appears.

## Tech Stack

- **Java 17** (toolchain pinned in `build.gradle`)
- **Spring Boot 4.0.6** with Spring Web MVC and Spring Data JPA
- **Spring AI 2.0.0-M5** with the Anthropic Claude starter (`spring-ai-starter-model-anthropic`)
- **PostgreSQL** as the runtime database driver
- **Gradle** (Groovy DSL) — wrapper checked in
- **JUnit 5** via `useJUnitPlatform()`

## Common commands

Use the Gradle wrapper (`./gradlew`) — never a system-installed Gradle.

| Task | Command |
| --- | --- |
| Run the app | `./gradlew bootRun` |
| Build (compile + test + jar) | `./gradlew build` |
| Compile only | `./gradlew classes` |
| Run all tests | `./gradlew test` |
| Run a single test class | `./gradlew test --tests com.toplms.ToplmsApplicationTests` |
| Run a single test method | `./gradlew test --tests 'com.toplms.ToplmsApplicationTests.contextLoads'` |
| Continuous test re-run | `./gradlew test --continuous` |
| Clean build artifacts | `./gradlew clean` |
| Build OCI image | `./gradlew bootBuildImage` |
| Show dependency tree | `./gradlew dependencies` |

Test reports land in `build/reports/tests/test/index.html`.

## Architecture notes

The codebase is currently a Spring Boot skeleton (`ToplmsApplication` + `application.yaml` + a context-loads smoke test). The architectural decisions below are *target state* for the SaaS LMS — code added to this repo should move toward them, not away.

### Multi-tenancy model

Two dimensions to keep distinct, because Spring/Hibernate handle them differently:

1. **Tenant resolution** — how an inbound HTTP request is mapped to a tenant. Path-based for now (`/{slug}/...`); subdomain or JWT claim later. Implement once as a `CurrentTenantIdentifierResolver` (Hibernate) plus a servlet `Filter` / `HandlerInterceptor` that stashes the tenant in a `ThreadLocal` (the project's `TenantContext` holder) for the duration of the request. Clear the `ThreadLocal` in a `finally` block — leaks across thread-pool reuse are the classic multi-tenant bug.
2. **Tenant isolation strategy** — **single shared database with `tenant_id` discriminator column.** Enforced at the ORM level via Hibernate's `@TenantId` annotation (Hibernate 6.2+): a `@TenantId String tenantId` field on every tenant-scoped entity makes Hibernate auto-apply `WHERE tenant_id = ?` to every query and auto-fill the column on every insert. Never write the predicate by hand in `@Query` — a forgotten filter is a cross-tenant data leak.

A `tenant` table (in the same shared database) stores the tenant registry: id, slug, status, plan, created_at. No per-tenant `DataSource`, no `MultiTenantConnectionProvider` — one `HikariCP` pool, one `EntityManagerFactory`. The control-plane tables (`tenant`, `superadmin_user`, `plan`) sit alongside tenant-plane tables (`course`, `enrollment`, …) in the same DB, distinguished only by the *absence* of a `tenant_id` column. Splitting the control plane into its own DB is a deferred refactor.

**Flyway** runs as Spring Boot's auto-migration against the single `DataSource`. One migration folder: `src/main/resources/db/migration/`. New tenant signup is an `INSERT` into the `tenant` table — no `CREATE DATABASE`, no per-tenant Flyway loop.

### Layering convention to adopt

As features land, keep the standard Spring layering so the tenant context flows cleanly:

```
controller (web)  →  service (@Transactional)  →  repository (Spring Data JPA)  →  entity
```

`@Transactional` boundaries belong on the service layer. The tenant `ThreadLocal` must be set *before* the transaction begins, otherwise Hibernate opens a connection on the wrong (or no) `DataSource`.

### Spring AI

`spring-ai-starter-model-anthropic` is on the classpath, so AI features (e.g. tutoring assistant, question generation, content summarisation) are expected. Configure via `spring.ai.anthropic.api-key` — keep the key in env vars or a secrets manager, never `application.yaml`. AI calls should be tenant-scoped for billing/quota reasons; consider wrapping the `ChatClient` so every call records `tenantId`.

## Configuration

`src/main/resources/application.yaml` only sets the application name today. As config grows, prefer:

- `application.yaml` for shared defaults
- `application-{profile}.yaml` (e.g. `application-dev.yaml`, `application-prod.yaml`) for env-specific overrides — activate with `SPRING_PROFILES_ACTIVE=dev`
- `@ConfigurationProperties` classes (with `@Validated`) over scattered `@Value` injections

The PostgreSQL driver is `runtimeOnly`, so the app will fail to start without `spring.datasource.*` (or per-tenant equivalents) configured.

## Build artifacts

Do not commit `build/` or `.gradle/`. The `.gitignore` already covers them.
