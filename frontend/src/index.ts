/**
 * Entrada principal de la aplicación cliente (TypeScript Vanilla)
 */

console.log("[App] Integrador Odontología - Frontend inicializado.");

export function initApp(): void {
  const rootElement = document.getElementById("app");
  if (rootElement) {
    console.log("[App] Montaje de componentes listo.");
  }
}

document.addEventListener("DOMContentLoaded", () => {
  initApp();
});
