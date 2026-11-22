# Discovery — Integrador Odontología

**Fecha**: 2026-11-16
**Fuentes investigadas**: Sesión directa de Q&A estructurada (sin fuentes externas URL)

## 1. Problema que resuelve

Los consultorios y clínicas odontológicas a menudo gestionan sus turnos de forma manual (papel o planillas compartidas), lo que deriva en solapamiento de agendas de los profesionales, dificultad para hacer seguimiento y evolución de historias clínicas de pacientes, y alto ausentismo por falta de recordatorios oportunos. El sistema busca centralizar y digitalizar la gestión clínica y operativa de turnos e historiales.

## 2. Usuarios / roles

- **Administrador**: Administra el personal clínico (odontólogos), recepcionistas, usuarios, especialidades y configuración del sistema.
- **Recepcionista**: Gestiona la agenda general del consultorio, registra y busca pacientes, reserva y asigna turnos, y confirma o cancela citas.
- **Odontólogo**: Consulta su agenda de atención personalizada del día y registra/consulta evoluciones e historial clínico de los pacientes que atiende.
- **Paciente**: Accede al portal de autogestión para consultar sus turnos asignados, solicitar nuevas citas y recibir recordatorios.

## 3. Casos de uso

1. **Como Paciente o Recepcionista**, quiero agendar un turno seleccionando profesional, fecha y bloque horario disponible para asegurar una cita sin colisiones.
2. **Como Paciente**, quiero recibir notificaciones/recordatorios (email o WhatsApp) antes de mi consulta para no olvidar mi turno y reducir el ausentismo.
3. **Como Odontólogo**, quiero ver mi lista de turnos diarios y registrar las notas de evolución clínica del paciente para mantener la trazabilidad de su tratamiento.
4. **Como Recepcionista o Paciente**, quiero cancelar o reprogramar un turno para liberar el slot y permitir que otro paciente lo ocupe.
5. **Como Administrador**, quiero gestionar los usuarios, roles y disponibilidades de los odontólogos para mantener actualizada la clínica.

## 4. Competidores / soluciones existentes

| Competidor / Alternativa | Problema que resuelve | Pricing / Tipo | Diferenciadores |
|---|---|---|---|
| Planillas Excel / Papel | Registro básico sin costo inicial | Gratuito | Muy propenso a solapamientos, sin validaciones ni portal de autogestión |
| Dentalink / Software médico comercial | Gestión completa clínica y odontograma en la nube | Suscripción mensual por profesional | Completo pero costoso y complejo para clínicas pequeñas o consultorios independientes |
| Google Calendar / Calendly | Agendamiento de reuniones | Freemium | No posee historia clínica, roles RBAC médicos ni ficha odontológica |

## 5. Funcionalidades necesarias (MVP)

- Autenticación y control de acceso robusto basado en JWT para 4 roles (ADMIN, RECEPCION, ODONTOLOGO, PACIENTE).
- Gestión de turnos con agenda por odontólogo, duración fija (bloques de 30 o 45 minutos) y validación estricta de no solapamiento.
- Registro y ficha de pacientes (datos personales, contacto, información básica).
- Historial clínico / notas de evolución asociadas a cada consulta odontológica.
- Módulo de notificaciones (email / WhatsApp) para confirmación y recordatorio automático de turnos.

## 6. Funcionalidades opcionales (Fase 2 / Post-MVP)

- Odontograma digital interactivo gráfico con detalle de caras y piezas dentales.
- Gestión de obras sociales, prepagas y facturación / liquidación de honorarios a profesionales.
- Reportes estadísticos de asistencia, cancelaciones y motivos frecuentes de consulta.

## 7. Reglas de negocio

- **Sin solapamiento**: Un odontólogo no puede tener más de una reserva activa en el mismo intervalo de tiempo.
- **Ciclo de vida del turno**: Estados controlados: `PENDIENTE`, `CONFIRMADO`, `ATENDIDO`, `CANCELADO`.
- **Duración estándar**: Slots predefinidos fijos (ej. 30 o 45 min) para previsibilidad de la agenda.
- **Privacidad y confidencialidad médica**: Los pacientes solo acceden a sus propios turnos; las evoluciones clínicas solo pueden ser creadas/modificadas por el profesional que atendió.

## 8. Integraciones

- Servicio de mensajería / correo SMTP (ej. Spring Mail / SendGrid / Gmail) para notificaciones por email.
- API de mensajería (ej. WhatsApp Business API / Twilio o adapter simulado para desarrollo) para recordatorios de turnos.
- Base de datos relacional (MySQL o PostgreSQL).

## 9. Restricciones

- **Backend**: Java con Spring Boot, gestionado con Gradle.
- **Frontend**: TypeScript vanilla con maquetación responsiva moderna (sin frameworks pesados como React/Angular).
- **Arquitectura**: Arquitectura en capas limpia, APIs RESTful documentadas y desacopladas del cliente.

## 10. Riesgos

- **Concurrencia en reservas**: Riesgo de reservas simultáneas del mismo turno por dos usuarios concurrentes; requiere manejo transaccional adecuado y bloqueo/constraint único en base de datos.
- **Complejidad de TypeScript Vanilla**: Evitar código monolítico o difícil de mantener en frontend; diseñar una arquitectura modular limpia con separación clara de componentes/vistas y servicios de API.
- **Dependencias de mensajería externa**: Las APIs de WhatsApp/SMS requieren cuotas o credenciales; se implementará con patrón Adapter/Strategy para permitir ejecución con mock en local/tests sin costo.

## 11. Preguntas abiertas

- Definición final de motor de BD entre MySQL y PostgreSQL para producción/entorno de evaluación.
- Ajuste final de plantillas y tiempos de antelación para el envío automático de notificaciones de turnos.
