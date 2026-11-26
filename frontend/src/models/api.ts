/**
 * Contratos transversales de la API REST.
 *
 * Reflejan `com.odontologia.dto.ErrorResponseDTO` del backend.
 */

/** Mapa campo -> mensaje de validación (errores de Bean Validation). */
export type ApiValidationDetails = Record<string, string>;

/**
 * Estructura estándar de error devuelta por `@RestControllerAdvice`.
 * `details` solo está presente en errores de validación.
 */
export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  details?: ApiValidationDetails;
}

/** Guard de runtime para discriminar un `ApiError` de cualquier excepción. */
export function isApiError(value: unknown): value is ApiError {
  if (typeof value !== 'object' || value === null) {
    return false;
  }
  const candidate = value as Partial<ApiError>;
  return typeof candidate.status === 'number' && typeof candidate.message === 'string';
}
