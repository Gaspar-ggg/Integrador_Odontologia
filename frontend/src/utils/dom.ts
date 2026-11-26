/**
 * Utilidades de DOM compartidas por las páginas de autenticación.
 * Sin dependencias externas y con tipado estricto.
 */

import { isApiError } from '../models/index.js';

export type AlertVariant = 'error' | 'success' | 'warning' | 'info';

/** Obtiene un elemento por id o falla ruidosamente si la vista no lo incluye. */
export function requireElement<T extends HTMLElement>(id: string): T {
  const element = document.getElementById(id);
  if (!element) {
    throw new Error(`No se encontró el elemento requerido: #${id}`);
  }
  return element as T;
}

/** Lee un campo de FormData sin recortar (útil para contraseñas). */
export function readField(data: FormData, name: string): string {
  const value = data.get(name);
  return typeof value === 'string' ? value : '';
}

export function showAlert(element: HTMLElement, message: string, variant: AlertVariant): void {
  element.textContent = message;
  element.className = `alert alert--${variant}`;
  element.hidden = false;
}

export function hideAlert(element: HTMLElement): void {
  element.hidden = true;
  element.textContent = '';
}

/**
 * Bloquea/desbloquea un botón de envío conservando su etiqueta original.
 * `busyLabel` vacío mantiene el texto actual.
 */
export function setButtonBusy(
  button: HTMLButtonElement,
  busy: boolean,
  busyLabel: string,
): void {
  if (busy) {
    if (button.dataset.idleLabel === undefined) {
      button.dataset.idleLabel = button.textContent ?? '';
    }
    button.disabled = true;
    button.setAttribute('aria-busy', 'true');
    if (busyLabel.length > 0) {
      button.textContent = busyLabel;
    }
    return;
  }

  button.disabled = false;
  button.removeAttribute('aria-busy');
  if (button.dataset.idleLabel !== undefined) {
    button.textContent = button.dataset.idleLabel;
  }
}

/** Traduce un error de la API a un mensaje legible para el usuario. */
export function describeError(error: unknown): string {
  if (isApiError(error)) {
    if (error.details) {
      const firstDetail = Object.values(error.details)[0];
      if (firstDetail) {
        return firstDetail;
      }
    }
    return error.message;
  }
  if (error instanceof Error && error.message.length > 0) {
    return error.message;
  }
  return 'Ocurrió un error inesperado. Intentá nuevamente.';
}

/** Conecta todos los botones `[data-toggle-password]` con su input asociado. */
export function bindPasswordToggles(root: ParentNode = document): void {
  const buttons = root.querySelectorAll<HTMLButtonElement>('[data-toggle-password]');
  buttons.forEach((button) => {
    const targetId = button.dataset.togglePassword;
    if (!targetId) {
      return;
    }
    const input = document.getElementById(targetId);
    if (!(input instanceof HTMLInputElement)) {
      return;
    }

    button.addEventListener('click', () => {
      const shouldShow = input.type === 'password';
      input.type = shouldShow ? 'text' : 'password';
      button.textContent = shouldShow ? 'Ocultar' : 'Mostrar';
      button.setAttribute('aria-pressed', String(shouldShow));
      button.setAttribute(
        'aria-label',
        shouldShow ? 'Ocultar contraseña' : 'Mostrar contraseña',
      );
    });
  });
}
