# Propuesta: C-05 pacientes-service

## ¿Por qué?

La gestión de pacientes y mesa de entrada es el núcleo operativo de la clínica. Recepcionistas y administradores necesitan registrar pacientes (incluso sin cuenta de usuario web según SU-02), asociar automáticamente su historia clínica base (RN-PA-02) y realizar búsquedas ágiles por DNI o nombre para agilizar la atención diaria.

## ¿Qué cambia?

1. Entidad `Paciente` con soft delete (`activo = true`) y atributos demográficos/obra social.
2. Entidad `HistoriaClinica` vinculada 1:1 con el paciente.
3. Repositorios `PacienteRepository` e `HistoriaClinicaRepository` con métodos de búsqueda.
4. Lógica transaccional en `PacienteService` para creación atómica de paciente + historia clínica.
5. Endpoints REST en `PacienteController` protegidos con `@PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")`.
6. Tests unitarios y de integración con cobertura completa.

## Capacidades

- `pacientes`: gestión y búsqueda de pacientes con historia clínica automática.
