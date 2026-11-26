/**
 * Controlador de la vista `registro.html`.
 *
 * Flujo (US-002):
 * 1. Crea el usuario con `POST /api/v1/auth/register` (rol PACIENTE).
 * 2. Inicia sesión para obtener el JWT.
 * 3. Crea la ficha de paciente con `POST /api/v1/pacientes`.
 *
 * Nota de integración: el backend actual restringe el alta de pacientes a
 * ADMIN/RECEPCION. Si el paso 3 devuelve 403, la cuenta queda creada y se
 * informa que la ficha debe completarse en recepción (sin tocar backend).
 */

import { login, register } from '../api/auth.js';
import { crearPaciente } from '../api/pacientes.js';
import { isApiError } from '../models/index.js';
import { removeToken } from '../utils/jwt.js';
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
const LOGIN_PENDING_URL = 'login.html?registered=1&pending=1';

const form = requireElement<HTMLFormElement>('registro-form');
const alertBox = requireElement<HTMLDivElement>('form-alert');
const submitButton = requireElement<HTMLButtonElement>('submit-button');
const passwordInput = requireElement<HTMLInputElement>('password');
const confirmPasswordInput = requireElement<HTMLInputElement>('confirmPassword');
const fechaNacimientoInput = requireElement<HTMLInputElement>('fechaNacimiento');

function toIsoDate(date: Date): string {
  const year = String(date.getFullYear());
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

fechaNacimientoInput.max = toIsoDate(new Date());

function clearPasswordMismatch(): void {
  confirmPasswordInput.setCustomValidity('');
}

passwordInput.addEventListener('input', clearPasswordMismatch);
confirmPasswordInput.addEventListener('input', clearPasswordMismatch);

function redirectLater(url: string, delayMs: number): void {
  window.setTimeout(() => {
    window.location.assign(url);
  }, delayMs);
}

function connectionMessage(): string {
  return 'No pudimos conectar con el servidor. Verificá que el backend esté en ejecución.';
}

async function handleSubmit(): Promise<void> {
  hideAlert(alertBox);

  const data = new FormData(form);
  const password = readField(data, 'password');
  const confirmPassword = readField(data, 'confirmPassword');

  if (password !== confirmPassword) {
    confirmPasswordInput.setCustomValidity('Las contraseñas no coinciden.');
    confirmPasswordInput.reportValidity();
    showAlert(alertBox, 'Las contraseñas no coinciden.', 'error');
    return;
  }
  clearPasswordMismatch();

  const username = readField(data, 'username').trim();
  const email = readField(data, 'email').trim();
  const dni = readField(data, 'dni').trim();
  const nombre = readField(data, 'nombre').trim();
  const apellido = readField(data, 'apellido').trim();
  const fechaNacimiento = readField(data, 'fechaNacimiento');
  const telefono = readField(data, 'telefono').trim();
  const obraSocial = readField(data, 'obraSocial').trim();

  setButtonBusy(submitButton, true, 'Creando cuenta…');

  try {
    const usuario = await register({ username, email, password });
    await login({ username, password });

    try {
      await crearPaciente({
        usuarioId: usuario.id,
        dni,
        nombre,
        apellido,
        fechaNacimiento,
        telefono,
        email,
        ...(obraSocial.length > 0 ? { obraSocial } : {}),
      });
    } catch (patientError) {
      removeToken();
      const message =
        isApiError(patientError) && patientError.status === 403
          ? 'Tu cuenta fue creada, pero la ficha de paciente debe completarla recepción. Te llevamos al inicio de sesión.'
          : `Tu cuenta fue creada, pero no pudimos registrar la ficha de paciente: ${
              isApiError(patientError)
                ? describeError(patientError)
                : 'no hubo respuesta del servidor'
            }.`;
      showAlert(alertBox, message, 'warning');
      setButtonBusy(submitButton, true, 'Redirigiendo…');
      redirectLater(LOGIN_PENDING_URL, 3500);
      return;
    }

    window.location.assign(DASHBOARD_URL);
  } catch (error) {
    setButtonBusy(submitButton, false, '');

    if (!isApiError(error)) {
      showAlert(alertBox, connectionMessage(), 'error');
      return;
    }

    if (error.status === 409) {
      showAlert(
        alertBox,
        'El nombre de usuario o el email ya están registrados. Probá iniciar sesión.',
        'error',
      );
      return;
    }

    if (error.status === 401) {
      showAlert(
        alertBox,
        'La cuenta se creó, pero no pudimos iniciar sesión. Probá ingresando manualmente.',
        'warning',
      );
      return;
    }

    showAlert(alertBox, describeError(error), 'error');
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
