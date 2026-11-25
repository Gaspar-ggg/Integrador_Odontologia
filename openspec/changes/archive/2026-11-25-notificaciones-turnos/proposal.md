# Proposal: [C-09] notificaciones-turnos

## Contexto y Motivación
Al confirmar un turno odontológico, el paciente debe recibir una notificación con fecha, hora, profesional y dirección del consultorio (US-008, RN-NT-01). El envío debe desacoplarse de la transacción de base de datos mediante eventos asíncronos para evitar rollbacks o latencia ante fallos externos (RN-NT-02), abstrayendo WhatsApp tras una interfaz con mock logger configurable (RN-NT-03).

## Alcance
- Agregar `spring-boot-starter-mail` en `backend/build.gradle` y propiedades `spring.mail` / `notificaciones` en `application.yml`.
- Implementar patrón Strategy/Adapter: interfaz `NotificationService` con `EmailNotificationAdapter` y `WhatsAppNotificationAdapter`.
- Publicar `TurnoConfirmadoEvent` desde `TurnoService` y procesarlo con `@TransactionalEventListener(AFTER_COMMIT)` + `@Async`.
- Resolver el caso borde `SU-02` (paciente sin email) y cerrar las preguntas abiertas de prioridad Media en `knowledge-base/10_preguntas_abiertas.md`.