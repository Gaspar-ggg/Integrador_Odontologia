# Preguntas Abiertas

## Inconsistencias detectadas

Actualmente no se detectaron inconsistencias contradictorias entre los requerimientos relevados y la arquitectura técnica propuesta. Todas las elecciones de frontend (TypeScript vanilla responsivo), backend (Spring Boot + Gradle) y base de datos (PostgreSQL + Docker Compose) están alineadas.

---

## Preguntas abiertas (priorizadas)

| Prioridad | Pregunta | Bloquea | Decisor sugerido |
|---|---|---|---|
| **Alta** | ¿Cuál es la duración exacta por defecto para los slots de turnos: 30 minutos o 45 minutos? | Modelado del generador de disponibilidad | Cátedra / Usuario |
| **Resuelta (C-09)** | Proveedor SMTP configurado vía variables de entorno (`MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`) en Spring Mail (`JavaMailSender`), permitiendo Mailtrap en desarrollo y Gmail/SendGrid en producción. | `C-09` (Cerrada) | Equipo de desarrollo |
| **Resuelta (C-09)** | WhatsApp abstraído mediante `NotificationService` (`WhatsAppNotificationAdapter`) con `WHATSAPP_MOCK_ENABLED=true` por defecto (Mock Logger) para entrega MVP según RN-NT-03. | `C-09` (Cerrada) | Cátedra / Usuario |
| **Baja** | ¿El Odontograma digital para la versión 2.0 requerirá diseño SVG interactivo pieza por pieza o un formato esquemático? | Planificación de la Fase 2 (Post-MVP) | Equipo de diseño |
