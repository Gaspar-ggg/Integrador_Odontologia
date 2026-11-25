# Tasks: [C-09] notificaciones-turnos

- [x] 1. Agregar `spring-boot-starter-mail` en `backend/build.gradle` y configurar `application.yml`.
- [x] 2. Implementar `AsyncConfig`, `TurnoConfirmadoEvent` y `TurnoNotificationListener` (`@TransactionalEventListener` + `@Async`).
- [x] 3. Implementar `NotificationService`, `EmailNotificationAdapter` y `WhatsAppNotificationAdapter`.
- [x] 4. Publicar `TurnoConfirmadoEvent` desde `TurnoService` al crear o confirmar un turno.
- [x] 5. Agregar tests unitarios (Mockito) e integración verificando tolerancia a fallos SMTP, paciente sin email y mock logger.
- [x] 6. Actualizar `knowledge-base/10_preguntas_abiertas.md`, archivar change en `openspec/changes/archive/` y marcar `[C-09]` en `CHANGES.md`.