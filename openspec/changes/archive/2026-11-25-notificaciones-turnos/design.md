# Design: [C-09] notificaciones-turnos

## Arquitectura
1. `TurnoService` publica `TurnoConfirmadoEvent` vía `ApplicationEventPublisher` al crear un turno `CONFIRMADO` o al confirmar uno `PENDIENTE`.
2. `TurnoNotificationListener` escucha con `@Async("notificationTaskExecutor")` y `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)`, iterando todos los beans `NotificationService`.
3. `EmailNotificationAdapter` envía mediante `JavaMailSender`. Si `pacienteEmail` es nulo o vacío (`SU-02`), registra `WARN` y omite sin error.
4. `WhatsAppNotificationAdapter` opera con `WHATSAPP_MOCK_ENABLED=true` por defecto emitiendo log estructurado `[WHATSAPP-MOCK]`.
5. Ambos adaptadores capturan cualquier excepción (`MailException`, `Exception`) y registran `log.error` sin propagar fallos (`RN-NT-02`).