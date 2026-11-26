/**
 * Contratos de agenda y turnos.
 *
 * Reflejan `DisponibilidadResponseDTO`, `SlotHorarioDTO`,
 * `TurnoRequestDTO`, `TurnoResponseDTO` y el enum `EstadoTurno` del backend.
 *
 * Fechas/horas: `LocalDate` -> `YYYY-MM-DD`, `LocalDateTime` -> `YYYY-MM-DDTHH:mm:ss`.
 */

export const ESTADOS_TURNO = ['PENDIENTE', 'CONFIRMADO', 'ATENDIDO', 'CANCELADO'] as const;

export type EstadoTurno = (typeof ESTADOS_TURNO)[number];

/** Estados permitidos al crear un turno (el backend los valida). */
export type EstadoTurnoInicial = Extract<EstadoTurno, 'PENDIENTE' | 'CONFIRMADO'>;

/** Guard de runtime para estados recibidos desde el backend. */
export function isEstadoTurno(value: string): value is EstadoTurno {
  return (ESTADOS_TURNO as readonly string[]).includes(value);
}

/** Bloque horario libre dentro de la jornada de un odontólogo. */
export interface SlotHorario {
  fechaHoraInicio: string;
  fechaHoraFin: string;
  horaInicio: string;
  horaFin: string;
}

/** Respuesta de `GET /api/v1/odontologos/{id}/disponibilidad?fecha=YYYY-MM-DD`. */
export interface Disponibilidad {
  odontologoId: number;
  odontologoNombreCompleto: string;
  fecha: string;
  duracionMinutos: number;
  slotsDisponibles: SlotHorario[];
}

export interface Turno {
  id: number;
  pacienteId: number;
  pacienteNombreCompleto: string;
  odontologoId: number;
  odontologoNombreCompleto: string;
  odontologoEspecialidad: string;
  fechaHoraInicio: string;
  fechaHoraFin: string;
  estado: EstadoTurno;
  motivoConsulta: string | null;
  activo: boolean;
}

/**
 * Payload de `POST /api/v1/turnos`.
 * `fechaHoraFin` es opcional: si se omite, el backend aplica la duración
 * estándar configurada (30 min por defecto).
 */
export interface TurnoRequest {
  pacienteId: number;
  odontologoId: number;
  fechaHoraInicio: string;
  fechaHoraFin?: string;
  motivoConsulta?: string;
  estado?: EstadoTurnoInicial;
}

/** Filtros de `GET /api/v1/turnos`. */
export interface TurnoFiltros {
  odontologoId?: number;
  pacienteId?: number;
  fecha?: string;
  estado?: EstadoTurno;
}
