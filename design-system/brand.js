/**
 * La Muelita Cuyana - Brand definition & lightweight DOM binding
 */
export const BRAND = {
  name: "La Muelita Cuyana",
  tagline: "Consultorio y Gestión Odontológica",
  shortName: "La Muelita Cuyana",
  isProvisional: false
};

// Auto-bind brand name to any DOM element with [data-brand-name]
if (typeof document !== "undefined") {
  document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll("[data-brand-name]").forEach((el) => {
      el.textContent = BRAND.name;
    });
    document.querySelectorAll("[data-brand-tagline]").forEach((el) => {
      el.textContent = BRAND.tagline;
    });
  });
}
