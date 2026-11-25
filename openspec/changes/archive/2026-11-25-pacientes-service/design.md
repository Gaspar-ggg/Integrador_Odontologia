# Diseño: C-05 pacientes-service

## Arquitectura

- **Capa Modelo**:
  - `Paciente`: hereda `AuditableEntity`, tabla `pacientes`, DNI único, `@SQLRestriction("activo = true")`.
  - `HistoriaClinica`: tabla `historias_clinicas`, relación `@OneToOne` con `Paciente`, fecha de creación y observaciones iniciales.
- **Capa Repositorio**:
  - `PacienteRepository`: `findByDni`, `findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase`, `existsByDni`.
  - `HistoriaClinicaRepository`: `findByPacienteId`.
- **Capa Servicio**:
  - `PacienteService`: `@Transactional` para creación conjunta de paciente e historia clínica base.
  - Validación de unicidad de DNI (HTTP 409).
- **Capa Controlador**:
  - `PacienteController`: `POST /api/v1/pacientes`, `GET /api/v1/pacientes/buscar`, `GET /api/v1/pacientes/{id}`, `DELETE /api/v1/pacientes/{id}`.
  - DTOs dedicados con Jakarta Validation (`PacienteRequestDTO`, `PacienteResponseDTO`, `HistoriaClinicaResponseDTO`).
