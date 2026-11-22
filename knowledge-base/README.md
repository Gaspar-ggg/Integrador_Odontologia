# Integrador Odontología — Base de Conocimiento

Base de conocimiento canónica y estructurada para el proyecto **Integrador Odontología** (Desarrollo de Software - 3° Año).

## Índice de Archivos Canónicos

| # | Archivo | Contenido principal |
|---|---|---|
| 01 | [01_vision_y_objetivos.md](01_vision_y_objetivos.md) | Propósito del sistema, objetivos por actor, alcance v1.0, fuera de alcance y métricas. |
| 02 | [02_descripcion_general.md](02_descripcion_general.md) | Stack tecnológico (Spring Boot, Gradle, TypeScript vanilla, PostgreSQL), arquitectura e integraciones. |
| 03 | [03_actores_y_roles.md](03_actores_y_roles.md) | Actores (Admin, Recepción, Odontólogo, Paciente), matriz de permisos RBAC y rutas públicas. |
| 04 | [04_modelo_de_datos.md](04_modelo_de_datos.md) | Modelo relacional, diagrama ERD, detalle de entidades, constraints y seed data. |
| 05 | [05_reglas_de_negocio.md](05_reglas_de_negocio.md) | Reglas codificadas por dominio (`RN-AU`, `RN-PA`, `RN-TU`, `RN-NT`, `RN-EX`). |
| 06 | [06_funcionalidades.md](06_funcionalidades.md) | Historias de usuario (`US-001` a `US-008`) con criterios de aceptación por épica. |
| 07 | [07_flujos_principales.md](07_flujos_principales.md) | Flujos extremo a extremo (Autenticación JWT, Reserva de Turnos, Atención Clínica y Evolución). |
| 08 | [08_arquitectura_propuesta.md](08_arquitectura_propuesta.md) | Patrones de diseño, estructura de directorios, seguridad y variables de entorno. |
| 09 | [09_decisiones_y_supuestos.md](09_decisiones_y_supuestos.md) | Decisiones arquitectónicas fundamentadas (`DD-01` a `DD-03`) y supuestos clave (`SU-01`, `SU-02`). |
| 10 | [10_preguntas_abiertas.md](10_preguntas_abiertas.md) | Preguntas abiertas y de configuración priorizadas para las siguientes fases. |

## Quick Start para Desarrolladores

1. **Entender el dominio**: Leer [01_vision_y_objetivos.md](01_vision_y_objetivos.md) y [03_actores_y_roles.md](03_actores_y_roles.md).
2. **Entender los datos**: Revisar [04_modelo_de_datos.md](04_modelo_de_datos.md) y las reglas en [05_reglas_de_negocio.md](05_reglas_de_negocio.md).
3. **Entender la arquitectura**: Consultar [02_descripcion_general.md](02_descripcion_general.md) y [08_arquitectura_propuesta.md](08_arquitectura_propuesta.md).
4. **Implementar funcionalidades**: Seguir [07_flujos_principales.md](07_flujos_principales.md) y [06_funcionalidades.md](06_funcionalidades.md).
5. **Antes de codificar**: Verificar [10_preguntas_abiertas.md](10_preguntas_abiertas.md) y [09_decisiones_y_supuestos.md](09_decisiones_y_supuestos.md).

## Resumen Ejecutivo

El sistema gestiona de punta a punta la operatoria de un consultorio o clínica odontológica: los pacientes pueden solicitar y gestionar turnos en línea recibiendo avisos preventivos; el personal administrativo gestiona la agenda y fichas de pacientes; y los odontólogos visualizan sus citas y asientan las notas de evolución clínica de manera digital, segura e inmutable.
Todo el backend está construido en Java con Spring Boot y Gradle sobre PostgreSQL (con Docker), y el frontend en TypeScript puro (Vanilla) modular y responsivo.
