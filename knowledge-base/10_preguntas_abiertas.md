# Preguntas Abiertas

## Inconsistencias detectadas

Actualmente no se detectaron inconsistencias contradictorias entre los requerimientos relevados y la arquitectura técnica propuesta. Todas las elecciones de frontend (TypeScript vanilla responsivo), backend (Spring Boot + Gradle) y base de datos (PostgreSQL + Docker Compose) están alineadas.

---

## Preguntas abiertas (priorizadas)

| Prioridad | Pregunta | Bloquea | Decisor sugerido |
|---|---|---|---|
| **Alta** | ¿Cuál es la duración exacta por defecto para los slots de turnos: 30 minutos o 45 minutos? | Modelado del generador de disponibilidad | Cátedra / Usuario |
| **Media** | ¿Qué proveedor SMTP se usará para el envío real de correos (Gmail SMTP, Mailtrap para testing, o SendGrid)? | Configuración del profile de producción | Equipo de desarrollo |
| **Media** | Para WhatsApp: ¿se requiere integración real con API oficial (Twilio/Meta) para la entrega final o es suficiente con el Mock Logger documentado? | Implementación del adapter externo | Cátedra / Usuario |
| **Baja** | ¿El Odontograma digital para la versión 2.0 requerirá diseño SVG interactivo pieza por pieza o un formato esquemático? | Planificación de la Fase 2 (Post-MVP) | Equipo de diseño |
