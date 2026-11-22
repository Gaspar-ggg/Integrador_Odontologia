# Integrador Odontología — Instrucciones para Agentes

> Este archivo (y su copia `CLAUDE.md`) es lo PRIMERO que todo agente lee al entrar al repo.
> Generado a partir de `knowledge-base/` y `CHANGES.md`. No editar a mano sin re-sincronizar ambos archivos.

---

## Stack Tecnológico

| Capa | Tecnologías | Versión mínima | Justificación |
|---|---|---|---|
| **Backend** | Java, Spring Boot, Gradle | Java 17+, Spring Boot 3.x | Robustez, ecosistema empresarial maduro, seguridad integrada y tipado estático seguro. |
| **Persistencia** | Spring Data JPA / Hibernate | JPA 3.x | ORM estándar, migraciones transparentes y repositorios basados en convenciones. |
| **Base de datos** | PostgreSQL (Docker Compose) / H2 | PostgreSQL 15+ | Motor relacional transaccional ACID robusto; H2 para tests automatizados rápidos en memoria. |
| **Seguridad** | Spring Security + JWT | Spring Security 6.x | Stateless, desacoplado de sesiones de servidor, autorización granular por rol. |
| **Frontend** | TypeScript Vanilla (compilado directo con `tsc`), HTML5, CSS3 Responsivo | TypeScript 5.x | Sin sobrecarga de frameworks SPA complejos; modular, tipado estricto y código nativo portable. |
| **Notificaciones** | Spring Mail (JavaMailSender) + Adapter Mensajería | Spring Boot Starter Mail | Envío de correos SMTP estándar y arquitectura de adapters para WhatsApp. |

Detalle completo: [knowledge-base/02_descripcion_general.md](knowledge-base/02_descripcion_general.md)

---

## Base de Conocimiento

La fuente de verdad del dominio vive en `knowledge-base/`. **Leé el archivo relevante ANTES de implementar.**

| Archivo | Cuándo leerlo |
|---------|---------------|
| [01_vision_y_objetivos.md](knowledge-base/01_vision_y_objetivos.md) | Entender propósito, objetivos y alcance del sistema |
| [02_descripcion_general.md](knowledge-base/02_descripcion_general.md) | Stack técnico, arquitectura de capas y endpoints REST |
| [03_actores_y_roles.md](knowledge-base/03_actores_y_roles.md) | Actores, matriz de permisos RBAC y rutas públicas vs protegidas |
| [04_modelo_de_datos.md](knowledge-base/04_modelo_de_datos.md) | Diagrama relacional, entidades, constraints y seed data |
| [05_reglas_de_negocio.md](knowledge-base/05_reglas_de_negocio.md) | Reglas codificadas (`RN-AU`, `RN-PA`, `RN-TU`, `RN-NT`, `RN-EX`) |
| [06_funcionalidades.md](knowledge-base/06_funcionalidades.md) | Historias de usuario (`US-001` a `US-008`) con criterios de aceptación |
| [07_flujos_principales.md](knowledge-base/07_flujos_principales.md) | Flujos E2E (Autenticación JWT, Reserva de Turno, Atención Médica) |
| [08_arquitectura_propuesta.md](knowledge-base/08_arquitectura_propuesta.md) | Patrones de diseño, estructura de directorios y env vars |
| [09_decisiones_y_supuestos.md](knowledge-base/09_decisiones_y_supuestos.md) | Decisiones arquitectónicas (`DD-01` a `DD-03`) y supuestos (`SU-01`, `SU-02`) |
| [10_preguntas_abiertas.md](knowledge-base/10_preguntas_abiertas.md) | ⚠️ Inconsistencias y definiciones abiertas a resolver antes de codear |

> ⚠️ Resolver las preguntas de prioridad **Alta** de `10_preguntas_abiertas.md` antes de arrancar el primer change (definición de slot estándar: 30 min o 45 min).

---

## Skills Disponibles

