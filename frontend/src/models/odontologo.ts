/**
 * Contratos de odontólogos.
 *
 * Reflejan `OdontologoPublicDTO`, `OdontologoResponseDTO` y
 * `OdontologoRequestDTO` del backend.
 */

/** Catálogo público (`GET /api/v1/public/odontologos`). */
export interface OdontologoPublico {
  id: number;
  nombre: string;
  apellido: string;
  matricula: string;
  especialidad: string;
}

/** Representación administrativa (`/api/v1/admin/odontologos`). */
export interface Odontologo extends OdontologoPublico {
  username: string;
  email: string;
  activo: boolean;
}

/** Payload de alta y modificación de odontólogos. */
export interface OdontologoRequest {
  nombre: string;
  apellido: string;
  matricula: string;
  especialidad: string;
  username: string;
  email: string;
  password: string;
}
