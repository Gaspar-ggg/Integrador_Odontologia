# Spec: web-ui (Frontend Web Interface)

## Purpose
Proveer una interfaz de usuario web ágil, accesible y segura para la autenticación de usuarios, consulta de disponibilidad de profesionales de odontología y reserva o cancelación de turnos.

## Requirements

### Requirement: Autenticación y Control de Sesión
El sistema debe permitir a los usuarios iniciar sesión con email y contraseña, guardar el JWT en almacenamiento local y validar la sesión contra `/api/v1/auth/me`.
- **Escenario Login exitoso**: Si las credenciales son válidas, almacena el token y redirige a `dashboard.html`.
- **Escenario Sesión inválida**: Si se ingresa a `dashboard.html` sin token o con token expirado, redirige a `login.html`.
- **Escenario Cierre de sesión**: Al hacer clic en salir, remueve el token y redirige a `login.html`.

### Requirement: Reserva Dinámica de Turnos
El sistema debe permitir seleccionar un odontólogo del catálogo y una fecha, consultando y renderizando los horarios disponibles en tiempo real.
- **Escenario Disponibilidad**: Presenta los slots horarios libres calculados por el backend.
- **Escenario Confirmación**: Muestra modal con fecha, hora y odontólogo seleccionado, permitiendo ingresar motivo y enviando la solicitud al backend.

### Requirement: Gestión de Citas Asignadas
El sistema debe listar las citas del paciente autenticado y ofrecer la opción de cancelarlas de forma inmediata con confirmación en pantalla.
