# CHANGES — Secuencia de Implementación

> Índice canónico de todos los changes del proyecto **Integrador Odontologia**.
> Cada change es atómico: un agente puede implementarlo en una sesión (~4-6 horas).
> **Leer este archivo antes de ejecutar cualquier `/opsx:propose`.**

---

## Cómo usar este documento

1. Identificar el change a implementar (verificar que sus dependencias están en `openspec/changes/archive/`).
2. Leer los docs de la knowledge-base indicados en "Leer antes".
3. Ejecutar `/opsx:propose <nombre-del-change>`.
4. Al terminar el change, archivarlo con `/opsx:archive <nombre-del-change>`.
5. Marcar el checkbox `[x]` en este archivo.

---

## Árbol de dependencias

```
C-01 foundation-setup
  └── C-02 core-models
        └── C-03 auth-jwt                     ← desbloquea catálogo y autenticación
              │
              ├── C-04 profesionales-service
              │     │
              │     └── C-06 turnos-agenda-backend  ← requiere C-04 y C-05
              │           │
              │           ├── C-07 agenda-web-frontend
              │           ├── C-08 historia-clinica-evoluciones  ← requiere C-05 y C-06
              │           └── C-09 notificaciones-turnos        ← requiere C-06 (al final)
              │
              └── C-05 pacientes-service      ← paralelo con C-04
```

### Paralelismo por fase

> Cada "gate" es un punto de sincronización. Los changes dentro de un grupo pueden ejecutarse en paralelo.

```
GATE 0: ninguna
  → C-01 foundation-setup (solo)

GATE 1: C-01 ✓
  → C-02 core-models (solo)

GATE 2: C-02 ✓
  → C-03 auth-jwt (solo)

GATE 3: C-03 ✓                                ← PRIMER FORK (2 paralelos)
  → C-04 profesionales-service                [Agente A]
  → C-05 pacientes-service                    [Agente B]

GATE 4: C-04 + C-05 ✓
  → C-06 turnos-agenda-backend (solo)         [Agente A]

GATE 5: C-06 ✓                                ← SEGUNDO FORK (3 paralelos)
  → C-07 agenda-web-frontend                  [Agente C]
  → C-08 historia-clinica-evoluciones         [Agente A]
  → C-09 notificaciones-turnos                [Agente B]
```

### Camino crítico (6 changes — mínimo irreducible)

```
C-01 → C-02 → C-03 → C-04 → C-06 → C-07
```

### Plan óptimo con 3 agentes

```
Paso │ Agente A (Backend Core)       │ Agente B (Backend Aux)       │ Agente C (Frontend)
─────┼───────────────────────────────┼──────────────────────────────┼─────────────────────────
  1  │ C-01 foundation-setup         │              —               │           —
  2  │ C-02 core-models              │              —               │           —
  3  │ C-03 auth-jwt                 │              —               │           —
  4  │ C-04 profesionales-service    │ C-05 pacientes-service       │           —
  5  │ C-06 turnos-agenda-backend    │              —               │           —
  6  │ C-08 historia-clinica-evoluc. │ C-09 notificaciones-turnos   │ C-07 agenda-web-frontend
```

---

## FASE 0 — Cimientos e Infraestructura Base

