/**
 * Cliente de autenticación.
 *
 * - `POST /api/v1/auth/login`
 * - `POST /api/v1/auth/register`
 * - `GET  /api/v1/auth/me`
 *
 * Los contratos viven en `models/auth.ts`; se re-exportan para no romper
 * imports existentes (`import { AuthResponse } from '../api/auth.js'`).
 */

import type {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  UserMeResponse,
} from '../models/index.js';
import { saveToken } from '../utils/jwt.js';
import { apiRequest } from './client.js';

export type { AuthResponse, LoginRequest, RegisterRequest, UserMeResponse };

/** Autentica al usuario y persiste el JWT en el almacenamiento local. */
export async function login(request: LoginRequest): Promise<AuthResponse> {
  const data = await apiRequest<AuthResponse>('/auth/login', {
    method: 'POST',
    body: request,
    auth: false,
  });
  saveToken(data.token);
  return data;
}

/** Registra un usuario nuevo (rol PACIENTE por defecto en el backend). */
export function register(request: RegisterRequest): Promise<UserMeResponse> {
  return apiRequest<UserMeResponse>('/auth/register', {
    method: 'POST',
    body: request,
    auth: false,
  });
}

/** Devuelve el usuario autenticado según el token Bearer actual. */
export function me(): Promise<UserMeResponse> {
  return apiRequest<UserMeResponse>('/auth/me');
}
