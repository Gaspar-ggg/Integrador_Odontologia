/**
 * Cliente de odontólogos.
 *
 * - `GET    /api/v1/public/odontologos`        (público)
 * - `POST   /api/v1/admin/odontologos`         (ADMIN)
 * - `PUT    /api/v1/admin/odontologos/{id}`    (ADMIN)
 * - `DELETE /api/v1/admin/odontologos/{id}`    (ADMIN, baja lógica)
 */

import type { Odontologo, OdontologoPublico, OdontologoRequest } from '../models/index.js';
import { apiRequest } from './client.js';

/** Catálogo visible para reserva de turnos (sin autenticación). */
export function listarOdontologosPublicos(): Promise<OdontologoPublico[]> {
  return apiRequest<OdontologoPublico[]>('/public/odontologos', { auth: false });
}

export function crearOdontologo(request: OdontologoRequest): Promise<Odontologo> {
  return apiRequest<Odontologo>('/admin/odontologos', {
    method: 'POST',
    body: request,
  });
}

export function modificarOdontologo(
  id: number,
  request: OdontologoRequest,
): Promise<Odontologo> {
  return apiRequest<Odontologo>(`/admin/odontologos/${id}`, {
    method: 'PUT',
    body: request,
  });
}

/** Baja lógica del odontólogo (el backend responde 204). */
export function darDeBajaOdontologo(id: number): Promise<void> {
  return apiRequest<void>(`/admin/odontologos/${id}`, { method: 'DELETE' });
}
