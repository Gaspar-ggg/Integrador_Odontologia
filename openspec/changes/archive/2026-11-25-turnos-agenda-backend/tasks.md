# Tasks — [C-06] turnos-agenda-backend

## 1. Modelo, Repositorio y Configuración

- [x] 1.1 Crear enum \EstadoTurno\ con reglas de transición de estado (RN-TU-04).
- [x] 1.2 Crear entidad JPA \Turno\ con índice \idx_odontologo_fecha\ y soft delete (\@SQLDelete\, \@SQLRestriction\).
- [x] 1.3 Agregar bloqueo pesimista \indByIdForUpdate\ en \OdontologoRepository\ y crear \TurnoRepository\ con consulta anti-solapamiento y filtros.
- [x] 1.4 Configurar \Clock\ bean en \TimeConfig\ y propiedades de agenda en \pplication.yml\.

## 2. DTOs, Servicio y Controlador REST

- [x] 2.1 Crear \TurnoRequestDTO\, \TurnoResponseDTO\, \DisponibilidadResponseDTO\ y \SlotHorarioDTO\.
- [x] 2.2 Implementar \TurnoService\ (disponibilidad dinámica, reserva transaccional anti-solapamiento, cancelación y listado filtrado).
- [x] 2.3 Implementar \TurnoController\ con \GET /api/v1/odontologos/{id}/disponibilidad\, \POST /api/v1/turnos\, \PUT /api/v1/turnos/{id}/cancelar\ y \GET /api/v1/turnos\.

## 3. Verificación TDD y Cierre OPSX

- [x] 3.1 Escribir tests unitarios y de integración (disponibilidad, colisión 409, concurrencia multi-hilo, turno contiguo de borde, fechas pasadas, transiciones de estado y endpoints REST).
- [x] 3.2 Ejecutar \./gradlew test\ y \./gradlew build\ contra H2 verificando 100% en verde.
- [x] 3.3 Archivar el change en \openspec/changes/archive/\ y marcar \[x]\ en \CHANGES.md\.
