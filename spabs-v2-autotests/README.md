# SPABS-V2 Automated Tests

UI tests (Selenium) and API tests (REST Assured) for SPABS-V2, written in Java with TestNG.

The tests run against a real backend connected to a throwaway PostgreSQL database in Docker, so they never touch your development data.

## Prerequisites

- Java 25
- Maven (or use the backend's `./mvnw`)
- Docker Desktop (running)
- Google Chrome

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

The backend must be pointed at the test database using these environment variables:

| Variable | Value |
|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5433/spabs_v2` |
| `SPRING_DATASOURCE_PASSWORD` | `test` |

### Option A: IntelliJ

Use the `SpabsV2Application (test DB)` run configuration, with these environment variables:

```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/spabs_v2;SPRING_DATASOURCE_PASSWORD=test
```

### Option B: Terminal

macOS / Linux:

```bash
cd spabs-v2-backend
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/spabs_v2 SPRING_DATASOURCE_PASSWORD=test ./mvnw spring-boot:run
```

Windows (PowerShell):

```powershell
cd spabs-v2-backend
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5433/spabs_v2"; $env:SPRING_DATASOURCE_PASSWORD="test"; ./mvnw spring-boot:run
```

On first start against an empty database, the backend creates the tables and seeds the super admin account (`superadmin@spabs.example` / `ChangeMe123!`).

> **Why `SPRING_DATASOURCE_*` and not `DB_URL` / `DB_PASSWORD`?**
> `application-local.yaml` may set the datasource directly, which would override `DB_URL` / `DB_PASSWORD`. The `SPRING_DATASOURCE_*` variables take priority over all YAML files, so the backend always uses the test database.

## 3. Run the tests

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

## Resetting the test database

To start again with an empty database:

```bash
docker rm -f spabs-test-db
docker run --name spabs-test-db -e POSTGRES_PASSWORD=test -e POSTGRES_DB=spabs_v2 -p 5433:5432 -d postgres:17
```

Then restart the backend so it recreates the tables and the super admin.

To look inside the database:

```bash
docker exec -it spabs-test-db psql -U postgres -d spabs_v2
```

(`\dt` lists tables, `\q` exits.)

## Project structure

```
spabs-v2-autotests/
├── pom.xml
└── src/test/java/
    └── sanity/        # setup checks: TestNG and Selenium are working
```

API and UI test packages are added as each round of the test plan is completed.

## Troubleshooting

| Problem | Fix |
|---|---|
| `Cannot connect to the Docker daemon` | Start Docker Desktop |
| `Connection refused` on port 5433 | Run `docker start spabs-test-db` |
| `container name is already in use` | Use `docker start` instead of `docker run`, or `docker rm -f spabs-test-db` first |
| `password authentication failed` | Check `SPRING_DATASOURCE_PASSWORD=test` matches the container's `POSTGRES_PASSWORD` |
| Backend still shows dev data | It was started without the test DB variables. Restart it with them |
| Port 8080 already in use | Another backend instance is running. Stop it first |