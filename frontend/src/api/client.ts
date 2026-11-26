/**
 * Cliente HTTP base para consumir la API REST.
 *
 * Centraliza:
 * - Prefijo `/api/v1`.
 * - Serialización de query params (ignora `undefined`, `null` y strings vacíos).
 * - Header `Authorization: Bearer <token>` a partir de `utils/jwt`.
 * - Parseo JSON y mapeo del `ApiError` estándar del backend.
 */

import type { ApiError } from '../models/index.js';
import { getAuthorizationHeader } from '../utils/jwt.js';

export const API_BASE_URL = '/api/v1';

export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';

export type QueryValue = string | number | boolean | null | undefined;

export interface ApiRequestOptions {
  method?: HttpMethod;
  query?: Record<string, QueryValue>;
  body?: unknown;
  /**
   * Adjunta el token Bearer. Por defecto `true`.
   * Usar `false` en endpoints públicos (login, registro, catálogo).
   */
  auth?: boolean;
}

function buildUrl(path: string, query?: Record<string, QueryValue>): string {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`;
  const url = `${API_BASE_URL}${normalizedPath}`;

  if (!query) {
    return url;
  }

  const params = new URLSearchParams();
  for (const [key, value] of Object.entries(query)) {
    if (value === undefined || value === null || value === '') {
      continue;
    }
    params.append(key, String(value));
  }

  const queryString = params.toString();
  return queryString.length > 0 ? `${url}?${queryString}` : url;
}

async function parseApiError(response: Response): Promise<ApiError> {
  const fallback: ApiError = {
    timestamp: new Date().toISOString(),
    status: response.status,
    error: response.statusText || 'Error HTTP',
    message: `La solicitud falló con estado ${response.status}`,
    path: response.url,
  };

  try {
    const body = (await response.json()) as Partial<ApiError>;
    return {
      timestamp: body.timestamp ?? fallback.timestamp,
      status: body.status ?? fallback.status,
      error: body.error ?? fallback.error,
      message: body.message ?? fallback.message,
      path: body.path ?? fallback.path,
      ...(body.details ? { details: body.details } : {}),
    };
  } catch {
    return fallback;
  }
}

/**
 * Ejecuta una request y devuelve el cuerpo tipado.
 * En errores HTTP lanza el `ApiError` normalizado del backend.
 */
export async function apiRequest<T>(path: string, options: ApiRequestOptions = {}): Promise<T> {
  const { method = 'GET', query, body, auth = true } = options;

  const headers: Record<string, string> = { Accept: 'application/json' };
  if (body !== undefined) {
    headers['Content-Type'] = 'application/json';
  }
  if (auth) {
    Object.assign(headers, getAuthorizationHeader());
  }

  const response = await fetch(buildUrl(path, query), {
    method,
    headers,
    ...(body !== undefined ? { body: JSON.stringify(body) } : {}),
  });

  if (!response.ok) {
    throw await parseApiError(response);
  }

  if (response.status === 204) {
    return undefined as unknown as T;
  }

  const text = await response.text();
  if (text.length === 0) {
    return undefined as unknown as T;
  }

  return JSON.parse(text) as T;
}
