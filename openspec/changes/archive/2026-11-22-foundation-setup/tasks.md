# Tasks

## 1. Infraestructura de Contenedores y Monorepo

- [x] 1.1 Crear archivo `docker-compose.yml` en la raíz con servicio PostgreSQL 15, puerto 5432 expuesto, credenciales por defecto y verificar validez sintáctica con `docker compose config`
- [x] 1.2 Configurar `.gitignore` en la raíz para ignorar artefactos de compilación (`backend/build/`, `.gradle/`, `frontend/dist/`, `node_modules/`) y archivos de entorno

## 2. Backend Scaffolding (Spring Boot 3 + Gradle)

- [x] 2.1 Crear estructura de directorios en `backend/` con `settings.gradle` y `build.gradle` (Spring Boot 3, Java 17/21, dependencias `web`, `data-jpa`, `security`, `validation`, `postgresql`, `h2`, `lombok`) y verificar que `./gradlew tasks` liste las tareas sin errores
- [x] 2.2 Crear `backend/src/main/resources/application.yml` configurando datasource PostgreSQL parametrizado (`${DB_HOST:localhost}`, `${DB_PORT:5432}`, `${DB_NAME:odontologia_db}`) y perfiles de ejecución
- [x] 2.3 Crear `backend/src/test/resources/application.yml` configurando H2 en memoria (`MODE=PostgreSQL`) para ejecución de tests aislados sin dependencia de servicios externos
- [x] 2.4 Crear clase principal `IntegradorOdontologiaApplication.java` con `@SpringBootApplication` y configuración mínima de seguridad que permita acceso público a `/api/health`
- [x] 2.5 Implementar `HealthController` respondiendo `GET /api/health` con payload `status: "UP"` y fecha/hora actual, validando su funcionamiento mediante `HealthControllerTest` con `./gradlew test`

## 3. Frontend Scaffolding (TypeScript Vanilla)

- [x] 3.1 Crear `frontend/package.json` y `frontend/tsconfig.json` con configuración estricta (`target: "ES2022"`, `moduleResolution: "node"`, `strict: true`, `outDir: "./dist"`), verificando que el comando `npm run build` esté configurado
- [x] 3.2 Crear estructura de carpetas `frontend/src/` (`api/`, `models/`, `utils/`, `pages/`) y archivo inicial `frontend/src/index.ts`
- [x] 3.3 Crear `frontend/public/index.html` con layout HTML5 base vinculando los estilos compartidos de `design-system/tokens.css` y el script de marca `design-system/brand.js`

## 4. Verificación de Compilación y Ciclo de Vida

- [x] 4.1 Ejecutar suite completa en backend con `./gradlew test build` y verificar salida limpia con tests pasando
- [x] 4.2 Ejecutar compilación de frontend con `npm run build` y verificar generación de archivos en `frontend/dist/` sin advertencias ni errores de tipado

## Workflow follow-up

- Iniciar implementación de tasks mediante `/opsx:apply foundation-setup`
- Archivar el change mediante `/opsx:archive foundation-setup` una vez completadas las tasks y marcar `[x]` en `CHANGES.md`
