# Visión y Objetivos

## Propósito del sistema
El sistema **Integrador Odontología** es una plataforma web integral diseñada para digitalizar, centralizar y optimizar la gestión operativa y clínica de un consultorio o clínica odontológica. Resuelve la desorganización de turnos en papel o planillas, previene el solapamiento de agendas de los profesionales odontólogos, garantiza la trazabilidad del historial clínico de los pacientes y reduce el ausentismo mediante notificaciones y recordatorios automatizados.

## Objetivos por actor

| Actor | Objetivo principal | Objetivos secundarios |
|---|---|---|
| **Paciente** | Agendar y consultar sus citas odontológicas de forma autónoma y transparente. | Recibir recordatorios oportunos vía email/WhatsApp y revisar el estado de sus consultas. |
| **Odontólogo** | Visualizar su agenda diaria organizada y registrar la evolución clínica de sus pacientes. | Acceder al historial previo de tratamientos y evitar solapamientos en su horario de atención. |
| **Recepcionista** | Gestionar la agenda global del consultorio, registrar pacientes y asignar turnos. | Confirmar asistencias, reprogramar o cancelar turnos y coordinar la recepción en sala de espera. |
| **Administrador** | Controlar el personal clínico, usuarios del sistema, disponibilidades y especialidades. | Monitorear la integridad operativa y configurar las políticas del consultorio. |

## Alcance v1.0 (MVP)

- **Autenticación y Autorización**: Control de acceso con JWT y 4 roles diferenciados (ADMIN, RECEPCION, ODONTOLOGO, PACIENTE).
- **Gestión de Pacientes**: Registro completo, búsqueda rápida y ficha de datos de contacto con baja lógica (*soft delete*).
- **Agenda y Turnos**: Reserva de citas por odontólogo, fecha y slots fijos (ej. 30 o 45 min) con validación estricta de no solapamiento.
- **Ciclo de vida del turno**: Estados controlados: `PENDIENTE`, `CONFIRMADO`, `ATENDIDO`, `CANCELADO`.
- **Historial Clínico**: Registro de notas de evolución, diagnóstico y tratamientos realizados en cada consulta.
- **Notificaciones**: Envío de confirmaciones y recordatorios por correo electrónico (SMTP) y mensajería (WhatsApp con adapter mock/desacoplado).

## Fuera de alcance v1.0

- Odontograma interactivo digital gráfico (definido para la versión 2.0).
- Facturación electrónica fiscal y liquidación de honorarios médicos.
- Módulo de cobranzas y convenios con obras sociales / prepagas complejas.
- Reportes estadísticos avanzados de rentabilidad y analítica de negocio.

## Métricas de éxito

- Cero solapamientos de turnos para un mismo odontólogo.
- Reducción medible del ausentismo mediante recordatorios automáticos de turnos.
- Tiempo de agendamiento y confirmación de turnos inferior a 1 minuto.
- 100% de consultas registradas con trazabilidad del profesional actuante.
