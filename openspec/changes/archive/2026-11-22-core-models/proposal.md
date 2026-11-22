# Proposal

## Why

El sistema requiere una base sólida de persistencia para la gestión de identidades y usuarios con control de roles (RBAC), auditoría con baja lógica (soft delete) y un estándar uniforme de respuestas de error. Sin estas estructuras core, no es posible implementar la autenticación JWT (C-03) ni vincular profesionales y pacientes a sus respectivas cuentas de usuario.

## What Changes

- Creación de la clase base abstracta `AuditableEntity` con `@MappedSuperclass`, conteniendo marcas de tiempo de auditoría (`fechaCreacion`, `fechaModificacion`) y control de estado lógico (`activo`).
- Creación de la entidad JPA `Usuario` con campos `username` único, `email` único, `passwordHash`, rol enumerado (`ADMIN`, `RECEPCION`, `ODONTOLOGO`, `PACIENTE`), estado activo y fecha de creación.
- Definición del repositorio `UsuarioRepository` con métodos de consulta y existencia por username y email.
- Implementación de `GlobalExceptionHandler` con `@RestControllerAdvice` respondiendo la estructura estándar `ErrorResponseDTO` (timestamp, status, error, message, path) ante excepciones de validación, recursos no encontrados y conflictos de datos.
- Creación del componente `DataInitializer` para sembrar los usuarios iniciales del sistema (`admin` y `recepcion`) con contraseñas cifradas en BCrypt al iniciar la aplicación.
- Pruebas automatizadas de persistencia para `UsuarioRepository` y pruebas de integración para `GlobalExceptionHandler` sobre base de datos H2 en memoria.

## Capabilities

### New Capabilities

- `user-identity`: Gestión de cuentas de usuario, persistencia de credenciales, roles del sistema (RBAC), auditoría con soft delete y manejo centralizado de excepciones operativas.

### Modified Capabilities

*(Ninguna)*

## Impact

- **Modelos y Persistencia**: Se incorporan las tablas relacionales base (`usuarios`) en PostgreSQL / H2.
- **Seguridad**: Se establece la estructura de contraseñas hasheadas (BCrypt) para la futura autenticación JWT.
- **Controladores y APIs**: Respuestas de error estandarizadas en formato JSON uniforme para toda la aplicación.
- **Seed inicial**: Usuarios operativos precargados para permitir pruebas locales inmediatas.
