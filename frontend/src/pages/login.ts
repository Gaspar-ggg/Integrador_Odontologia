/**
 * Controlador de la vista `login.html`.
 *
 * Flujo:
 * 1. Valida el formulario (restricciones HTML + required).
 * 2. Autentica contra `POST /api/v1/auth/login` (persiste el JWT).
 * 3. Redirige al dashboard según el rol devuelto.
 */

import { login } from '../api/auth.js';
import { isApiError } from '../models/index.js';
import type { RolUsuario } from '../models/index.js';
import {
  bindPasswordToggles,
  describeError,
  hideAlert,
  readField,
  requireElement,
  setButtonBusy,
  showAlert,
} from '../utils/dom.js';

const DASHBOARD_URL = 'dashboard.html';

/** Destino posterior al login. Un único dashboard por ahora, listo para separar por rol. */
const REDIRECT_BY_ROLE: Record<RolUsuario, string> = {
  ADMIN: DASHBOARD_URL,
  RECEPCION: DASHBOARD_URL,
  ODONTOLOGO: DASHBOARD_URL,
  PACIENTE: DASHBOARD_URL,
};

const form = requireElement<HTMLFormElement>('login-form');
const alertBox = requireElement<HTMLDivElement>('form-alert');
const submitButton = requireElement<HTMLButtonElement>('submit-button');

function loginErrorMessage(error: unknown): string {
  if (isApiError(error)) {
    if (error.status === 401) {
      return 'Usuario o contraseña incorrectos.';
    }
    if (error.status === 403) {
      return 'La cuenta está deshabilitada. Contactá a recepción.';
    }
    return describeError(error);
  }
  return 'No pudimos conectar con el servidor. Verificá que el backend esté en ejecución.';
}

async function handleSubmit(): Promise<void> {
  hideAlert(alertBox);
  setButtonBusy(submitButton, true, 'Ingresando…');

  try {
    const data = new FormData(form);
    const auth = await login({
      username: readField(data, 'username').trim(),
      password: readField(data, 'password'),
    });

    const destination = REDIRECT_BY_ROLE[auth.rol] ?? DASHBOARD_URL;
    window.location.assign(destination);
  } catch (error) {
    showAlert(alertBox, loginErrorMessage(error), 'error');
    setButtonBusy(submitButton, false, '');
  }
}

form.addEventListener('submit', (event) => {
  event.preventDefault();
  if (!form.reportValidity()) {
    return;
  }
  void handleSubmit();
});

bindPasswordToggles();

const params = new URLSearchParams(window.location.search);
if (params.get('expired') === '1') {
  showAlert(alertBox, 'Tu sesión expiró. Ingresá nuevamente.', 'warning');
} else if (params.get('logout') === '1') {
  showAlert(alertBox, 'Cerraste sesión correctamente.', 'info');
} else if (params.get('registered') === '1') {
  const pending = params.get('pending') === '1';
  showAlert(
    alertBox,
    pending
      ? 'Tu cuenta fue creada. La ficha de paciente quedó pendiente de completar en recepción.'
      : 'Cuenta creada correctamente. Ya podés iniciar sesión.',
    pending ? 'warning' : 'success',
  );
}
