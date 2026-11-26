# Design: C-07 agenda-web-frontend

## Architecture Overview

El frontend está estructurado como una Single Page / Multi Page Application híbrida liviana construida con TypeScript vanilla modular (ES Modules) y Vite/TSC, sin sobrecarga de frameworks pesados, garantizando máxima velocidad, auditabilidad y portabilidad.

```
frontend/
├── public/
│   ├── index.html          # Landing y portal de bienvenida
│   ├── login.html          # Vista de inicio de sesión
│   ├── registro.html       # Vista de registro de pacientes
│   ├── dashboard.html      # Panel principal de gestión y reserva de turnos
│   └── styles/
│       ├── auth.css        # Estilos para login/registro con tokens
│       └── dashboard.css   # Estilos para agenda, calendario y modales
└── src/
    ├── api/                # Clientes HTTP hacia backend Spring Boot
    │   ├── client.ts       # fetch con interceptor de auth y ApiError
    │   ├── auth.ts         # /api/v1/auth
    │   ├── odontologos.ts  # /api/v1/odontologos
    │   ├── pacientes.ts    # /api/v1/pacientes
    │   └── turnos.ts       # /api/v1/turnos
    ├── models/             # Interfaces TypeScript espejo de DTOs backend
    ├── pages/              # Controladores de vista con ciclo de vida
    └── utils/              # Manipulación segura de DOM y JWT storage
```

## Key Decisions

1. **Uso estricto de Tokens de Diseño**: Todos los componentes visuales respetan las variables CSS de `design-system/tokens.css` (colores primarios, secundarios, estados, tipografía y radios).
2. **Cliente API tipado y seguro**: `apiRequest<T>()` en `client.ts` añade automáticamente el header `Authorization: Bearer <token>` si `auth: true` (por defecto) y parsea el cuerpo de error unificado del backend (`GlobalExceptionHandler`).
3. **Control de Sesión y RBAC en cliente**: `dashboard.ts` invoca `/auth/me` al cargar. Si el token expiró o es inválido, redirige a `login.html`. Si el usuario tiene rol `ADMIN` o `RECEPCION`, permite buscar pacientes para asignar turnos a terceros; si es `PACIENTE`, auto-vincula su propio ID.
4. **Reserva Reactiva**: Al seleccionar profesional y fecha, se consultan los slots en tiempo real. Los slots ocupados o pasados quedan deshabilitados visualmente.
5. **Modal de Confirmación**: Antes de realizar la mutación `POST /turnos`, se abre un diálogo modal accesible para ingresar motivo de consulta y validar fecha/hora.
