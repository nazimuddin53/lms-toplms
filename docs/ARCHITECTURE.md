# toplms — Architecture & Developer Guide

A multi-tenant Learning Management System (LMS) built as a SaaS product on
Spring Boot. This document describes how the application is structured and how
its main flows (signup, login, and admin management of users, courses, teachers
and students) actually work, as built.

> Status: early development. Authentication, tenant provisioning, roles, a tenant
> admin dashboard, and management of courses/teachers/students are implemented.
> Enrolments and the learner-facing experience are not built yet.

---

## 1. Tech stack

| Concern | Choice |
| --- | --- |
| Language | Java 17 (Gradle toolchain) |
| Framework | Spring Boot 4.0.6 (Spring Web MVC, Spring Data JPA, Spring Security) |
| Views | Thymeleaf (server-side HTML) |
| Database | PostgreSQL (`toplms_db` on `localhost:5432`) |
| ORM | Hibernate (via `spring-boot-starter-data-jpa`) |
| AI (planned) | Spring AI 2.0.0-M5, Anthropic starter |
| Build | Gradle (Groovy DSL), wrapper checked in |

---

## 2. Module structure

A two-module Gradle build (plus a pure-aggregator root):

```
toplms/
├── build.gradle          root: aggregator; shared config in subprojects { }
├── gradle.properties     versions (Spring Boot, Java) in one place
├── engine/               core library  (NOT bootable)
└── app/                  Spring Boot entry point (the ONLY bootable module)
```

### engine/ — business logic, no web

A `java-library` jar. Holds the domain entities, repositories, and services. It
has **no** `main()` method and **no** web/security starters — anything that has
no opinion about HTTP belongs here.

```
engine/src/main/java/com/toplms/
├── master/            CONTROL PLANE — tenants, users, roles
│   ├── domain/        Tenant, TenantUser, TenantRole, TenantStatus   (@Entity)
│   ├── repository/    TenantRepository, TenantUserRepository, TenantRoleRepository
│   └── service/       TenantProvisioningService, TenantUserService,
│                      NewTenantCommand, NewUserCommand, *Exception
├── tenant/            TENANT PLANE — the LMS domain (per-tenant content)
│   ├── domain/        Course   (@Entity)
│   ├── repository/    CourseRepository
│   └── service/       CourseService, NewCourseCommand
└── config/tenant/     TenantContext (ThreadLocal holder, for future tenant routing)
```

### app/ — web layer + Spring Boot wiring

Applies the `org.springframework.boot` plugin (produces the executable jar) and
depends on `:engine`. Holds controllers, templates, static assets, and security.

```
app/src/main/java/com/toplms/
├── ToplmsApplication.java            @SpringBootApplication entry point
├── config/SecurityConfig.java        the Spring Security filter chain + BCrypt bean
├── security/                         TenantUserDetailsService, TenantUserPrincipal,
│                                     RoleBasedAuthenticationSuccessHandler
└── web/
    ├── publicpage/   HomeController, SignupController, SignupForm
    ├── auth/         LoginController (GET /login only)
    └── tenant/       AdminDashboardController, AdminUserController, AdminRoleController,
                      AdminCourseController, AdminMemberController (teachers+students),
                      TenantProfileController, CreateUserForm, CourseForm, MemberForm

app/src/main/resources/
├── application.yaml
├── static/css/       landing.css, admin.css
└── templates/
    ├── public/       index.html (landing), signup.html
    ├── auth/         login.html
    └── tenant/       profile.html, admin-dashboard.html, admin-users.html,
                      admin-new-user.html, admin-roles.html, admin-courses.html,
                      admin-course-new.html, admin-members.html, admin-member-new.html,
                      fragments.html (shared sidebar)
```

**Why the split:** `engine` can't `import` anything from `app` (Gradle won't allow
it), so business logic can never accidentally depend on the web layer. Tests of
`engine` start fast (no embedded Tomcat). Future delivery channels (CLI, batch,
API) reuse `engine` unchanged.

---

## 3. Multi-tenancy model

**Single shared database.** All tenants live in one PostgreSQL database; rows are
scoped to a tenant by a foreign key. Control-plane tables (`tenant`) describe the
businesses themselves; tenant-scoped tables (`tenant_user`, `tenant_role`) carry
a `tenant_id`.

