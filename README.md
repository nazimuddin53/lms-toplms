# toplms

A **multi-tenant Learning Management System (LMS)**, built as a SaaS product on
Spring Boot. Each business signs up, gets its own isolated workspace, and manages
its own users, roles, courses, and learners.

> Early development. Signup, tenant provisioning, login, roles, a tenant admin
> dashboard, and management of courses/teachers/students work end to end.
> Enrolments and the learner-facing experience are next.

For deep detail (data model, security beans, URL map, glossary) see
[`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md). This README focuses on **how the
project works** — both the runtime flow and the development workflow.

---

## How it works (runtime working process)

The platform serves three audiences, kept apart by URL and by role:

```
 PUBLIC                      TENANT WORKSPACE                 (future) PLATFORM
 visitors                    a business's own users           superadmins
 ───────                     ─────────────────────            ─────────────
 /  /signup  /login          /app/profile  /app/admin/**      /admin/**  (not built)
```

### The end-to-end lifecycle

```
1. SIGN UP  (a business onboards)
   Visitor → GET /signup → fills the form → POST /signup
      → TenantProvisioningService (one DB transaction):
            • creates the Tenant (status ACTIVE, unique slug e.g. "acme")
            • seeds roles ADMIN, INSTRUCTOR, LEARNER for that tenant
            • creates the founding user with the ADMIN role (password BCrypt-hashed)
      → redirect to /login?registered

2. LOG IN  (role decides where you land)
   User → GET /login → POST /login (handled by Spring Security, not a controller)
      → credentials checked against the BCrypt hash
      → RoleBasedAuthenticationSuccessHandler:
            ADMIN  → /app/admin    (admin dashboard)
            other  → /app/profile

3. ADMINISTER  (the ADMIN runs the workspace, via a sidebar)
   /app/admin            overview: course / teacher / student counts
   /app/admin/courses    list + create courses
   /app/admin/teachers   list + add teachers  (users with the INSTRUCTOR role)
   /app/admin/students   list + add students  (users with the LEARNER role)
   /app/admin/roles      the tenant's roles
      • Teachers/Students are just tenant users filtered by role; the section
        decides the role, so those forms have no role field.
      • Everything is scoped to the admin's own tenant (taken from the principal,
        never the form); new accounts get a BCrypt-hashed password.

4. ACCESS CONTROL  (enforced server-side)
   /app/admin/** requires ROLE_ADMIN. A logged-in INSTRUCTOR/LEARNER hitting it
   gets 403; an anonymous visitor is redirected to /login.
```

Each business's data is partitioned by a `tenant_id` foreign key in a single
shared PostgreSQL database (single-DB multi-tenancy).

---

## Run it locally

**Prerequisite:** PostgreSQL database `toplms_db` on `localhost:5432` (credentials
in `app/src/main/resources/application.yaml`).

```bash
./gradlew bootRun        # start the app at http://localhost:8080
```

Then walk the working process yourself:

1. Open `http://localhost:8080/` → **Get started**
2. Fill the signup form → you're redirected to **/login**
3. Sign in with the email/password you just chose
4. As the founding **ADMIN** you land on the **admin dashboard** (`/app/admin`)
5. Use the sidebar: **Courses** (add a course), **Teachers** / **Students** (add
   people). Then sign out and log in as a teacher to see the non-admin
   experience (they land on `/app/profile`, and `/app/admin` returns 403).

> If a previous run is still up, stop it first (Ctrl+C) — otherwise port 8080 is
> held by old code and you'll be testing a stale build.

---

## Project structure

Two Gradle modules (plus a root aggregator):

```
engine/   core library — domain entities, repositories, services. No web, no main().
app/      Spring Boot app — controllers, Thymeleaf templates, security. Depends on engine.
```

The dependency only points one way (`app → engine`), so business logic can never
depend on the web layer. See [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) §2.

---

## Development workflow

### Everyday commands

| Task | Command |
| --- | --- |
| Run the app | `./gradlew bootRun` |
| Compile only (fast feedback) | `./gradlew classes` |
| Build (compile + test + jar) | `./gradlew build` |
| Run tests | `./gradlew test` |
| Clean | `./gradlew clean` |

Use the wrapper (`./gradlew`), never a system Gradle.

### How a feature flows through the layers

When adding a feature, work from the inside out and keep each concern in its layer:

```
1. engine/domain      @Entity            — the data
2. engine/repository  JpaRepository      — how to load/save it
3. engine/service     @Service @Transactional — the business rules (one transaction)
4. app/web            @Controller + form DTO  — the HTTP boundary (validate input)
5. app/templates      Thymeleaf .html    — the page
6. app/config         SecurityConfig     — who is allowed in
```

Rules of thumb that keep the codebase consistent:

- **`@Transactional` lives on services**, never controllers or repositories.
- **Web forms are DTOs in `app/`**, translated into domain *command* objects before
  calling a service — entities and HTTP forms never share a class.
- **Never trust the client**: re-check authorization/ownership in the service
  (e.g. a submitted `roleId` must belong to the caller's tenant).
- **Validate at the boundary**: `@Valid` on the form DTO; business rules
  (uniqueness, etc.) in the service.

### Schema

Currently generated by Hibernate (`spring.jpa.hibernate.ddl-auto: update`). The
production path is Flyway migrations with `ddl-auto: validate`.

---

## Tech stack

Java 17 · Spring Boot 4.0.6 (Web MVC, Data JPA, Security) · Thymeleaf ·
PostgreSQL · Hibernate · Spring AI (Anthropic, planned) · Gradle.
