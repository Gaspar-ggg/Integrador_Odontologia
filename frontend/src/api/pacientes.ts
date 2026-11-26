/**
 * Cliente de pacientes.
 *
 * - `POST   /api/v1/pacientes`          (ADMIN, RECEPCION)
 * - `GET    /api/v1/pacientes/buscar`   (ADMIN, RECEPCION)
 * - `GET    /api/v1/pacientes/{id}`     (ADMIN, RECEPCION)
 * - `DELETE /api/v1/pacientes/{id}`     (ADMIN, RECEPCION, baja lógica)
 */

import type { Paciente, PacienteFiltros, PacienteRequest } from '../models/index.js';
import { apiRequest } from './client.js';

export function crearPaciente(request: PacienteRequest): Promise<Paciente> {
  return apiRequest<Paciente>('/pacientes', {
    method: 'POST',
    body: request,
  });
}

/** Búsqueda por DNI exacto o por nombre/apellido (`q`). */
export function buscarPacientes(filtros: PacienteFiltros = {}): Promise<Paciente[]> {
  return apiRequest<Paciente[]>('/pacientes/buscar', {
    query: { dni: filtros.dni, q: filtros.q },
  });
}

export function obtenerPaciente(id: number): Promise<Paciente> {
  return apiRequest<Paciente>(`/pacientes/${id}`);
}

/**
 * Endpoint previsto para que el paciente autenticado consulte su propia ficha.
 * El backend actual en upstream/main no lo expone: el dashboard trata
 * 400/403/404/405/500/501 como "vínculo no disponible" y sigue sin romper.
 */
export function obtenerPacienteActual(): Promise<Paciente> {
  return apiRequest<Paciente>('/pacientes/me');
}

/** Baja lógica del paciente (el backend responde 204). */
export function eliminarPaciente(id: number): Promise<void> {
  return apiRequest<void>(`/pacientes/${id}`, { method: 'DELETE' });
}
