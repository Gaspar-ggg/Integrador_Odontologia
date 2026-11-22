# Proposal

## Why

El proyecto Integrador Odontología necesita establecer su estructura de código ejecutable, entorno de base de datos local y cadenas de compilación iniciales para permitir el desarrollo incremental del backend (Spring Boot 3) y del frontend (TypeScript vanilla). Sin este andamiaje base, no es posible implementar ni probar las entidades de dominio, autenticación ni lógica de turnos definida en la Knowledge Base.

## What Changes

- Creación de la estructura monorepo conteniendo los módulos `backend/` y `frontend/`.
- Configuración de `backend/` con Gradle y Spring Boot 3.x (Java 17/21), incluyendo dependencias de Spring Web, Spring Data JPA, Spring Security, Spring Validation, Lombok, PostgreSQL driver y H2 para testing automatizado en memoria.
- Configuración de `application.yml` en backend con profiles y endpoint de verificación operativa `GET /api/health`.
- Configuración de `docker-compose.yml` en la raíz para orquestar la instancia de PostgreSQL 15+ para desarrollo local.
- Configuración de `frontend/` con TypeScript 5.x (`tsconfig.json` estricto, `package.json`), scripts de build con `tsc` y estructura de carpetas (`src/`, `public/`) vinculada a los tokens de diseño de `design-system/tokens.css`.
- Verificación automatizada de compilación sin errores para ambos entornos (`./gradlew test build` y `npm run build`).

## Capabilities

### New Capabilities

- `system-foundation`: Inicialización del entorno de ejecución, health check básico del sistema, infraestructura local de base de datos y pipeline de compilación para backend y frontend.

### Modified Capabilities

*(Ninguna, es la primera capability del sistema).*

## Impact

- **Código y Directorios**: Se crean los directorios `backend/` y `frontend/` en la raíz del proyecto, junto con `docker-compose.yml`.
- **APIs**: Se expone el endpoint inicial no autenticado `GET /api/health` para comprobación de estado.
- **Dependencias**: Se integran Gradle Wrapper y dependencias oficiales de Spring Boot 3, junto con `typescript` en frontend.
- **Sistemas**: Servicio PostgreSQL local en puerto 5432 mediante Docker.
