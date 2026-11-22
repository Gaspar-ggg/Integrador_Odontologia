# Design

## Context

El proyecto se estructura como un monorepo modular dentro del directorio raíz. Ver `proposal.md` para la motivación general.
Se requiere definir la configuración técnica inicial de dos subsistemas independientes: un backend API REST (Spring Boot 3 + Gradle) y una aplicación web cliente (TypeScript vanilla).

## Goals / Non-Goals

**Goals:**
- Estructurar el árbol de directorios `backend/` y `frontend/`.
- Configurar `backend/build.gradle` con Java 17/21 y dependencias clave: Spring Web, Spring Data JPA, Spring Security, Spring Validation, PostgreSQL driver, Lombok y H2 Database para testing.
- Configurar perfiles Spring (`application.yml` y `application-test.yml`): PostgreSQL local en dev y H2 en memoria para testing automatizado.
- Exponer el controlador `HealthController` en `/api/health` para verificación de disponibilidad del backend.
- Proveer `docker-compose.yml` para levantar PostgreSQL 15+ en localhost:5432.
- Configurar `frontend/` con `tsconfig.json` estricto, script de compilación `tsc` y shell HTML básico enlazando a `design-system/tokens.css`.
- Asegurar que tanto `./gradlew test` como `npm run build` ejecuten con código de salida 0.

**Non-Goals:**
- Modelado de tablas ni entidades JPA de negocio (pertenece a C-02 y siguientes).
- Implementación de seguridad y filtros JWT (pertenece a C-03).
- Implementación de pantallas interactivas de agenda o turnos (pertenece a C-07).

## Decisions

### Decisión 1: Configuración de Gradle y Spring Boot 3
- **Elección**: Gradle con Groovy DSL (`build.gradle`) sobre Java 17 o 21 con plugin `org.springframework.boot` versión 3.x.
- **Razón**: Máxima compatibilidad, estándares de la cátedra y tiempo de inicio optimizado.
- **Alternativa descartada**: Maven (menos flexible para monorepos y tareas custom).

### Decisión 2: Estrategia de Testing y Base de Datos Aislada
- **Elección**: Base de datos H2 en memoria (`jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1`) para el entorno de test (`src/test/resources/application.yml`).
- **Razón**: Permite ejecutar la suite completa de pruebas unitarias y de integración (`./gradlew test`) de forma rápida, hermética y sin requerir que Docker esté corriendo.
- **Alternativa descartada**: Testcontainers con Docker obligatorio en cada build (añade sobrecarga de arranque innecesaria para esta fase inicial).

### Decisión 3: Estructura de Frontend con TypeScript Vanilla
- **Elección**: Compilación nativa directa vía `tsc` sin bundlers adicionales (Vite/Webpack). Estructura:
  - `frontend/src/` (`api/`, `models/`, `utils/`, `pages/`)
  - `frontend/public/` (`index.html`, `css/`)
- **Razón**: Sencillez, cero dependencias complejas de terceros y alineación con la decisión de diseño DD-01 de la Knowledge Base.

### Decisión 4: Integración del Sistema de Diseño
- **Elección**: `public/index.html` enlaza de forma relativa a `../../design-system/tokens.css` y `../../design-system/brand.js`.
- **Razón**: Reutilización directa del sistema de diseño aprobado sin duplicar archivos CSS.

## UI Mockups
Decision: no (project policy)

## Risks / Trade-offs

- **[Diferencias de dialecto entre H2 y PostgreSQL]** → *Mitigación*: Se configura `MODE=PostgreSQL` en H2 y se restringe el uso a estándar ANSI SQL / JPQL.
- **[Rutas relativas de módulos en TypeScript nativo para el navegador]** → *Mitigación*: Se configura `module: "ES2022"`, `moduleResolution: "node"` y `outDir: "./dist"` en `tsconfig.json`.
- **[Versión de Java en el entorno de ejecución local]** → *Mitigación*: Se fija `sourceCompatibility = '17'` con compatibilidad hacia Java 21 en `build.gradle` para permitir ejecución en cualquier entorno moderno.
