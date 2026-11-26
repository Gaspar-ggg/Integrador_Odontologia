# 🦷 Clínica Odontológica "La Muelita Cuyana"
### Sistema Integral de Gestión de Turnos e Historias Clínicas
> Proyecto Integrador — **Desarrollo de Software (3° Año)**

---

## 📌 Descripción del Proyecto

Sistema web integral diseñado para digitalizar y optimizar la operatoria de una clínica odontológica moderna:
* **Pacientes**: Solicitud y gestión de turnos interactivos en línea con validación anti-solapamiento y recordatorios preventivos.
* **Mesa de Entrada / Recepción**: Gestión ágil de agenda, altas y búsquedas de pacientes, asignación y cancelación de citas.
* **Odontólogos**: Visualización de agenda diaria, consulta del historial médico del paciente y registro inmutable de notas de evolución clínica.
* **Administración**: Gestión de usuarios, roles, profesionales y disponibilidad de consultorios.

---

## 👥 Equipo de Desarrollo y Roles

| Miembro | Rol en el Proyecto | Responsabilidad Técnica |
| :--- | :--- | :--- |
| **Sebastian** | **Líder de Proyecto / Tech Lead** | Arquitectura general, setup de monorepo, modelos core, code reviews y dueño del repositorio principal. |
| **Tomas** | **Backend Developer** | Autenticación y autorización basada en JWT stateless, Spring Security 6 y catálogo de profesionales. |
| **Diego** | **Backend Developer** | Gestión de fichas de pacientes y módulo clínico de evoluciones odontológicas con inmutabilidad legal. |
| **Gaspar** | **Backend Developer** | Motor transaccional de turnos, cálculo de disponibilidad anti-colisión concurrente y servicio de notificaciones. |
| **Facundo** | **Frontend Specialist** | Arquitectura cliente en TypeScript vanilla, maquetación responsiva y enlace a tokens del sistema de diseño. |

---

## 🛠️ Stack Tecnológico

| Capa | Tecnología | Versión | Justificación |
| :--- | :--- | :--- | :--- |
| **Backend** | Java / Spring Boot / Gradle | Java 17+, Spring Boot 3.x, Gradle 9.x | Arquitectura empresarial en capas, tipado seguro y alto rendimiento. |
| **Persistencia** | Spring Data JPA / Hibernate | JPA 3.x | Mapeo objeto-relacional estándar, auditoría y soporte de soft delete. |
| **Base de Datos** | PostgreSQL / H2 | PostgreSQL 15+ / H2 in-memory | PostgreSQL en Docker para desarrollo/producción; H2 para tests herméticos. |
| **Seguridad** | Spring Security + JWT | Spring Security 6.x | Seguridad stateless con tokens HMAC-SHA256 y control granular de roles (RBAC). |
| **Frontend** | TypeScript Vanilla, HTML5, CSS3 | TypeScript 5.x | Sin sobrecarga de frameworks; tipado estricto directo (`tsc`) y código nativo portable. |
| **Diseño / UI** | Design System "La Muelita Cuyana" | Tokens CSS centralizados | Identidad visual corporativa, paleta consistente y layout responsivo. |

---

## 📂 Estructura del Repositorio (Monorepo)

```text
Integrador_Odontologia/
├── backend/                   # API REST en Spring Boot 3 y Gradle
│   ├── src/main/java/         # Código de negocio (controller, model, repository, dto, exception)
│   ├── src/main/resources/    # Configuración de perfiles (application.yml para PostgreSQL)
│   ├── src/test/              # Suite de 116 pruebas automatizadas (H2 in-memory)
│   ├── build.gradle           # Dependencias y tareas de compilación
│   └── Dockerfile             # Imagen multi-stage JRE 17
├── frontend/                  # Cliente Web modular en TypeScript
│   ├── src/                   # Código TypeScript modular (api, models, utils, pages)
│   ├── public/                # Shell HTML y estilos (login, registro, dashboard)
│   ├── dist/                  # JavaScript compilado generado por tsc
│   ├── tsconfig.json          # Configuración estricta de compilación TS
│   ├── nginx.conf             # Servidor Nginx con proxy inverso hacia /api/v1
│   └── Dockerfile             # Imagen multi-stage Node/Nginx
├── design-system/             # Identidad visual de "La Muelita Cuyana"
│   ├── tokens.css             # Variables CSS globales (colores, tipografía, espacios)
│   ├── brand.js               # Enlace dinámico de marca
│   └── assets/                # Logotipos y recursos gráficos
├── docs/
│   └── discovery/             # Informe de Discovery y Benchmark de mercado
│       ├── informe-discovery.md
│       └── informe-discovery.pdf
├── knowledge-base/            # Base de Conocimiento canónica (10 documentos de dominio)
├── openspec/                  # Especificaciones formales y trazabilidad del proyecto
│   ├── specs/                 # Specs vigentes de capabilities
│   └── changes/archive/       # Registro histórico de changes implementados
├── docker-compose.yml         # Orquestación de Postgres, Backend y Frontend
├── CHANGES.md                 # Roadmap y secuencia de 9 changes completados (100%)
├── AGENTS.md / CLAUDE.md      # Reglas duras y estándares de codificación para IA
└── README.md                  # Este documento
```

---

## 🧪 Ejecución de Pruebas (Suite de Tests)

