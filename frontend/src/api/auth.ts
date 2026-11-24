import { getAuthorizationHeader, saveToken } from '../utils/jwt.js';

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
  rol: string;
}

export interface UserMeResponse {
  id: number;
  username: string;
  email: string;
  rol: string;
  activo: boolean;
}

export interface ApiError {
  status: number;
  error: string;
  message: string;
  path: string;
  timestamp: string;
  details?: Record<string, string>;
}

const API_BASE = '/api/v1/auth';

async function parseJsonResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const error = (await response.json()) as ApiError;
    throw error;
  }
  return (await response.json()) as T;
}

export async function login(request: LoginRequest): Promise<AuthResponse> {
  const response = await fetch(`${API_BASE}/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  });

  const data = await parseJsonResponse<AuthResponse>(response);
  saveToken(data.token);
  return data;
}

export async function register(request: RegisterRequest): Promise<UserMeResponse> {
  const response = await fetch(`${API_BASE}/register`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  });

  return parseJsonResponse<UserMeResponse>(response);
}

export async function me(): Promise<UserMeResponse> {
  const response = await fetch(`${API_BASE}/me`, {
    method: 'GET',
    headers: {
      ...getAuthorizationHeader(),
    },
  });

  return parseJsonResponse<UserMeResponse>(response);
}
