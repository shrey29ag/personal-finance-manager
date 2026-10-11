# Personal Finance Manager API

A REST API for tracking personal income and expenses, organising them into categories, setting savings goals
with live progress, and generating monthly and yearly reports. Authentication is session-based (a
`JSESSIONID` cookie), and every user sees only their own data.

## Tech stack

- Java 17, Spring Boot 3.5.6, Maven (via the bundled Maven wrapper)
- Spring Web, Spring Data JPA, Spring Security, Bean Validation
- H2 in-memory database
- JUnit 5, Mockito, `@WebMvcTest`, JaCoCo (80% line-coverage gate)
- Docker (multi-stage build), deployed on Render

## Local setup

Requirements: JDK 17 or newer. Maven does not need to be installed, because the wrapper downloads it.

```bash
# macOS / Linux / Git Bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

```powershell
# Windows (PowerShell needs the -D argument quoted)
mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

The API starts on `http://localhost:8080/api`. The `dev` profile is also the default when
`SPRING_PROFILES_ACTIVE` is unset. Set the `PORT` environment variable to use a different port.

## Running tests and coverage

```bash
./mvnw verify
```

This runs all unit and controller tests and then the JaCoCo check. The build fails if line coverage drops
below 80%. The HTML coverage report is written to `target/site/jacoco/index.html`.

## Running the end-to-end script

With the application running locally (dev profile):

```bash
bash financial_manager_tests.sh http://localhost:8080/api
```

On Windows, run it from Git Bash or WSL. The script needs `bash` and `curl`. Each run registers fresh users
(their emails include a timestamp), so it can be re-run against the same server.

## Deployment

The app is deployed on Render as a Docker web service, built from the `Dockerfile` in the repository root:

- **Build stage:** `eclipse-temurin:17-jdk` runs `./mvnw -B -DskipTests package`.
- **Runtime stage:** `eclipse-temurin:17-jre`.
  - Runs as a non-root user.
  - Uses `SPRING_PROFILES_ACTIVE=prod`, which sets a Secure session cookie.
  - Uses `JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=75`.
  - Listens on the `PORT` that Render injects.

Live URL: `https://personal-finance-manager-jalt.onrender.com/api`

To run the end-to-end script against the deployment:

```bash
bash financial_manager_tests.sh https://personal-finance-manager-jalt.onrender.com/api
```

The free Render tier spins down when idle, so the first request after a pause can take a while.

## Test results

The E2E script passes 86/86 against the deployed URL. `./mvnw verify` runs 122 tests at 96.8% line coverage.

## API reference

All endpoints are under `/api`. Request and response bodies are JSON. Dates use the `YYYY-MM-DD` format.
Money values have two decimal places. Every endpoint except register and login requires an authenticated
session cookie. Without one, the response is `401`.

### Error format

Every error response has the same shape:

```json
{"error":"Bad Request","message":"amount: must be greater than 0","status":400}
```

| Status | When |
|---|---|
| 400 | Validation failure, malformed JSON, bad date format, unknown enum value, wrong path or parameter type, business-rule violation |
| 401 | Not logged in, or invalid credentials |
| 403 | Deleting a default category |
| 404 | Resource does not exist or belongs to another user |
| 409 | Duplicate username or category name |

### Authentication

#### `POST /api/auth/register`

```json
{"username":"jane@example.com","password":"securePassword123","fullName":"Jane Doe","phoneNumber":"+1234567890"}
```

All fields are required:

- `username` must be a valid email address.
- `phoneNumber` must be 7–15 digits, with an optional leading `+`.

| Status | Body |
|---|---|
| 201 | `{"message":"User registered successfully","userId":1}` |
| 400 | Missing or invalid field |
| 409 | Username already registered |

#### `POST /api/auth/login`

```json
{"username":"jane@example.com","password":"securePassword123"}
```

| Status | Body |
|---|---|
| 200 | `{"message":"Login successful"}`, plus a `Set-Cookie: JSESSIONID=...` header |
| 401 | Wrong password or unknown user |

#### `POST /api/auth/logout`

| Status | Body |
|---|---|
| 200 | `{"message":"Logout successful"}` (the session is invalidated) |
| 401 | Not logged in |

### Categories

Default categories are shared by all users and cannot be deleted:

