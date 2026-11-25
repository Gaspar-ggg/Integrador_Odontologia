# Design — [C-06] turnos-agenda-backend

## Decisiones técnicas y resolución de preguntas abiertas

1. **Resolución de 10_preguntas_abiertas.md (Duración estándar de slot y horario laboral)**:
   - Se fija una duración por defecto de **30 minutos** (\genda.duracion-slot-minutos=30\) para el generador de disponibilidad, admitiendo reservas en bloques estándar de **30 o 45 minutos** según \RN-TU-02\.
   - Dado que \Odontologo\ no modela turnos rotativos en el MVP, se define una ventana laboral estándar configurable (\genda.hora-inicio=09:00\, \genda.hora-fin=18:00\) para validar \RN-TU-03\.

2. **Detección matemática de solapamiento (RN-TU-01)**:
   - Dos intervalos \[inicioA, finA)\ y \[inicioB, finB)\ se solapan si y solo si \inicioA < finB && finA > inicioB\.
   - Usar desigualdad estricta permite turnos contiguos (\10:00–10:30\ seguido de \10:30–11:00\) sin falsos positivos y detecta solapamientos totales o parciales.
   - Los turnos en estado \CANCELADO\ o con \ctivo = false\ quedan excluidos de la verificación, garantizando liberación inmediata (\RN-TU-05\).

3. **Control de concurrencia (409 Conflict)**:
   - Antes de verificar solapamientos en \TurnoService.crearTurno\, se adquiere un bloqueo pesimista (\LockModeType.PESSIMISTIC_WRITE\) sobre el registro \Odontologo\ dentro de \@Transactional\. Esto serializa reservas simultáneas sobre el mismo profesional tanto en PostgreSQL como en H2.

4. **Testabilidad temporal determinista (Clock)**:
   - Se inyecta \java.time.Clock\ en \TurnoService\ para validar fechas/horas pasadas (\RN-TU-03\) sin depender del reloj del sistema en los tests.
