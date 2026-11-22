# system-foundation Specification

## Purpose
Define el andamiaje ejecutable del sistema, la comprobación de salud operativa de la API, el pipeline de compilación de backend y frontend, y el entorno de persistencia local.

## Requirements

### Requirement: System health check endpoint
The system SHALL expose an unauthenticated HTTP GET endpoint at `/api/health` returning the operational status of the service and timestamp.

#### Scenario: Successful health check query
- **WHEN** a client performs an HTTP GET request to `/api/health`
- **THEN** the system returns HTTP status 200 OK with a JSON response containing `status: "UP"` and current timestamp

### Requirement: Backend build and testing lifecycle
The backend system SHALL support automated compilation, execution of test suites against an isolated in-memory database, and deployment artifact packaging using Gradle.

#### Scenario: Clean backend build execution
- **WHEN** the `./gradlew test build` command is executed in the backend directory
- **THEN** the build succeeds, all tests pass against the in-memory H2 database, and an executable JAR file is created

### Requirement: Frontend compilation and design token linkage
The frontend system SHALL compile TypeScript strictly into modular JavaScript and link the HTML shell to the centralized design system tokens.

#### Scenario: Clean frontend compilation
- **WHEN** the `npm run build` command is executed in the frontend directory
- **THEN** the TypeScript compiler completes with zero type errors and outputs the compiled artifacts

#### Scenario: Layout shell links design tokens
- **WHEN** the frontend `public/index.html` is requested or inspected
- **THEN** it includes a valid link tag pointing to `design-system/tokens.css`

### Requirement: Local database persistence environment
The repository SHALL provide a declarative Docker Compose configuration defining the PostgreSQL database service required for local development.

#### Scenario: Valid Docker Compose service definition
- **WHEN** the `docker-compose.yml` file is validated with `docker compose config`
- **THEN** the PostgreSQL service is defined with version 15 or higher, port 5432 exposed, and default database credentials