- **INCOME:** Salary
- **EXPENSE:** Food, Rent, Transportation, Entertainment, Healthcare, Utilities

Each user can also add custom categories, which only that user sees.

#### `GET /api/categories`

The response lists the default categories plus the current user's custom categories.

```json
{"categories":[
  {"name":"Salary","type":"INCOME","custom":false,"isCustom":false},
  {"name":"Food","type":"EXPENSE","custom":false,"isCustom":false},
  {"name":"Freelance","type":"INCOME","custom":true,"isCustom":true}
]}
```

| Status | Meaning |
|---|---|
| 200 | List returned |

#### `POST /api/categories`

```json
{"name":"Freelance","type":"INCOME"}
```

| Status | Body |
|---|---|
| 201 | `{"name":"Freelance","type":"INCOME","custom":true,"isCustom":true}` |
| 400 | Blank name, or `type` not `INCOME`/`EXPENSE` |
| 409 | The name matches a default category or one of the user's own categories |

#### `DELETE /api/categories/{name}`

| Status | Body |
|---|---|
| 200 | `{"message":"Category deleted successfully"}` |
| 400 | The category is used by one or more transactions |
| 403 | It is a default category |
| 404 | The user has no custom category with that name |

### Transactions

#### `POST /api/transactions`

```json
{"amount":5000.00,"date":"2024-01-15","category":"Salary","description":"January Salary"}
```

- `amount` must be greater than 0.
- `date` is required and cannot be in the future.
- `category` must be a default category or one of the user's own.
- `description` is optional.
- The transaction's `type` comes from its category.

| Status | Body |
|---|---|
| 201 | `{"id":1,"amount":5000.00,"date":"2024-01-15","category":"Salary","description":"January Salary","type":"INCOME"}` |
| 400 | Zero/negative/missing amount, future date or bad format (e.g. `01-17-2024`), unknown category |

#### `GET /api/transactions`

Optional query parameters, which can be combined:

- `startDate` and `endDate` (inclusive range)
- `category` (category name)
- `categoryId`
- `type` (`INCOME` or `EXPENSE`)

Results are sorted newest first.

```
GET /api/transactions?startDate=2024-01-01&endDate=2024-01-31&type=EXPENSE
```

```json
{"transactions":[
  {"id":3,"amount":400.00,"date":"2024-01-17","category":"Food","description":"Groceries","type":"EXPENSE"},
  {"id":2,"amount":1200.00,"date":"2024-01-16","category":"Rent","description":"Monthly rent payment","type":"EXPENSE"}
]}
```

| Status | Meaning |
|---|---|
| 200 | List returned (`{"transactions":[]}` when empty) |
| 400 | Bad date or type parameter, or `startDate` after `endDate` |

#### `PUT /api/transactions/{id}`

All fields are optional. The date cannot be changed: a `date` field in the body is ignored.

```json
{"amount":5500.00,"category":"Salary","description":"Updated January Salary with bonus"}
```

| Status | Body |
|---|---|
| 200 | The updated transaction (same shape as create) |
| 400 | Zero/negative amount, unknown category, non-numeric id |
| 404 | Transaction not found or owned by another user |

#### `DELETE /api/transactions/{id}`

| Status | Body |
|---|---|
| 200 | `{"message":"Transaction deleted successfully"}` |
| 404 | Transaction not found or owned by another user |

### Savings goals

How progress is calculated:

- `currentProgress` = total income − total expenses, counting the user's transactions dated on or after the
  goal's `startDate`.
- `progressPercentage` = `currentProgress / targetAmount × 100`, rounded to 2 decimal places.
- `remainingAmount` = `targetAmount − currentProgress`, never below 0.

#### `POST /api/goals`

```json
{"goalName":"Emergency Fund","targetAmount":10000.00,"targetDate":"2027-01-01","startDate":"2024-01-01"}
```

- `targetAmount` must be greater than 0.
- `targetDate` must be in the future.
- `startDate` is optional. It defaults to today and must be before `targetDate`.

| Status | Body |
|---|---|
| 201 | `{"id":2,"goalName":"Emergency Fund","targetAmount":10000.00,"targetDate":"2027-01-01","startDate":"2024-01-01","currentProgress":6550.00,"progressPercentage":65.5,"remainingAmount":3450.00}` |
| 400 | Zero/negative amount, past target date, start date not before target date |