| Agente | Rol | Skills que carga |
|--------|-----|------------------|
| **Backend Core** | Spring Boot / JPA / DTOs / Controladores | `spring-data-jpa`, `crud-rest-controller`, `dto-creator`, `mapper-creator`, `run-tests`, `codefmt` |
| **Backend Aux & Sec** | Seguridad, JWT, Diagnóstico, Integraciones | `spring-security-configuration`, `java-debug`, `coverage`, `mutation-testing` |
| **Frontend** | TypeScript vanilla, UI & Tokens | `design-system`, `emil-design-eng`, `design-taste-frontend-v1` |
| **Orquestación** | SDD / OPSX / Documentación | `active-orchestrator`, `roadmap-generator`, `kb-creator`, `skill-registry`, `agent-instruction` |

Cargá la skill correspondiente al contexto ANTES de escribir código.

> Los compact rules de cada skill los resuelve el orquestador desde `.atl/skill-registry.md` (generado por `skill-registry`; no versionado — no está en el repo). Esta tabla solo mapea skill→rol.

---

## Roadmap de Changes

El plan de implementación completo está en [CHANGES.md](CHANGES.md). Resumen:

- **Total**: 9 changes en 5 fases (Fase 0 a Fase 4).
- **Camino crítico** (6 changes): `C-01 → C-02 → C-03 → C-04 → C-06 → C-07`.
- **Primer change**: `C-01` (`foundation-setup`).

**Antes de cualquier `/opsx:propose`**: leé [CHANGES.md](CHANGES.md), identificá las dependencias del change y los archivos de "Leer antes".

---

## Reglas Duras (específicas del proyecto)

> Reglas **globales** ya definidas en el entorno (orquestador, governance, TDD estricto, engram): el proyecto las **hereda**, no se repiten acá.

Son contrato obligatorio del proyecto; romperlas es considerado un defecto:

- **NUNCA exponer entidades JPA en controladores REST** → Mapear siempre a DTOs (`*RequestDTO`, `*ResponseDTO`) con validaciones Jakarta (`@Valid`, `@NotNull`, `@NotBlank`).
- **NUNCA capturar excepciones genéricas silenciándolas** → Centralizar el manejo en `@RestControllerAdvice` respondiendo estructura estándar `ErrorResponseDTO` con HTTP status semántico.
- **NUNCA borrar físicamente registros clínicos ni usuarios** → Aplicar soft delete (`activo = false`) preservando integridad referencial, y respetar inmutabilidad estricta (solo INSERT y SELECT) en `EvolucionClinica`.
- **NUNCA permitir solapamiento de turnos para un mismo odontólogo** → Validar disponibilidad con `@Transactional` en `TurnoService` y asegurar unicidad a nivel base de datos (`idx_odontologo_fecha`).
- **NUNCA usar `any` en TypeScript** → Tipar estricto mediante interfaces compartidas coincidentes con los DTOs del backend y compilar en modo estricto (`tsc`).
- **NUNCA hardcodear estilos visuales arbitrarios** → Consumir exclusivamente las variables CSS de `design-system/tokens.css` y la marca de `design-system/brand.js`.
- **NUNCA ejecutar tests automáticos contra la BD de producción/desarrollo** → Utilizar H2 en memoria para tests unitarios/integración en Gradle (`./gradlew test`) y PostgreSQL en Docker para ejecución local.

---

## Flujo de Trabajo

```
1. Leer la KB relevante (knowledge-base/)        → entender el dominio
2. Identificar el change en CHANGES.md           → respetar dependencias
3. /opsx:propose C-NN-nombre                     → proposal + design + specs + tasks
4. Implementar las tasks (cargando skills)       → respetando las reglas duras
5. /opsx:archive C-NN-nombre + marcar [x]        → cerrar el change
```

Aplicar TODAS las reglas duras en cada paso. Ante conflicto entre la KB y este archivo, las reglas duras prevalecen.
