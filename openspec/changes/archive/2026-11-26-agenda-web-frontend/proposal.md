# Proposal: C-07 agenda-web-frontend

## Why

Con los servicios de autenticación JWT (C-03), profesionales (C-04), pacientes (C-05) y la agenda backend con cálculo dinámico de disponibilidad y bloqueo anti-solapamiento (C-06) ya operativos y probados, el sistema requería una interfaz de usuario web responsiva y accesible que permitiera a los pacientes y recepcionistas interactuar de forma intuitiva con la clínica. Este change completa el MVP entregando las vistas de autenticación, el dashboard con control de sesión por roles y la reserva interactiva de turnos.

## What Changes

- Implementar vistas HTML responsivas y semánticas en `frontend/public/`: `login.html`, `registro.html`, `dashboard.html` e `index.html`.
- Consumir el sistema de diseño centralizado aplicando `design-system/tokens.css` y `design-system/brand.js` con soporte de diseño adaptativo y paleta médica.
- Crear el cliente HTTP centralizado en TypeScript (`frontend/src/api/client.ts`) con gestión automática de prefijo `/api/v1`, headers `Authorization: Bearer <token>` y manejo de errores `ApiError`.
- Desarrollar submódulos de API: `auth.ts`, `odontologos.ts`, `pacientes.ts`, `turnos.ts`.
- Modelar en TypeScript las entidades y contratos de la API en `frontend/src/models/` (`auth.ts`, `odontologo.ts`, `paciente.ts`, `turno.ts`, `api.ts`).
- Desarrollar la lógica de interacción de páginas en `frontend/src/pages/`:
  - `login.ts`: autenticación con redirección por rol y feedback visual.
  - `registro.ts`: registro de nuevos usuarios pacientes con validación en tiempo real.
  - `dashboard.ts`: control de sesión contra `/auth/me`, selector dinámico de profesional y fecha, renderizado de slots disponibles, modal de confirmación de reserva y gestión de cancelación de turnos.
- Utilitarios DOM (`dom.ts` y `jwt.ts`) para manipulación accesible y segura.

## Capabilities

### New Capabilities
- `web-ui`: Interfaz web para autenticación, navegación por rol, visualización de agenda, reserva de turnos en tiempo real y cancelación de citas.

### Modified Capabilities
- Ninguna del backend. Se consumen los endpoints existentes sin alterar sus contratos.

## Impact
- **Frontend**: Nuevo código modular en TypeScript bajo `frontend/src/` y páginas HTML/CSS en `frontend/public/`.
- **Compilación**: Compilación limpia mediante `npm run build` (`tsc`).