El proyecto cuenta con **116 pruebas automatizadas** desarrolladas bajo metodología Strict TDD (repositorio, servicio transaccional, seguridad RBAC, concurrencia multi-hilo y validación de endpoints).

Para clonar y correr todos los tests del backend con base de datos H2 en memoria (sin necesidad de configurar nada previo):

```bash
git clone https://github.com/Seba0015/Integrador_Odontologia.git
cd Integrador_Odontologia/backend
./gradlew test
```

Para validar la compilación limpia y tipado estricto del frontend:
```bash
cd ../frontend
npm install
npm run build
```

---

## 🐳 Opción Rápida con Docker (Todo el Sistema en 1 Comando)

Si tienes Docker instalado, puedes levantar la Base de Datos PostgreSQL, la API REST en Spring Boot y el Frontend Web servido por Nginx ejecutando desde la raíz:

```bash
docker compose up --build
```
* **Frontend Web:** [http://localhost:3000](http://localhost:3000)
* **Backend API:** [http://localhost:8080/api/v1](http://localhost:8080/api/v1)
* **PostgreSQL:** `localhost:5432`

---

## 🚀 Puesta en Marcha Local Manual (Quickstart)

### Requisitos previos:
* **JDK 17 o 21** instalado (`java -version`).
* **Node.js 18+ y npm** instalados (`node -v && npm -v`).
* **Docker y Docker Compose** instalados (opcional para BD local).

### 1. Clonar el repositorio
```bash
git clone https://github.com/Seba0015/Integrador_Odontologia.git
cd Integrador_Odontologia
```

### 2. Iniciar la Base de Datos (PostgreSQL en Docker)
```bash
docker compose up -d
```
*(Crea una instancia de PostgreSQL en `localhost:5432` con usuario `postgres` y BD `odontologia_db`).*

### 3. Compilar y Ejecutar el Backend
```bash
cd backend

# Ejecutar las pruebas automatizadas (usan H2 en memoria automáticamente)
./gradlew test

# Levantar la aplicación en modo desarrollo
./gradlew bootRun
```
* El backend iniciará en: `http://localhost:8080`.
* **Health Check**: Puedes comprobar el estado del servicio en:  
  `curl http://localhost:8080/api/health` $\rightarrow$ `{"status":"UP","timestamp":"..."}`.

### 4. Compilar el Frontend
```bash
cd ../frontend

# Instalar dependencias de desarrollo (TypeScript)
npm install

# Compilar TypeScript a JavaScript modular en dist/
npm run build
```
* Abre `frontend/public/index.html` en tu navegador para ver la interfaz conectada al diseño centralizado.

---

## 🔑 Credenciales Iniciales de Prueba (Seed Data)

Al arrancar la aplicación por primera vez, `DataInitializer` precarga automáticamente:

| Usuario | Contraseña | Rol asignado | Propósito |
| :--- | :--- | :--- | :--- |
| `admin` | `admin123` | `ADMIN` | Administración general, alta de profesionales y control total. |
| `recepcion` | `recepcion123` | `RECEPCION` | Mesa de entrada, alta de pacientes y gestión de turnos. |

---

## 🗺️ Roadmap de Implementación (100% Completado)

El proyecto se desarrolló de forma modular y verificable siguiendo los 9 changes definidos en [`CHANGES.md`](CHANGES.md), con metodología OpenSpec y Strict TDD:

- [x] **C-01 `foundation-setup`** — Scaffolding del monorepo, Docker, Gradle y TypeScript base. *(Sebastian)*
- [x] **C-02 `core-models`** — Modelo de Usuario, auditoría con soft delete y manejo global de excepciones. *(Sebastian)*
- [x] **C-03 `auth-jwt`** — Autenticación stateless JWT, Spring Security 6 y RBAC. *(Tomas - PR #5)*
- [x] **C-04 `profesionales-service`** — Catálogo y CRUD administrativo de odontólogos. *(Tomas - PR #6)*
- [x] **C-05 `pacientes-service`** — Gestión de pacientes y creación automática de historia clínica. *(Diego - PR #7)*
- [x] **C-06 `turnos-agenda-backend`** — Lógica de turnos, cálculo de disponibilidad y bloqueo pesimista anti-solapamiento. *(Gaspar - PR #8)*
- [x] **C-07 `agenda-web-frontend`** — Interfaz web interactiva de turnos y agenda en TypeScript vanilla. *(Facundo - PR #11)*
- [x] **C-08 `historia-clinica-evoluciones`** — Registro inmutable de notas clínicas por profesional. *(Diego - PR #10)*
- [x] **C-09 `notificaciones-turnos`** — Envío desacoplado y tolerante a fallos de confirmaciones por email y WhatsApp. *(Gaspar - PR #9)*

---

## 📖 Documentación Adicional

* 📚 **[Base de Conocimiento Completa](knowledge-base/README.md)**: Requisitos funcionales, modelo relacional ERD, historias de usuario y flujos E2E.
* 🎨 **[Sistema de Diseño](design-system/README.md)**: Tokens CSS, paleta de colores y componentes interactivos.
* 📋 **[Instrucciones para Agentes y Desarrolladores](AGENTS.md)**: Reglas duras de arquitectura (DTOs obligatorios, inmutabilidad clínica, convenciones REST).
