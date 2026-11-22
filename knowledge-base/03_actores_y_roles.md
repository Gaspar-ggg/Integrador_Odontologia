# Actores y Roles

## Actores del sistema

| Actor | Rol en el sistema | Descripción y Modo de Interacción |
|---|---|---|
| **Administrador** | `ROLE_ADMIN` | Control total del sistema. Configura profesionales, recepcionistas, usuarios, horarios base de atención y audita registros. |
| **Recepcionista** | `ROLE_RECEPCION` | Personal de mesa de entrada. Gestiona la agenda compartida de turnos, atiende pacientes presenciales o telefónicos, y actualiza estados de citas. |
| **Odontólogo** | `ROLE_ODONTOLOGO` | Profesional clínico. Accede a su agenda de citas, visualiza la ficha del paciente asignado y redacta las evoluciones odontológicas de la consulta. |
| **Paciente** | `ROLE_PACIENTE` | Usuario final del servicio. Autogestiona sus citas, consulta el estado de sus reservas y recibe avisos informativos. |

## RBAC — Matriz de permisos

| Recurso / Operación | ADMIN | RECEPCION | ODONTOLOGO | PACIENTE |
|---|:---:|:---:|:---:|:---:|
| **Gestionar Usuarios del Personal** (`/usuarios`) | `CRUD` | `-` | `-` | `-` |
| **Gestionar Perfil Propio** | `R, U` | `R, U` | `R, U` | `R, U` |
| **Crear / Buscar Pacientes** | `CRUD` | `CRUD` | `R` (asociados) | `R, U` (solo su perfil) |
| **Ver Agenda Global de Turnos** | `R` | `R` | `-` | `-` |
| **Ver Agenda Propia de Turnos** | `R` | `-` | `R` | `R` (solo sus citas) |
| **Agendar Turno** | `C` | `C` | `-` | `C` (a su nombre) |
| **Cancelar / Reprogramar Turno** | `U` | `U` | `U` (los suyos) | `U` (los suyos) |
| **Cambiar Estado a Atendido** | `-` | `-` | `U` | `-` |
| **Ver Historial Clínico** | `R` | `-` | `R` | `R` (solo el propio) |
| **Registrar Evolución Clínica** | `-` | `-` | `C` | `-` |
| **Baja Lógica (Soft Delete)** | `D` | `D` (pacientes) | `-` | `-` |

*Leyenda: C (Create), R (Read), U (Update), D (Delete/Soft Delete), - (Sin acceso).*

## Rutas públicas (Sin autenticación requerida)

- `POST /api/v1/auth/login`: Autenticación con usuario/contraseña.
- `POST /api/v1/auth/register`: Registro inicial de nuevo paciente.
- `GET /api/v1/public/odontologos`: Listado público de profesionales y especialidades para la visualización del paciente antes de reservar.
- Archivos estáticos del frontend público: `/index.html`, `/login.html`, `/registro.html`, estilos CSS y scripts compilados.
