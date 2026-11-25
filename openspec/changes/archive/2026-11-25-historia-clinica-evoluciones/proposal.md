# Proposal — [C-08] historia-clinica-evoluciones

## Contexto y objetivo
Implementar el registro clínico inmutable de evoluciones odontológicas vinculadas a la `HistoriaClinica` del paciente, al `Odontologo` tratante y opcionalmente al `Turno` atendido.

## Alcance
- Entidad JPA inmutable `EvolucionClinica` (`RN-PA-03`: solo INSERT y SELECT, bloqueo físico de UPDATE y DELETE).
- Control de acceso estricto por rol (`RN-PA-04`: únicamente `ROLE_ODONTOLOGO` puede registrar evoluciones).
- Transición automática del turno asociado a estado `ATENDIDO` (`RN-TU-04`) al registrar la evolución.
- Endpoints REST `POST /api/v1/historias-clinicas/paciente/{id}/evoluciones` y `GET /api/v1/historias-clinicas/paciente/{id}`.