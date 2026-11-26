# Tasks: C-07 agenda-web-frontend

- [x] 1. Definir interfaces y modelos TypeScript espejo del backend (`models/auth.ts`, `models/turno.ts`, `models/odontologo.ts`, `models/paciente.ts`, `models/api.ts`).
- [x] 2. Implementar cliente HTTP base tipado (`src/api/client.ts`) con gestión de Bearer token y parseo de `ApiError`.
- [x] 3. Implementar clientes de servicio para cada recurso: `api/auth.ts`, `api/odontologos.ts`, `api/pacientes.ts`, `api/turnos.ts`.
- [x] 4. Diseñar plantillas HTML accesibles y responsivas (`public/login.html`, `public/registro.html`, `public/dashboard.html`) utilizando variables del design system.
- [x] 5. Desarrollar hojas de estilo CSS (`styles/auth.css`, `styles/dashboard.css`) consumiendo `tokens.css`.
- [x] 6. Desarrollar controladores de autenticación (`src/pages/login.ts`, `src/pages/registro.ts`) con validación y redirección según rol.
- [x] 7. Desarrollar controlador de panel principal (`src/pages/dashboard.ts`) con control de sesión `/auth/me`, selector de odontólogos, calendario interactivo de disponibilidad, modal de reserva y listado de citas con cancelación.
- [x] 8. Validar compilación limpia con `npm run build` (`tsc`) sin errores de tipos.
