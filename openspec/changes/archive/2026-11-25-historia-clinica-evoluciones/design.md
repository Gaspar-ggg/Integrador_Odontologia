# Design — [C-08] historia-clinica-evoluciones

## Decisiones técnicas
1. **Inmutabilidad clínica estricta (RN-PA-03)**:
   - `EvolucionClinica` no hereda de `AuditableEntity` (evitando columnas mutables y baja lógica), incorpora `@Immutable` de Hibernate, columnas `updatable = false` y hooks `@PreUpdate`/`@PreRemove` que lanzan `UnsupportedOperationException`.
   - `EvolucionClinicaRepository` sobreescribe todas las variantes de `delete*` lanzando `UnsupportedOperationException` y no se exponen endpoints `PUT`, `PATCH` ni `DELETE`.
2. **Autoría clínica (RN-PA-04)**:
   - El endpoint de creación exige `@PreAuthorize("hasRole('ODONTOLOGO')")` y resuelve el profesional tratante desde el `Authentication.getName()` autenticado (`OdontologoRepository.findByUsuarioUsername`).
3. **Cierre automático de turno (RN-TU-04)**:
   - Cuando se envía `turnoId`, se verifica pertenencia al paciente y al odontólogo autenticado, unicidad de evolución por turno y transición válida `CONFIRMADO -> ATENDIDO` mediante `EstadoTurno.puedeTransicionarA(EstadoTurno.ATENDIDO)`.