> `Course` is the first tenant-plane entity (package `com.toplms.tenant`). For now
> tenant scoping is done with an explicit `@ManyToOne Tenant` association plus
> `findByTenant_Id(...)` queries. Target (not yet built): switch tenant-scoped
> entities to Hibernate's `@TenantId` so every query is auto-filtered by tenant.

The schema is currently created by Hibernate (`spring.jpa.hibernate.ddl-auto:
update`). Production should switch to Flyway migrations (`ddl-auto: validate`).

---

## 4. Data model

### `tenant` — a customer business (control plane)

| Column | Notes |
| --- | --- |
| `id` | PK, identity |
| `business_name` | not null |
| `slug` | unique — the workspace key, e.g. `acme` |
| `status` | enum `TenantStatus` (PROVISIONING / ACTIVE / SUSPENDED), stored as text |
| `created_at` | set once on insert |

### `tenant_role` — a role within a tenant

| Column | Notes |
| --- | --- |
| `id` | PK |
| `tenant_id` | FK → `tenant` (`@ManyToOne`) |
| `role_name` | e.g. ADMIN, INSTRUCTOR, LEARNER |
| `description` | human-readable |
| `menu_json` | reserved for per-role menu/permissions config |
| `status` | currently present but unused for roles |

Seeded automatically at signup: every new tenant gets ADMIN, INSTRUCTOR, LEARNER.

### `tenant_user` — a login account

| Column | Notes |
| --- | --- |
| `id` | PK |
| `tenant_id` | FK → `tenant` (`@ManyToOne`) |
| `role_id` | FK → `tenant_role` (`@ManyToOne`, nullable) |
| `full_name` | not null |
| `email` | unique — the login identifier |
| `password_hash` | BCrypt hash, never plaintext |
| `created_at` | set once on insert |

> `TenantUser` lives in `master/` (not a tenant-scoped package) because login
> happens by email *before* we know the tenant — the lookup can't be tenant-scoped.

### `course` — a course (tenant plane)

| Column | Notes |
| --- | --- |
| `id` | PK |
| `tenant_id` | FK → `tenant` (`@ManyToOne`) — which workspace owns it |
| `title` | not null |
| `description` | free text |
| `created_at` | set once on insert |

> "Teachers" and "students" are **not** separate tables — they are `tenant_user`
> rows filtered by role (`INSTRUCTOR` / `LEARNER`).

---

## 5. Authentication & authorization

Powered by Spring Security. Key beans (all in `app/`):

| Bean | Responsibility |
| --- | --- |
| `SecurityConfig` | Declares the `SecurityFilterChain`: which URLs are public, which need a role; wires form login + logout; defines the `PasswordEncoder` (BCrypt). |
| `TenantUserDetailsService` | `loadUserByUsername(email)` → loads the user + tenant + role, builds a `TenantUserPrincipal`. |
| `TenantUserPrincipal` | The logged-in user. Authorities = `ROLE_<roleName>` (so role `ADMIN` → authority `ROLE_ADMIN`). Also carries tenant id/slug/name for the views. |
| `RoleBasedAuthenticationSuccessHandler` | After login, redirects ADMIN → `/app/admin`, everyone else → `/app/profile`. |

### Access rules (from `SecurityConfig`)

```
permitAll : /, /signup, /signup/**, /login, /error, /css/**, /js/**, /images/**
ROLE_ADMIN: /app/admin, /app/admin/**
authenticated: everything else
```

CSRF protection is **on**. Every form POST (signup, login, logout, create-user)
includes a hidden `_csrf` token; a POST without it is rejected with 403.

---

## 6. Request flows

### Signup (create a tenant)

```
GET  /signup                      SignupController → public/signup.html
POST /signup  (SignupForm, @Valid)
   → format validation (@NotBlank/@Email/@Pattern) via BindingResult
   → SignupController builds NewTenantCommand
   → TenantProvisioningService.provision(cmd)   [@Transactional]
        - check slug + email are unique
        - save Tenant (status ACTIVE)
        - seed roles: ADMIN, INSTRUCTOR, LEARNER
        - save founding TenantUser with the ADMIN role (password BCrypt-hashed)
   → redirect /login?registered
```

### Login (the POST is the framework's, not a controller)

```
GET  /login                       LoginController → auth/login.html
POST /login  (username, password, _csrf)
   → handled by Spring Security's UsernamePasswordAuthenticationFilter
   → TenantUserDetailsService.loadUserByUsername(email)
   → PasswordEncoder.matches(typed, stored hash)
   → success: store Authentication in session
              RoleBasedAuthenticationSuccessHandler:
                  ROLE_ADMIN → /app/admin   (admin dashboard)
                  else       → /app/profile
   → failure: redirect /login?error
```