### [C-01] `foundation-setup`
- **Estado**: `[x]` completado (archivado 2026-11-22)
- **Scope**: Scaffolding del monorepo, configuración de compilación y entorno Docker
  - Estructura de directorios base: `backend/`, `frontend/`, `knowledge-base/`, `design-system/`
  - `backend/`: Proyecto Spring Boot 3.x con Java 17/21 y Gradle (`build.gradle`, `settings.gradle`) con dependencias: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-security`, `spring-boot-starter-validation`, driver PostgreSQL, H2 para tests y Lombok
  - `backend/src/main/resources/application.yml` configurado con variables de entorno `${DB_HOST:localhost}`, `${DB_PORT:5432}`, `${DB_NAME:odontologia_db}` y health check `/api/health`
  - `docker-compose.yml` en la raíz con servicio PostgreSQL 15+ y puerto 5432 expuesto
  - `frontend/`: Scaffolding con `tsconfig.json` estricto, `package.json` con script de compilación `"build": "tsc"`, estructura `src/` (`api/`, `models/`, `utils/`, `pages/`) y `public/` (`css/`, `index.html`) enlazando `tokens.css` de `design-system/`
  - Tests: Compilación limpia de Gradle (`./gradlew build`) y TypeScript (`npm run build`)
- **Dependencias**: ninguna
- **Governance**: BAJO
- **Leer antes**:
  - `knowledge-base/01_vision_y_objetivos.md` §Propósito del sistema
  - `knowledge-base/02_descripcion_general.md` §Stack tecnológico
  - `knowledge-base/08_arquitectura_propuesta.md` §Estructura de directorios
  - `knowledge-base/09_decisiones_y_supuestos.md` §DD-01, DD-02

---

### [C-02] `core-models`
- **Estado**: `[ ]` pendiente
- **Scope**: Entidades base, auditoría, manejo global de excepciones y seed inicial
  - Entidad JPA `Usuario`: `id` (BIGSERIAL PK), `username` (UNIQUE), `password_hash`, `email` (UNIQUE), `rol` ENUM (`ADMIN`, `RECEPCION`, `ODONTOLOGO`, `PACIENTE`), `activo` (BOOLEAN DEFAULT TRUE), `fecha_creacion`
  - MappedSuperclass `AuditableEntity` con timestamps y soporte de soft delete mediante `@Where(clause = "activo = true")` y método de baja lógica
  - `UsuarioRepository` con métodos `findByUsername`, `findByEmail`, `existsByUsername`, `existsByEmail`
  - `GlobalExceptionHandler` con `@RestControllerAdvice` respondiendo estructura estándar uniforme `ErrorResponseDTO` (timestamp, status, error, message, path) según RN-EX-02
  - Migración inicial DDL / script SQL de tablas core
  - `DataInitializer` ejecutando seed inicial: 1 usuario ADMIN (`admin` / hash BCrypt) y 1 usuario RECEPCION (`recepcion`)
  - Tests: Test de persistencia de `UsuarioRepository` sobre BD H2 en memoria y test unitario de `GlobalExceptionHandler`
- **Dependencias**: C-01
- **Governance**: CRITICO
- **Leer antes**:
  - `knowledge-base/04_modelo_de_datos.md` §Usuario
  - `knowledge-base/05_reglas_de_negocio.md` §RN-AU, RN-EX
  - `knowledge-base/08_arquitectura_propuesta.md` §Patrones aplicados (Layered Architecture, Repository)
  - `knowledge-base/09_decisiones_y_supuestos.md` §DD-03 (Soft Delete)

---

## FASE 1 — Autenticación y Catálogos de Usuarios

### [C-03] `auth-jwt`
- **Estado**: `[ ]` pendiente
- **Scope**: Autenticación JWT stateless, configuración de seguridad Spring Security y RBAC
  - Configuración de Spring Security 6 (`SecurityFilterChain`, sesión `STATELESS`, habilitación de CORS para frontend local)
  - `JwtTokenProvider`: generación y validación de tokens firmados HMAC-SHA256 con claims (`username`, `id`, `rol`) y expiración en 24 horas (RN-AU-02)
  - `JwtAuthenticationFilter`: interceptor de peticiones para resolver header `Authorization: Bearer <token>`
  - `POST /api/v1/auth/login`: autenticación con `AuthenticationManager`, validación BCrypt con costo >= 10 (RN-AU-01), rechazo de cuentas inactivas con `403 Forbidden` (RN-AU-04) y retorno de JWT + datos del rol
  - `POST /api/v1/auth/register`: registro de pacientes con asignación automática de `ROLE_PACIENTE` y validación de duplicados
  - Cliente frontend en `src/api/auth.ts` y utilidades en `src/utils/jwt.ts` para almacenamiento del token y headers de autenticación
  - Tests: Login exitoso, rechazo por contraseña inválida (401), bloqueo de usuario inactivo (403), validación de claims y expiración del token
- **Dependencias**: C-02
- **Governance**: CRITICO
- **Leer antes**:
  - `knowledge-base/03_actores_y_roles.md` §RBAC — Matriz de permisos
  - `knowledge-base/05_reglas_de_negocio.md` §Dominio: Autenticación y Cuentas (RN-AU)
  - `knowledge-base/06_funcionalidades.md` §US-001, US-002
  - `knowledge-base/07_flujos_principales.md` §Flujo 1: Autenticación y Autorización (JWT)
  - `knowledge-base/08_arquitectura_propuesta.md` §Seguridad

---

### [C-04] `profesionales-service`
- **Estado**: `[ ]` pendiente
- **Scope**: Gestión de odontólogos, especialidades, matrículas y catálogo público
  - Entidad JPA `Odontologo`: `id` (BIGSERIAL PK), `usuario_id` (FK UNIQUE a `Usuario`), `nombre`, `apellido`, `matricula` (UNIQUE), `especialidad`, `activo` (BOOLEAN)
  - `OdontologoRepository` con filtros por especialidad, matrícula y estado activo
  - DTOs con validaciones Jakarta (`@NotBlank`, `@Size`): `OdontologoRequestDTO`, `OdontologoResponseDTO`
  - Endpoints administrativos protegidos con `@PreAuthorize("hasRole('ADMIN')")`:
    - `POST /api/v1/admin/odontologos`: alta de profesional vinculando cuenta de usuario
    - `PUT /api/v1/admin/odontologos/{id}`: modificación de datos profesionales
    - `DELETE /api/v1/admin/odontologos/{id}`: soft delete (`activo = false`) preservando integridad histórica (RN-EX-01)
  - Endpoint público: `GET /api/v1/public/odontologos`: catálogo de profesionales y especialidades para reserva sin requerir login
  - Seed de profesional de prueba en `DataInitializer`
  - Tests: CRUD completo de odontólogos, rechazo de matrícula duplicada (409) y acceso público al listado
- **Dependencias**: C-03
- **Governance**: BAJO
- **Leer antes**:
  - `knowledge-base/03_actores_y_roles.md` §Actores del sistema
  - `knowledge-base/04_modelo_de_datos.md` §Odontologo
  - `knowledge-base/05_reglas_de_negocio.md` §Dominio: Excepciones Globales (RN-EX-01)
  - `knowledge-base/06_funcionalidades.md` §Épica 1 & Épica 3

---

### [C-05] `pacientes-service`
- **Estado**: `[ ]` pendiente
- **Scope**: Gestión y búsqueda de pacientes en mesa de entrada con creación de historia clínica base
  - Entidad JPA `Paciente`: `id` (BIGSERIAL PK), `usuario_id` (FK opcional a `Usuario`), `dni` (UNIQUE), `nombre`, `apellido`, `fecha_nacimiento`, `telefono`, `email`, `obra_social`, `activo` (BOOLEAN)
  - Entidad JPA `HistoriaClinica`: `id` (BIGSERIAL PK), `paciente_id` (FK UNIQUE a `Paciente`), `fecha_creacion`, `observaciones_generales`
  - `PacienteRepository` con búsqueda por DNI exacto y coincidencia parcial en nombre/apellido
  - `HistoriaClinicaRepository` con búsqueda por `paciente_id`
  - Lógica transaccional en `PacienteService`: alta de paciente con creación automática de su `HistoriaClinica` asociada (RN-PA-02)
  - Endpoints protegidos para `ADMIN` y `RECEPCION`:
    - `POST /api/v1/pacientes`: alta de paciente (permite pacientes sin usuario web registrado según SU-02)
    - `GET /api/v1/pacientes/buscar?dni={dni}&q={query}`: búsqueda ágil para recepcionistas (US-003)
    - `GET /api/v1/pacientes/{id}`: ficha del paciente y datos de contacto
    - `DELETE /api/v1/pacientes/{id}`: soft delete del paciente conservando su historia clínica (RN-PA-05)
  - Tests: Alta de paciente con creación de historia clínica automática, rechazo de DNI duplicado y búsqueda por coincidencia
- **Dependencias**: C-03
- **Governance**: BAJO
- **Leer antes**:
  - `knowledge-base/04_modelo_de_datos.md` §Paciente, HistoriaClinica
  - `knowledge-base/05_reglas_de_negocio.md` §Dominio: Pacientes e Historias Clínicas (RN-PA)
  - `knowledge-base/06_funcionalidades.md` §US-002, US-003
  - `knowledge-base/09_decisiones_y_supuestos.md` §SU-02

---

## FASE 2 — Agenda y Gestión de Turnos

### [C-06] `turnos-agenda-backend`
- **Estado**: `[ ]` pendiente
- **Scope**: Lógica transaccional de turnos, cálculo de disponibilidad y validación anti-solapamiento
  - Entidad JPA `Turno`: `id` (BIGSERIAL PK), `paciente_id` (FK), `odontologo_id` (FK), `fecha_hora_inicio` (TIMESTAMP), `fecha_hora_fin` (TIMESTAMP), `estado` ENUM (`PENDIENTE`, `CONFIRMADO`, `ATENDIDO`, `CANCELADO`), `motivo_consulta`, `activo` (BOOLEAN)
  - Índice y constraint de no solapamiento en BD: `idx_odontologo_fecha` sobre `odontologo_id`, `fecha_hora_inicio`, `fecha_hora_fin`
  - Algoritmo de cálculo de disponibilidad horaria en `TurnoService`: bloques de duración fija estándar (30/45 min según RN-TU-02), excluyendo turnos activos (`PENDIENTE`, `CONFIRMADO`, `ATENDIDO`) y bloqueando reservas en fechas pasadas (RN-TU-03)
  - Transacción `@Transactional` con validación estricta de concurrencia y no solapamiento (RN-TU-01); retorna `409 Conflict` ante colisión de reservas simultáneas
  - Máquina de estados de turnos: transiciones válidas según RN-TU-04 y liberación inmediata del horario ante cancelación (RN-TU-05)
  - Endpoints REST:
    - `GET /api/v1/odontologos/{id}/disponibilidad?fecha=YYYY-MM-DD`: cálculo de slots libres
    - `POST /api/v1/turnos`: creación y confirmación de turno con validación anti-solapamiento
    - `PUT /api/v1/turnos/{id}/cancelar`: cancelación de turno con liberación del bloque horario
    - `GET /api/v1/turnos`: listado de turnos con filtros por odontólogo, paciente, fecha y estado
  - Tests: Test de no solapamiento simultáneo, test de cálculo de disponibilidad por intervalos, transiciones de estado permitidas e inválidas
- **Dependencias**: C-04, C-05
- **Governance**: MEDIO
- **Leer antes**:
  - `knowledge-base/04_modelo_de_datos.md` §Turno
  - `knowledge-base/05_reglas_de_negocio.md` §Dominio: Turnos y Disponibilidad (RN-TU)
  - `knowledge-base/06_funcionalidades.md` §US-004, US-005, US-006
  - `knowledge-base/07_flujos_principales.md` §Flujo 2: Reserva y Confirmación de Turno
  - `knowledge-base/09_decisiones_y_supuestos.md` §SU-01

---

### [C-07] `agenda-web-frontend`
- **Estado**: `[ ]` pendiente
- **Scope**: Frontend TypeScript vanilla para autenticación, dashboard por rol y reserva interactiva de turnos
  - Vistas HTML responsivas en `frontend/public/`: `login.html`, `registro.html`, `dashboard.html` utilizando variables de `design-system/tokens.css` y branding de `design-system/brand.js`
  - Cliente API en TypeScript (`src/api/turnos.ts`, `src/api/odontologos.ts`, `src/api/pacientes.ts`) consumiendo los endpoints con fetch y token Bearer
  - Módulo de autenticación en cliente (`src/pages/login.ts`): formularios de login y registro, guardado de sesión y redirección según rol
  - Componente de agenda interactiva (`src/pages/agenda.ts`): selección de profesional y fecha, consulta dinámica de disponibilidad y renderizado de slots horarios libres
  - Modal de confirmación de turno (`src/components/modalTurno.ts`): formulario para motivo de consulta, envío de reserva y visualización de confirmación
  - Gestión de citas del usuario: listado de turnos activos y acción para cancelar/reprogramar citas
  - Tests: Validación de formatos de fecha/hora, control de estados en UI y compilación limpia con `tsc`
- **Dependencias**: C-03, C-06
- **Governance**: MEDIO
- **Leer antes**:
  - `knowledge-base/06_funcionalidades.md` §US-004, US-005, US-006
  - `knowledge-base/07_flujos_principales.md` §Flujo 1 & Flujo 2
  - `knowledge-base/08_arquitectura_propuesta.md` §Estructura de directorios (frontend)
  - `knowledge-base/09_decisiones_y_supuestos.md` §DD-01 (TypeScript Vanilla)

---

## FASE 3 — Atención Clínica e Historia Médica

### [C-08] `historia-clinica-evoluciones`
- **Estado**: `[ ]` pendiente
- **Scope**: Registro de notas de evolución odontológica, inmutabilidad clínica y transición de turnos a atendido
  - Entidad JPA `EvolucionClinica`: `id` (BIGSERIAL PK), `historia_clinica_id` (FK), `odontologo_id` (FK), `turno_id` (FK opcional), `fecha_hora` (TIMESTAMP DEFAULT CURRENT_TIMESTAMP), `diagnostico` (TEXT), `tratamiento_realizado` (TEXT), `observaciones` (TEXT)
  - Regla de inmutabilidad clínica (RN-PA-03): únicamente operaciones de inserción y lectura cronológica; bloqueo explícito de modificaciones o eliminación física
  - Control RBAC con `@PreAuthorize("hasRole('ODONTOLOGO')")`: solo el odontólogo puede registrar evoluciones clínicas (RN-PA-04)
  - Transacción clínica en `HistoriaClinicaService`: inserción de `EvolucionClinica` y actualización automática del `Turno` vinculado a estado `ATENDIDO` (RN-TU-04)
  - Endpoints REST:
    - `POST /api/v1/historias-clinicas/paciente/{id}/evoluciones`: registro de diagnóstico y tratamiento realizado
    - `GET /api/v1/historias-clinicas/paciente/{id}`: consulta del historial clínico completo con lista cronológica de evoluciones
  - Interfaz de usuario para odontólogo: pantalla de "Citas de Hoy", visor del historial del paciente y formulario de registro de evolución
  - Tests: Registro de evolución por profesional logueado, verificación de cambio de estado a ATENDIDO, intento rechazado de modificación (inmutabilidad) y control de acceso por rol
- **Dependencias**: C-04, C-05, C-06
- **Governance**: ALTO
- **Leer antes**:
  - `knowledge-base/04_modelo_de_datos.md` §HistoriaClinica, EvolucionClinica
  - `knowledge-base/05_reglas_de_negocio.md` §Dominio: Pacientes e Historias Clínicas (RN-PA-03, RN-PA-04)
  - `knowledge-base/06_funcionalidades.md` §US-007 (Registro de Evolución Odontológica)
  - `knowledge-base/07_flujos_principales.md` §Flujo 3: Atención Odontológica y Carga de Evolución Clínica

---

## FASE 4 — Notificaciones y Recordatorios

### [C-09] `notificaciones-turnos`
- **Estado**: `[ ]` pendiente
- **Scope**: Envío desacoplado de confirmaciones y recordatorios por correo electrónico y mensajería
  - Arquitectura Strategy / Adapter: interfaz `NotificationService` con implementaciones `EmailNotificationAdapter` y `WhatsAppNotificationAdapter`
  - `EmailNotificationAdapter`: integración con Spring Mail (`JavaMailSender`) utilizando configuración SMTP (`MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`) y plantilla con fecha, hora, odontólogo y consultorio (RN-NT-01)
  - `WhatsAppNotificationAdapter`: adaptador desacoplado con soporte de mock logger para desarrollo local (`WHATSAPP_MOCK_ENABLED=true`) según RN-NT-03
  - Desacoplamiento transaccional: ejecución asíncrona mediante `@Async` o evento de dominio `@EventListener` tras confirmación exitosa del turno
  - Tolerancia a fallas controlada (RN-NT-02): captura de errores de envío con logging estructurado sin hacer rollback ni cancelar la reserva del turno
  - Tests: Test con Mockito verificando que un fallo de conexión SMTP no cancela la transacción del turno, y test de generación del cuerpo de notificación con datos del turno
- **Dependencias**: C-06
- **Governance**: ALTO
- **Leer antes**:
  - `knowledge-base/05_reglas_de_negocio.md` §Dominio: Notificaciones (RN-NT)
  - `knowledge-base/06_funcionalidades.md` §US-008 (Envío de Notificaciones por Email y Mensajería)
  - `knowledge-base/07_flujos_principales.md` §Flujo 2: Reserva y Confirmación de Turno (Paso 8)
  - `knowledge-base/08_arquitectura_propuesta.md` §Strategy / Adapter Pattern & Variables de entorno
