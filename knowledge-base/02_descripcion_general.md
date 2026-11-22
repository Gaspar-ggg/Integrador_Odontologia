# Descripción General

## Stack tecnológico

| Capa | Tecnologías | Versión mínima | Justificación |
|---|---|---|---|
| **Backend** | Java, Spring Boot, Gradle | Java 17+, Spring Boot 3.x | Robustez, ecosistema empresarial maduro, seguridad integrada y tipado estático seguro. |
| **Persistencia** | Spring Data JPA / Hibernate | JPA 3.x | ORM estándar, migraciones transparentes y repositorios basados en convenciones. |
| **Base de datos** | PostgreSQL (Docker Compose) / H2 | PostgreSQL 15+ | Motor relacional transaccional ACID robusto; H2 para tests automatizados rápidos en memoria. |
| **Seguridad** | Spring Security + JWT | Spring Security 6.x | Stateless, desacoplado de sesiones de servidor, autorización granular por rol. |
| **Frontend** | TypeScript Vanilla (compilado directo con `tsc`), HTML5, CSS3 Responsivo | TypeScript 5.x | Sin sobrecarga de frameworks SPA complejos; modular, tipado estricto y código nativo portable. |
| **Notificaciones** | Spring Mail (JavaMailSender) + Adapter Mensajería | Spring Boot Starter Mail | Envío de correos SMTP estándar y arquitectura de adapters para WhatsApp. |

## Arquitectura general

El sistema sigue una arquitectura cliente-servidor desacoplada basada en una API RESTful stateless:

```
+-----------------------------------------------------------+
|                  Frontend (Web Client)                    |
|   HTML5 + CSS3 Responsivo + TypeScript Vanilla (tsc)      |
+-----------------------------------------------------------+
                            |
                     HTTPS / JSON (REST)
                     (JWT Bearer Token)
                            v
+-----------------------------------------------------------+
|               Backend (Spring Boot API)                   |
|  [Security Filter Chain] -> [Controller Layer]            |
|                                    |                      |
|                           [Service Layer]                 |
|                            (Business Logic)               |
|                                    |                      |
|                         [Repository / JPA]                |
+-----------------------------------------------------------+
               |                               |
               v                               v
    +--------------------+           +--------------------+
    | PostgreSQL Database|           | External Services  |
    | (Docker Container) |           | - SMTP Server      |
    +--------------------+           | - WhatsApp Gateway |
                                     +--------------------+
```

## Integraciones externas

| Servicio | Propósito | Tipo | Detalle |
|---|---|---|---|
| **Servidor SMTP (Mail)** | Confirmaciones de alta de turno y recordatorios preventivos. | SMTP / TLS | Implementado con `JavaMailSender` (configurable vía variables de entorno). |
| **Gateway WhatsApp** | Mensajería de texto directa con el paciente. | HTTP REST / Strategy Pattern | Implementado mediante interfaz desacoplada `NotificationService` con adapter Mock para desarrollo y test. |
| **PostgreSQL DB** | Almacenamiento persistente relacional. | JDBC / TCP | Levantado localmente mediante `docker-compose.yml`. |

## API REST (Resumen de Endpoints Principales)

- **`/api/v1/auth`**:
  - `POST /login`: Autenticación con credenciales y emisión de JWT.
  - `POST /register`: Registro público para pacientes.
- **`/api/v1/usuarios`**:
  - `GET /`: Listado de usuarios del sistema (solo ADMIN).
  - `POST /`: Creación de profesionales o personal administrativo.
  - `GET /{id}` / `PUT /{id}` / `DELETE /{id}`: Gestión de perfiles y baja lógica.
- **`/api/v1/odontologos`**:
  - `GET /`: Directorio de profesionales disponibles.
  - `GET /{id}/disponibilidad`: Horarios libres según fecha y duración fija de slot.
- **`/api/v1/pacientes`**:
  - `GET /`: Búsqueda y filtrado de pacientes (ADMIN, RECEPCION, ODONTOLOGO).
  - `POST /`: Registro de paciente.
  - `GET /{id}` / `PUT /{id}`: Ficha de paciente.
- **`/api/v1/turnos`**:
  - `GET /`: Consulta de turnos (filtrados por odontólogo, fecha o paciente).
  - `POST /`: Reserva de nuevo turno (valida disponibilidad estricta).
  - `PATCH /{id}/estado`: Transición de estado (`CONFIRMADO`, `ATENDIDO`, `CANCELADO`).
- **`/api/v1/historias-clinicas`**:
  - `GET /paciente/{pacienteId}`: Expediente clínico completo.
  - `POST /paciente/{pacienteId}/evoluciones`: Registro de consulta médica por el odontólogo.
