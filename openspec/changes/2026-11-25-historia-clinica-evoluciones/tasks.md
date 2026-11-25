# Tasks — [C-08] historia-clinica-evoluciones

- [x] Crear entidad JPA inmutable `EvolucionClinica` con relaciones a `HistoriaClinica`, `Odontologo` y `Turno`.
- [x] Implementar `EvolucionClinicaRepository` con bloqueo de operaciones de borrado (`RN-PA-03`) y consulta ordenada.
- [x] Implementar DTOs y `HistoriaClinicaService` aplicando `RN-PA-03`, `RN-PA-04` y `RN-TU-04`.
- [x] Implementar `HistoriaClinicaController` con endpoints `POST /api/v1/historias-clinicas/paciente/{id}/evoluciones` y `GET /api/v1/historias-clinicas/paciente/{id}`.
- [x] Agregar pruebas unitarias y de integración verificando RBAC, transición de turno e inmutabilidad.