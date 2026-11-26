/**
 * Contratos de autenticación.
 *
 * Reflejan `LoginRequest`, `RegisterRequest`, `AuthResponse` y
 * `UserMeResponse` del backend (`/api/v1/auth`).
 */

/** Roles soportados por el sistema (RBAC). */
export const ROLES_USUARIO = ['ADMIN', 'RECEPCION', 'ODONTOLOGO', 'PACIENTE'] as const;

export type RolUsuario = (typeof ROLES_USUARIO)[number];

/** Guard de runtime para roles recibidos desde el backend o persistidos. */
export function isRolUsuario(value: string): value is RolUsuario {
  return (ROLES_USUARIO as readonly string[]).includes(value);
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  tipo: string;
  rol: RolUsuario;
}

export interface UserMeResponse {
  id: number;
  username: string;
  email: string;
  rol: RolUsuario;
  activo: boolean;
  /**
   * ID de la ficha de paciente cuando el backend decide exponerlo en `/auth/me`.
   * Hoy puede no venir; el dashboard resuelve el vínculo por otra vía o muestra
   * un estado pendiente sin romper tipos.
   */
  pacienteId?: number | null;
}