There is **no** `@PostMapping("/login")` in the codebase — and there shouldn't
be. `formLogin()` installs the filter that handles it.

### Admin: create a user

```
GET  /app/admin/users/new         AdminUserController → tenant/admin-new-user.html
POST /app/admin/users (CreateUserForm, @Valid)
   → TenantUserService.createUser(NewUserCommand)   [@Transactional]
        - tenantId comes from the logged-in admin's principal (never the form)
        - email uniqueness check
        - the chosen role must belong to this admin's tenant (anti-tampering)
        - save TenantUser (password BCrypt-hashed)
   → redirect /app/admin/users  (success banner via flash attribute)
```

---

## 7. URL map

| Method | Path | Handler | Access |
| --- | --- | --- | --- |
| GET | `/` | HomeController | public |
| GET | `/signup` | SignupController | public |
| POST | `/signup` | SignupController | public |
| GET | `/login` | LoginController | public |
| POST | `/login` | Spring Security | public |
| POST | `/logout` | Spring Security | authenticated |
| GET | `/app/profile` | TenantProfileController | authenticated |
| GET | `/app/admin` | AdminDashboardController | ROLE_ADMIN |
| GET | `/app/admin/users` | AdminUserController | ROLE_ADMIN |
| GET | `/app/admin/users/new` | AdminUserController | ROLE_ADMIN |
| POST | `/app/admin/users` | AdminUserController | ROLE_ADMIN |
| GET | `/app/admin/roles` | AdminRoleController | ROLE_ADMIN |
| GET | `/app/admin/courses` | AdminCourseController | ROLE_ADMIN |
| GET | `/app/admin/courses/new` | AdminCourseController | ROLE_ADMIN |
| POST | `/app/admin/courses` | AdminCourseController | ROLE_ADMIN |
| GET | `/app/admin/teachers` | AdminMemberController | ROLE_ADMIN |
| GET | `/app/admin/teachers/new` | AdminMemberController | ROLE_ADMIN |
| POST | `/app/admin/teachers` | AdminMemberController | ROLE_ADMIN |
| GET | `/app/admin/students` | AdminMemberController | ROLE_ADMIN |
| GET | `/app/admin/students/new` | AdminMemberController | ROLE_ADMIN |
| POST | `/app/admin/students` | AdminMemberController | ROLE_ADMIN |

---

## 8. Running locally

Prerequisites: a PostgreSQL database `toplms_db` on `localhost:5432` (credentials
in `app/src/main/resources/application.yaml`).

```bash
./gradlew bootRun        # run the app  (http://localhost:8080)
./gradlew build          # compile + test + jar
./gradlew classes        # compile only (fast feedback)
./gradlew test           # run tests
```

Then: open `/` → "Get started" → sign up → you are redirected to `/login` →
sign in → as the founding ADMIN you land on `/app/admin`.

> If a previous `bootRun` is still running, stop it first (Ctrl+C, or the JVM
> will hold port 8080) — otherwise you'll be testing old code.

---

## 9. Spring concepts used (quick glossary)

| Concept | Where | One-liner |
| --- | --- | --- |
| `@Entity` / `@ManyToOne` | engine domain | JPA maps classes ↔ tables; `@ManyToOne` = a FK association |
| Spring Data repository | engine repository | extend `JpaRepository`; methods derived from names (`findByEmail`) |
| `@Service` + `@Transactional` | engine service | business logic; method = one atomic DB transaction |
| Command object (record) | engine service | domain-shaped input, decoupled from web forms |
| `@Controller` + view name | app web | returns a Thymeleaf template name to render |
| Form DTO + `@Valid` | app web | `@NotBlank`/`@Email` validated into a `BindingResult` |
| POST/Redirect/GET + flash | app web | redirect after POST; one-shot data survives the redirect |
| Spring Security `formLogin` | SecurityConfig | a filter handles `POST /login`, not a controller |
| `UserDetailsService` | security | bridges the DB to Security's auth |
| `PasswordEncoder` (BCrypt) | security | hash on signup, verify on login |
| `AuthenticationSuccessHandler` | security | role-based redirect after login |
| Thymeleaf fragments | tenant templates | shared sidebar written once, reused per page |
| Static resources | `static/css` | files under `static/` served at the URL root |
