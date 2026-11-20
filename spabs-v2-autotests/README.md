# SPABS-V2 Automated Tests

UI tests (Selenium) and API tests (REST Assured) for SPABS-V2, written in Java with TestNG.

The tests run against a real backend connected to a throwaway PostgreSQL database in Docker, so they never touch your development data.

## Prerequisites

- Java 25
- Maven (or use the backend's `./mvnw`)
- Docker Desktop (running)
- Google Chrome
- Node.js (to run the frontend, for UI tests)

You don't need to download ChromeDriver. Selenium Manager downloads the matching driver automatically on the first run.

## 1. Start the test database

First time only, create the container:

```bash
docker run --name spabs-test-db -e POSTGRES_PASSWORD=test -e POSTGRES_DB=spabs_v2 -p 5433:5432 -d postgres:17
```

After that, start it with:

```bash
docker start spabs-test-db
```

Check that it's running:

```bash
docker ps
```

The test database uses port **5433**, so it doesn't clash with a local Postgres on 5432.

## 2. Start the backend against the test database

The backend must be started with these environment variables:

| Variable | Value | Why |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5433/spabs_v2` | Use the Docker test database |
| `SPRING_DATASOURCE_PASSWORD` | `test` | Password of the test database |
| `APP_SEED_SAMPLEDATA_ENABLED` | `true` | Create sample users, teams, players and fees (needed for UI tests) |
| `MAIL_HOST` | `localhost` | Make welcome emails fail instantly instead of timing out |
| `MAIL_PORT` | `1025` | (nothing listens here, which is intended) |

### Option A: IntelliJ

Use the `SpabsV2Application (test DB)` run configuration, with these environment variables:

```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/spabs_v2;SPRING_DATASOURCE_PASSWORD=test;APP_SEED_SAMPLEDATA_ENABLED=true;MAIL_HOST=localhost;MAIL_PORT=1025
```

IntelliJ settings used in this project (**Settings → Build, Execution, Deployment → Build Tools → Maven → Runner**):

- **Delegate IDE build/run actions to Maven:** on (avoids MapStruct/Lombok annotation processing errors in IntelliJ's own builder)
- **Skip Tests:** on (so running the backend doesn't first run the automated tests, which need the backend to be up)

### Option B: Terminal

macOS / Linux:

```bash
cd spabs-v2-backend
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/spabs_v2 \
SPRING_DATASOURCE_PASSWORD=test \
APP_SEED_SAMPLEDATA_ENABLED=true \
MAIL_HOST=localhost MAIL_PORT=1025 \
./mvnw spring-boot:run
```

Windows (PowerShell):

```powershell
cd spabs-v2-backend
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5433/spabs_v2"
$env:SPRING_DATASOURCE_PASSWORD="test"
$env:APP_SEED_SAMPLEDATA_ENABLED="true"
$env:MAIL_HOST="localhost"
$env:MAIL_PORT="1025"
./mvnw spring-boot:run
```

On first start against an empty database, the backend creates the tables, seeds the super admin, and (with sample data enabled) seeds sample accounts. Look for the log line starting with `Seeded sample data:`.

> **Why `SPRING_DATASOURCE_*` and not `DB_URL` / `DB_PASSWORD`?**
> `application-local.yaml` may set the datasource directly, which would override `DB_URL` / `DB_PASSWORD`. The `SPRING_DATASOURCE_*` variables take priority over all YAML files, so the backend always uses the test database.

> **Why `SAMPLEDATA` with no underscore?** Spring maps the property `app.seed.sample-data.enabled` to an environment variable by removing the dash.

### Test accounts

| Account | Password | Notes |
|---|---|---|
| `superadmin@spabs.example` | `ChangeMe123!` | Must change password on first login, so the UI redirects to `/change-password` and most API endpoints return 403. Used only by the auth API tests |
| `admin.sample@spabs.example` | `Password123` | Admin, lands on `/dashboard/admin`. Used by most API and UI tests |
| `coach1@spabs.example` | `Password123` | Coach |
| `parent1@spabs.example` | `Password123` | Parent (other parent emails are printed in the seeding log) |

## 3. Start the frontend (UI tests only)

```bash
cd spabs-v2-frontend
npm install     # first time only
npm run dev
```

The frontend runs on `http://localhost:5173`. API tests don't need it.

## 4. Run the tests

The backend (and, for UI tests, the frontend) must be running **before** you run the tests.

From this folder:

```bash
cd spabs-v2-autotests
mvn test
```

Or from the repository root, running only this module:

```bash
mvn test -pl spabs-v2-autotests
```

Running `mvn test` from the root without `-pl` also runs the backend's unit tests.

> Test classes must end in `Test` or `Tests` (e.g. `AuthTests`), otherwise `mvn test` skips them.

UI tests create players named `UI Test <timestamp>` in the test database. Reset the database whenever you want a clean start.

## Resetting the test database

To start again with an empty database:

```bash
docker rm -f spabs-test-db
docker run --name spabs-test-db -e POSTGRES_PASSWORD=test -e POSTGRES_DB=spabs_v2 -p 5433:5432 -d postgres:17
```

Then restart the backend so it recreates the tables, the super admin and the sample data.

To look inside the database:

```bash
docker exec -it spabs-test-db psql -U postgres -d spabs_v2
```

(`\dt` lists tables, `\q` exits.)

## Project structure

```
spabs-v2-autotests/
├── pom.xml
└── src/test/java/com/ahmadisyraf39/spabs_v2/autotests/
    ├── config/
    │   └── TestConfig.java        # shared settings (frontend BASE_URL)
    ├── api/
    │   ├── BaseApiTest.java       # REST Assured base URI, login helpers
    │   ├── AuthTests.java         # POST /auth/login, GET /auth/me
    │   └── PlayerTests.java       # player CRUD, validation, auth
    ├── ui/
    │   ├── BaseUiTest.java        # fresh Chrome + wait per test
    │   ├── LoginUiTests.java      # login form, redirect, error, forgot password link
    │   └── PlayersUiTests.java    # create player, form validation, details page
    ├── pages/                     # Page Object Model: locators and actions, no asserts
    │   ├── LoginPage.java
    │   ├── ForgotPasswordPage.java
    │   ├── PlayersListPage.java
    │   ├── PlayerFormPage.java
    │   └── PlayerDetailsPage.java
    └── sanity/                    # setup checks: TestNG and Selenium are working
```

UI tests locate elements by `data-testid` attributes added to the frontend.

## Troubleshooting

| Problem | Fix |
|---|---|
| `Cannot connect to the Docker daemon` | Start Docker Desktop |
| `Connection refused` on port 5433 | Run `docker start spabs-test-db` |
| `Connection refused` on port 8080 (in tests) | The backend isn't running. Start it first |
| `container name is already in use` | Use `docker start` instead of `docker run`, or `docker rm -f spabs-test-db` first |
| `password authentication failed` | Check `SPRING_DATASOURCE_PASSWORD=test` matches the container's `POSTGRES_PASSWORD` |
| Backend still shows dev data | It was started without the test DB variables. Restart it with them |
| Only the super admin exists | Sample data wasn't enabled. Check `APP_SEED_SAMPLEDATA_ENABLED=true` (no underscore in `SAMPLEDATA`) and that the database has no teams yet |
| API tests get 403 on `/players` | They're using the super admin. Use `admin.sample@spabs.example` |
| UI login lands on `/change-password` | Same cause: use the sample admin, not the super admin |
| UI tests time out on `localhost:5173` | The frontend isn't running. Run `npm run dev` |
| Date of birth fills in wrongly | Chrome expects the date in your locale's order. See `TYPED_DATE_OF_BIRTH` in `PlayersUiTests` |
| Port 8080 already in use | Another backend instance is running. Stop it first |