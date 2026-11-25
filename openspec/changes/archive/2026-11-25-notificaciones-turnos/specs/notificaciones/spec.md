# Specification: Notificaciones de Turnos

## Reglas Cubiertas
- **RN-NT-01**: Confirmación con fecha, hora, odontólogo y dirección del consultorio.
- **RN-NT-02**: Tolerancia a fallas sin abortar ni revertir la reserva del turno.
- **RN-NT-03**: WhatsApp abstraído con modo mock logger (`WHATSAPP_MOCK_ENABLED=true`).

## Escenarios de Aceptación
- **Escenario 1 (Confirmación exitosa)**: Al crear o confirmar un turno con email y teléfono, se ejecutan `EmailNotificationAdapter` y `WhatsAppNotificationAdapter` tras el commit.
- **Escenario 2 (Paciente sin email - SU-02)**: Si `paciente.email` es nulo o vacío, se omite el correo con `WARN` y se ejecuta WhatsApp normalmente.
- **Escenario 3 (Falla SMTP controlada - RN-NT-02)**: Ante `MailException` en `JavaMailSender`, se loguea el error y el turno queda persistido en `CONFIRMADO`.
- **Escenario 4 (WhatsApp Mock Logger - RN-NT-03)**: Con `WHATSAPP_MOCK_ENABLED=true`, se registra el log simulado sin llamadas externas.