# Pass Vault

A small Privileged Access Management backend built with Spring Boot. Owners store server/database credentials, share them with teammates, and approve time limited access to passwords. Passwords are encrypted at rest and rotated automatically on a schedule.

## Features

- JWT login with role based access (`ADMIN`, `USER`)
- Resource level permissions: `OWNER`, `FULL` share, `VIEW` share
- Account passwords encrypted with AES-256-GCM
- Access request workflow: request → approve / reject → auto expire, or revoke early
- Password rotated automatically when someone's access ends (check-out / check-in)
- Owner configured rotation schedule (minutes / hours / days)
- Password change history and full audit trail

## Tech stack

Java 17, Spring Boot 3.5, Spring Security, Hibernate 6 (Session + Criteria API with JPA metamodel), PostgreSQL (H2 for local dev), JJWT, Lombok

## Run

```bash
./mvnw spring-boot:run
```

On Windows use `mvnw.cmd spring-boot:run`. To use another port set `PORT`, e.g. `PORT=9090`.

- API base URL: http://localhost:8095/api
- H2 console: http://localhost:8095/h2-console (JDBC URL `jdbc:h2:file:./data/passvault`, user `sa`)

A default admin is created on first start: `admin` / `Admin@123`.

### Run with PostgreSQL

Create the database once:

```sql
CREATE DATABASE pam_vault;
```

Then start with the `postgres` profile. Tables are created automatically.

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

Connection defaults are `localhost:5432`, database `pam_vault`, user `postgres` / `postgres`. Override them with `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` and `DB_PASSWORD`.

## Configuration

| Property | Env var | Purpose |
|---|---|---|
| `spring.datasource.*` | `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | PostgreSQL connection (`postgres` profile) |
| `app.jwt.secret` | `JWT_SECRET` | Base64 HMAC key for tokens |
| `app.vault.key` | `VAULT_KEY` | Base64 256-bit AES key for passwords |
| `app.admin.password` | `ADMIN_PASSWORD` | Initial admin password |
| `app.access.pending-timeout-hours` | | Pending requests expire after this |
| `app.access.rotate-after-checkout` | | Rotate password when access ends |

The keys in `application.properties` are for local development only. Set your own through env vars anywhere else.

## How it works

```
Owner creates resource → adds accounts → shares with user (VIEW)
User raises request (15 - 480 min) → owner approves
User can view the password until accessEndTime
Scheduler expires the grant → password is rotated
```

Request states: `PENDING → APPROVED → EXPIRED / REVOKED / CANCELLED`, or `PENDING → REJECTED / CANCELLED / EXPIRED`.

### Scheduled jobs

| Job | Runs | Does |
|---|---|---|
| `AccessExpiryJob.expireFinishedAccess` | every minute | Expires approved grants past their end time |
| `AccessExpiryJob.expireStaleRequests` | hourly | Expires pending requests nobody acted on |
| `PasswordRotationJob` | every minute | Rotates passwords whose policy is due |

## API overview

| Area | Endpoints |
|---|---|
| Auth | `POST /api/auth/login`, `GET /api/auth/me` |
| Users (admin) | `POST /api/users`, `GET /api/users`, `PUT /api/users/{id}/disable`, `PUT /api/users/{id}/enable` |
| Resources | `POST/GET /api/resources`, `GET/PUT/DELETE /api/resources/{id}` |
| Sharing | `POST/GET /api/resources/{id}/shares`, `DELETE /api/resources/{id}/shares/{userId}` |
| Accounts | `POST/GET /api/resources/{id}/accounts`, `GET/PUT /api/accounts/{id}/password`, `DELETE /api/accounts/{id}` |
| Access requests | `POST /api/accounts/{id}/access-requests`, `GET /api/access-requests/my`, `GET /api/access-requests/pending`, `PUT /api/access-requests/{id}/approve|reject|revoke|cancel` |
| Rotation | `PUT/GET /api/accounts/{id}/rotation-policy`, `POST /api/accounts/{id}/rotate-now`, `GET /api/accounts/{id}/password-history` |
| Audit | `GET /api/audit-logs` (admin), `GET /api/resources/{id}/audit-logs` (owner) |

## Project structure

```
com.passvault
├── config        security, default admin
├── security      JWT filter, token service, user principal
├── controller    REST endpoints
├── service       business logic
├── scheduler     expiry and rotation jobs
├── dao           Hibernate DAOs (type-safe Criteria API queries)
├── entity        JPA entities
├── dto           request / response records
├── enums
└── exception     custom exceptions and global handler
```

## Note

Rotation updates the password stored in the vault only. A production-grade vault would also change it on the target system (SSH, JDBC, LDAP).
