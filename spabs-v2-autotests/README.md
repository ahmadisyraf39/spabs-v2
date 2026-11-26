# SPABS-V2 Automated Tests

![Automated tests](https://github.com/ahmadisyraf39/spabs-v2/actions/workflows/tests.yml/badge.svg)

API tests (REST Assured) and UI tests (Selenium WebDriver) for SPABS-V2, written in Java 25 with TestNG.

The tests run against the real app (backend, frontend and PostgreSQL) started with Docker Compose, on a fresh database seeded with sample data. The same setup runs in GitHub Actions on every pull request.

## What is tested

| Area | API tests | UI tests |
|---|---|---|
| Login and authentication | `AuthTests` | `LoginUiTests` |
| Role-based access (admin, coach, parent) | `RoleAccessTests` | `RoleAccessUiTests` |
| Data scoping (users only see their own teams and children) | `DataScopingTests` | |
| Players: create, update, delete, validation | `PlayerTests` | `PlayersUiTests` |
| Fees: assign, pay, double payment, team summary | `FeeTests` | `FeesUiTests` |
| Recurring training sessions | `RecurringActivityTests` | `RecurringActivityUiTests` |
| Attendance: record, duplicates, bulk, edit window, summary | `AttendanceTests` | `AttendanceUiTests` |
| Membership history: end stint, move teams, jersey numbers | `MembershipHistoryTests` | `MembershipHistoryUiTests` |

## Prerequisites

- Docker Desktop (running)
- Java 25 and Maven
- Google Chrome

You don't need to download ChromeDriver. Selenium Manager downloads the matching driver automatically.

## 1. Start the app

From the repository root:

```bash
docker compose up -d --build
```

Wait until the sample data is seeded:

```bash
docker compose logs -f backend
```

Look for the line starting with **`Seeded sample data`**, then press `Ctrl+C` (the containers keep running).

> Wait for `Seeded sample data`, not for `Started ...Application`. The sample data seeder runs *after* Spring prints "Started", so tests started earlier can fail to log in.

| Service | URL |
|---|---|
| Frontend | http://localhost:5173 |
| API | http://localhost:8080/api/v1 |
| Mailpit (email inbox) | http://localhost:8025 |
| PostgreSQL | `localhost:5433`, database `spabs_v2`, user `postgres`, password `test` |

## 2. Run the tests

```bash
cd spabs-v2-autotests
mvn test -DexcludedGroups=known-bugs
```

Useful variations:

```bash
mvn test -Dtest=FeeTests                         # one class
mvn test -Dtest='**/api/*Tests'                  # API tests only
mvn test -DHEADLESS=true -DexcludedGroups=known-bugs   # no Chrome windows
```

From the repository root, add `-pl spabs-v2-autotests`. Without it, Maven also runs the backend's own unit tests.

> Test classes must end in `Test` or `Tests` (e.g. `AuthTests`), otherwise `mvn test` skips them.

## 3. Stop and reset

```bash
docker compose down -v
```

`-v` deletes the database, so the next `docker compose up` starts with a fresh, freshly seeded one.

## Settings

`TestConfig` reads these from a `-D` property or an environment variable, with local defaults:

| Setting | Default | Purpose |
|---|---|---|
| `BASE_URL` | `http://localhost:5173` | Frontend URL for UI tests |
| `API_BASE_URL` | `http://localhost:8080/api/v1` | Backend URL for API tests |
| `HEADLESS` | `false` | Run Chrome without a window (`true` in CI) |

## Test accounts

Created by the backend's sample data seeder:

| Account | Password | Role |
|---|---|---|
| `admin.sample@spabs.example` | `Password123` | Admin, used by most API and UI tests |
| `coach1@spabs.example` | `Password123` | Coach |
| `parent1@spabs.example` | `Password123` | Parent |

Tests that need a special state (e.g. a user who must change their password) create their own users through the API, so they don't depend on shared accounts. The seeded super admin is not used by the tests.

## Known bugs

Tests that document an unfixed bug are tagged `groups = "known-bugs"` and reference a GitHub Issue. CI excludes them with `-DexcludedGroups=known-bugs`, so a red build always means something *new* broke. When a bug is fixed, remove the tag so its test runs again.

| Issue | Bug |
|---|---|
| #8 | `GET /users` and `GET /parents` return every user's contact details to any logged-in user |
| #9 | A parent can read another child's profile and attendance |
| #10 | A coach can read the roster of a team they don't coach (needs a product decision) |

To run only these tests: `mvn test -Dgroups=known-bugs`

## CI (GitHub Actions)

`.github/workflows/tests.yml` runs on every pull request, every push to `main`, and manually from the Actions tab:

1. Set up Java 25 (Maven dependencies cached)
2. `docker compose up -d --build`
3. Wait for `Seeded sample data` in the backend log
4. `mvn test -DexcludedGroups=known-bugs` with `HEADLESS=true`
5. Always, even when tests fail: upload the **`test-results`** artifact (surefire reports, failure screenshots, container logs), then stop the stack

`main` is protected: changes go through a pull request, and the `api-and-ui-tests` check must pass before merging.

**When a CI run fails:** download `test-results` from the run page. `surefire-reports/` has the errors, `screenshots/` shows the page at the moment a UI test failed, and `docker-logs.txt` has the backend logs.

## Design decisions

- **Page Object Model.** Page classes hold locators and actions; assertions stay in the tests.
- **`data-testid` locators** added to the frontend, so tests don't break when text or styling changes.
- **Explicit waits only**, never `Thread.sleep` and never implicit waits.
- **A fresh browser per UI test** (`@BeforeMethod`) at a fixed 1366x768 window, so the sidebar is always visible.
- **Test data created through the API** (`TestDataApi`) before UI tests, with unique names, so tests don't depend on each other or on existing data.
- **Date and time inputs are set as ISO values** (`2027-01-04`, `17:00`) through `InputHelper`, not typed. Typing depends on the browser locale: CI's US-English Chrome reads `04012027` as 1 April, not 4 January.
- **One timezone everywhere.** The backend uses `LocalDateTime`, so the backend container and the CI job both run on `Asia/Kuala_Lumpur`. Without it, the attendance window check saw a just-started activity as 7 hours in the future.
- **Screenshots on failure**, saved by `BaseUiTest` to `target/screenshots/`.

## Developing tests without the full Docker stack

For backend debugging or hot reload, run only the database (and Mailpit) in Docker:

```bash
docker compose up -d db mailpit
```

Then start the backend from a terminal with the test database settings.

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

macOS / Linux:

```bash
cd spabs-v2-backend
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/spabs_v2 \
SPRING_DATASOURCE_PASSWORD=test \
APP_SEED_SAMPLEDATA_ENABLED=true \
MAIL_HOST=localhost MAIL_PORT=1025 \
./mvnw spring-boot:run
```

Check the startup log for `Database JDBC URL [jdbc:postgresql://localhost:5433/...]` to confirm it's using the test database. Then start the frontend with `npm run dev` in `spabs-v2-frontend`.

Notes:

- **`SPRING_DATASOURCE_*` instead of `DB_URL` / `DB_PASSWORD`:** `application-local.yaml` may set the datasource directly, and `SPRING_DATASOURCE_*` variables take priority over every YAML file.
- **`SAMPLEDATA` has no underscore:** Spring maps `app.seed.sample-data.enabled` to an environment variable by removing the dash.
- **Mail:** Mailpit's SMTP port isn't published to your PC, so welcome emails fail fast here. That's intended; the full Docker stack sends them to Mailpit.
- **IntelliJ:** with *Delegate IDE build/run actions to Maven* on, the run configuration's environment variables are **not** passed to the backend, and it silently connects to your dev database. Use the terminal commands above.

## Project structure

```
spabs-v2-autotests/
├── pom.xml
└── src/test/java/com/ahmadisyraf39/spabs_v2/autotests/
    ├── config/
    │   └── TestConfig.java        # URLs and headless mode, from -D properties or env vars
    ├── api/                       # REST Assured tests
    │   ├── BaseApiTest.java       # base URI, JSON config, login and auth helpers
    │   ├── AuthTests.java
    │   ├── RoleAccessTests.java
    │   ├── DataScopingTests.java
    │   ├── PlayerTests.java
    │   ├── FeeTests.java
    │   ├── RecurringActivityTests.java
    │   ├── AttendanceTests.java
    │   └── MembershipHistoryTests.java
    ├── ui/                        # Selenium tests
    │   ├── BaseUiTest.java        # fresh Chrome per test, headless option, screenshots on failure
    │   ├── LoginUiTests.java
    │   ├── RoleAccessUiTests.java
    │   ├── PlayersUiTests.java
    │   ├── FeesUiTests.java
    │   ├── RecurringActivityUiTests.java
    │   ├── AttendanceUiTests.java
    │   └── MembershipHistoryUiTests.java
    ├── pages/                     # Page Object Model: locators and actions, no asserts
    │   ├── InputHelper.java       # sets date/time inputs as ISO values (locale independent)
    │   ├── Sidebar.java           # component object shared by all pages
    │   └── ...Page.java
    ├── support/
    │   └── TestDataApi.java       # creates test data through the API for UI tests
    └── sanity/
        └── SanityTest.java        # checks that TestNG runs
```

## Troubleshooting

| Problem | Fix |
|---|---|
| `Cannot connect to the Docker daemon` | Start Docker Desktop |
| `port is already allocated` (5433, 8080 or 5173) | Something else uses the port: an old container (`docker ps`), a backend in IntelliJ, or `npm run dev`. Stop it |
| All tests fail with `Connection refused` | The stack isn't up yet. Check `docker compose ps` and wait for `Seeded sample data` |
| The first logins fail, later tests pass | Tests started before seeding finished. Wait for `Seeded sample data` |
| `SessionNotCreatedException: Chrome instance exited` in CI | Chrome was started without headless mode. Create browsers only through `BaseUiTest` |
| Page refresh shows nginx `404 Not Found` | The frontend image was built without `nginx.conf`. Rebuild with `docker compose up -d --build frontend` |
| CORS error in the browser console | Use `http://localhost:5173`, not `127.0.0.1:5173` |
| A date or time is off by a day or several hours | A timezone mismatch. The backend container and the CI job must use `Asia/Kuala_Lumpur` |
| Local backend shows dev data | It was started without the test database variables (see the IntelliJ note above) |