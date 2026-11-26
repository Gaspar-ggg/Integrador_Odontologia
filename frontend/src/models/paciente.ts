/**
 * Contratos de pacientes.
 *
 * Reflejan `PacienteResponseDTO` y `PacienteRequestDTO` del backend.
 * Las fechas (`fechaNacimiento`) viajan como ISO `YYYY-MM-DD`.
 */

export interface Paciente {
  id: number;
  usuarioId: number | null;
  dni: string;
  nombre: string;
  apellido: string;
  fechaNacimiento: string;
  telefono: string;
  email: string | null;
  obraSocial: string | null;
  activo: boolean;
}

/** Payload de alta de pacientes (`POST /api/v1/pacientes`). */
export interface PacienteRequest {
  usuarioId?: number | null;
  dni: string;
  nombre: string;
  apellido: string;
  fechaNacimiento: string;
  telefono: string;
  email?: string;
  obraSocial?: string;
}

/** Filtros de búsqueda aguda (`GET /api/v1/pacientes/buscar`). */
export interface PacienteFiltros {
  dni?: string;
  q?: string;
}
