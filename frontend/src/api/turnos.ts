/**
 * Cliente de turnos y disponibilidad.
 *
 * - `GET  /api/v1/odontologos/{id}/disponibilidad?fecha=YYYY-MM-DD`
 * - `POST /api/v1/turnos`
 * - `GET  /api/v1/turnos`
 * - `PUT  /api/v1/turnos/{id}/cancelar`
 * - `PUT  /api/v1/turnos/{id}/confirmar` (ADMIN, RECEPCION)
 */

import type {
  Disponibilidad,
  Turno,
  TurnoFiltros,
  TurnoRequest,
} from '../models/index.js';
import { apiRequest } from './client.js';

/** Slots libres de un odontólogo para una fecha (`YYYY-MM-DD`). */
export function obtenerDisponibilidad(
  odontologoId: number,
  fecha: string,
): Promise<Disponibilidad> {
  return apiRequest<Disponibilidad>(`/odontologos/${odontologoId}/disponibilidad`, {
    query: { fecha },
  });
}

export function crearTurno(request: TurnoRequest): Promise<Turno> {
  return apiRequest<Turno>('/turnos', {
    method: 'POST',
    body: request,
  });
}

export function listarTurnos(filtros: TurnoFiltros = {}): Promise<Turno[]> {
  return apiRequest<Turno[]>('/turnos', {
    query: {
      odontologoId: filtros.odontologoId,
      pacienteId: filtros.pacienteId,
      fecha: filtros.fecha,
      estado: filtros.estado,
    },
  });
}

export function cancelarTurno(id: number): Promise<Turno> {
  return apiRequest<Turno>(`/turnos/${id}/cancelar`, { method: 'PUT' });
}

export function confirmarTurno(id: number): Promise<Turno> {
  return apiRequest<Turno>(`/turnos/${id}/confirmar`, { method: 'PUT' });
}
