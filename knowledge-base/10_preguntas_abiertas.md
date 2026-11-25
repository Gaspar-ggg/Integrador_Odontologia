# Preguntas Abiertas

## Inconsistencias detectadas

Actualmente no se detectaron inconsistencias contradictorias entre los requerimientos relevados y la arquitectura técnica propuesta. Todas las elecciones de frontend (TypeScript vanilla responsivo), backend (Spring Boot + Gradle) y base de datos (PostgreSQL + Docker Compose) están alineadas.

---

## Preguntas abiertas (priorizadas)

| Prioridad | Pregunta | Bloquea | Decisor sugerido |
|---|---|---|---|
| ~~**Alta**~~ *(Resuelta en C-06)* | Duración estándar definida en **30 minutos** por defecto (genda.duracion-slot-minutos=30), admitiendo bloques fijos de 30 o 45 min y franja 09:00–18:00. | Desbloqueado (C-06 archivado) | Equipo / C-06 |
| **Media** | ¿Qué proveedor SMTP se usará para el envío real de correos (Gmail SMTP, Mailtrap para testing, o SendGrid)? | Configuración del profile de producción | Equipo de desarrollo |
| **Media** | Para WhatsApp: ¿se requiere integración real con API oficial (Twilio/Meta) para la entrega final o es suficiente con el Mock Logger documentado? | Implementación del adapter externo | Cátedra / Usuario |
| **Baja** | ¿El Odontograma digital para la versión 2.0 requerirá diseño SVG interactivo pieza por pieza o un formato esquemático? | Planificación de la Fase 2 (Post-MVP) | Equipo de diseño |