#### `GET /api/goals`

| Status | Body |
|---|---|
| 200 | `{"goals":[ ...goal objects... ]}` (`{"goals":[]}` when empty) |

#### `GET /api/goals/{id}`

| Status | Body |
|---|---|
| 200 | One goal object |
| 404 | Goal not found or owned by another user |

#### `PUT /api/goals/{id}`

Both fields are optional and validated as on create.

```json
{"targetAmount":15000.00,"targetDate":"2028-01-01"}
```

| Status | Body |
|---|---|
| 200 | The updated goal with recalculated progress, e.g. `..."currentProgress":9050.00,"progressPercentage":60.33,"remainingAmount":5950.00}` |
| 400 | Invalid amount or date |
| 404 | Goal not found or owned by another user |

#### `DELETE /api/goals/{id}`

| Status | Body |
|---|---|
| 200 | `{"message":"Goal deleted successfully"}` |
| 404 | Goal not found or owned by another user |

### Reports

#### `GET /api/reports/monthly/{year}/{month}`

```json
{"month":1,"year":2024,
 "totalIncome":{"Salary":5500.00,"Freelance":1500.00},
 "totalExpenses":{"Food":450.00},
 "netSavings":6550.00}
```

A month with no transactions returns empty maps and `"netSavings":0`.

| Status | Meaning |
|---|---|
| 200 | Report returned |
| 400 | Month outside 1–12, year outside 1–9999, or non-numeric value |

#### `GET /api/reports/yearly/{year}`

```json
{"year":2024,"totalIncome":{"Salary":5500.00,"Freelance":1500.00},"totalExpenses":{"Food":450.00},"netSavings":6550.00}
```

| Status | Meaning |
|---|---|
| 200 | Report returned |
| 400 | Year outside 1–9999, or non-numeric value |

## Design decisions

- **Spring Boot 3.5.6.** The assignment requires Spring Boot 3.x. Spring Initializr now defaults to 4.x, so
  the `pom.xml` pins the 3.5.6 parent explicitly.
- **`custom` and `isCustom`.** Category responses include both keys with the same value. The test script
  checks `custom`, and the assignment PDF shows `isCustom`.
- **Money and percentages.** Money is stored and returned as `BigDecimal` with scale 2 (`DECIMAL(19,2)`),
  normalised with `HALF_UP`, so amounts serialise as `5000.00`. `progressPercentage` is a `double` rounded
  to 2 decimal places, so it serialises as `65.5` or `60.33` rather than `65.50`.
- **Zero totals.** Zero totals (`currentProgress`, `netSavings`) are returned as `BigDecimal.ZERO`, which
  serialises as `0` rather than `0.00`. The test script compares these values as raw strings.
- **Date on transaction update.** A `date` sent in `PUT /api/transactions/{id}` is silently ignored, and the
  request still returns 200. The script expects a 2xx response with the original date unchanged.
- **Other users' resources.** Another user's transaction or goal returns 404, not 403, so the API does not
  reveal that the resource exists. Every user-owned lookup filters by the logged-in user's id, so this
  applies everywhere.
- **Live goal progress.** Goal progress is computed from transactions on every read and never stored. It
  therefore stays correct after transactions are added, edited, or deleted.
- **JPA Specifications for filters.** Transaction filters are built with JPA Specifications. JPQL patterns
  like `:param is null or ...` with nullable typed parameters are unreliable in Hibernate 6.
- **CSRF and 401 handling.** CSRF protection is disabled because this is a JSON API used by non-browser
  clients. Unauthenticated requests get a JSON 401 from a custom `AuthenticationEntryPoint` instead of a
  redirect to a login page.
- **Session cookie.** The session cookie is `Secure` in the `prod` profile and not `Secure` in `dev`. That
  lets curl send it over plain http locally.
- **H2 in-memory database.** All data is lost when the application restarts, including when Render spins
  the service down after inactivity.
- **Testing approach.** Services are unit-tested with Mockito, and controllers with `@WebMvcTest` using the
  real security configuration. The real database queries (filters and progress sums) are covered by the
  end-to-end script.
- **Future-date test expiry.** The script's future-date test posts a transaction dated `2026-12-31`. It only
  passes while that date is still in the future (until 31 Dec 2026).
