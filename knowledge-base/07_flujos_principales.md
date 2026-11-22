# Flujos Principales

Este documento detalla los flujos de extremo a extremo que representan los procesos centrales del sistema.

---

## Flujo 1: Autenticación y Autorización (JWT)

**Disparador**: El usuario ingresa a la aplicación web e introduce sus credenciales en la pantalla de login.  
**Actor**: Cualquier usuario (Admin, Recepción, Odontólogo, Paciente).

### Pasos:
1. El usuario completa su nombre de usuario/email y contraseña en el formulario web.
2. El frontend (TypeScript) envía una petición `POST /api/v1/auth/login` con el payload JSON.
3. El filtro de Spring Security intercepta la petición y delega en el `AuthenticationManager`.
4. Se verifica que el usuario exista, esté activo (`activo = true`) y que el hash BCrypt de la contraseña coincida.
5. Si es válido, el servicio `JwtTokenProvider` genera un token JWT firmado que incluye `sub` (username), `id` y `roles`.
6. La API responde con status `200 OK` y el payload `{ token: "...", tipo: "Bearer", rol: "..." }`.
7. El cliente almacena el token de forma segura (ej. `localStorage` o `sessionStorage`) y lo adjunta en el encabezado `Authorization: Bearer <token>` de las subsiguientes peticiones.

### Diagrama de secuencia:
```
Usuario        Frontend (TS)          Spring Security / API          Database
   |                 |                          |                       |
   |-- Ingresa cred -|                          |                       |
   |                 |-- POST /auth/login ----->|                       |
   |                 |                          |-- Busca usuario ----->|
   |                 |                          |<-- Datos + Hash ------|
   |                 |                          |-- Valida BCrypt       |
   |                 |                          |-- Genera JWT Token    |
   |                 |<-- 200 OK + JWT Token ---|                       |
   |                 |-- Almacena token local   |                       |
   |<- Redirige home |                          |                       |
```

### Casos de error:
- Credenciales inválidas → Responde `401 Unauthorized` con mensaje "Usuario o contraseña incorrectos".
- Usuario dado de baja (`activo = false`) → Responde `403 Forbidden` con mensaje "Cuenta deshabilitada".

---

## Flujo 2: Reserva y Confirmación de Turno

**Disparador**: Un paciente o recepcionista selecciona un turno disponible en la agenda.  
**Actor**: Paciente o Recepcionista.

### Pasos:
1. El usuario selecciona odontólogo y fecha en la pantalla de agenda.
2. El frontend consulta `GET /api/v1/odontologos/{id}/disponibilidad?fecha=YYYY-MM-DD`.
3. El backend calcula los bloques horarios libres contrastando el horario laboral del odontólogo contra los turnos existentes activos.
4. El usuario elige un bloque libre (ej. 10:00 a 10:45) y presiona "Confirmar Turno".
5. El frontend envía `POST /api/v1/turnos` con `{ pacienteId, odontologoId, fechaHoraInicio, fechaHoraFin, motivo }`.
6. En una transacción `@Transactional`, el backend valida que no exista solapamiento (RN-TU-01).
7. Se persiste el nuevo `Turno` en estado `PENDIENTE` o `CONFIRMADO`.
8. Se invoca de forma desacoplada el servicio de notificaciones (`NotificationService`) para disparar email y WhatsApp.
9. Se retorna status `201 Created` con el detalle del turno agendado.

### Diagrama de secuencia:
```
Actor         Frontend (TS)           TurnoController / Service           DB            NotificationService
  |                 |                              |                       |                     |
  |-- Selecciona ---|                              |                       |                     |
  |   slot horario  |-- POST /api/v1/turnos ------>|                       |                     |
  |                 |                              |-- Valida solapamiento>|                     |
  |                 |                              |<-- OK (Sin conflicto)-|                     |
  |                 |                              |-- INSERT Turno ------>|                     |
  |                 |                              |<-- Guardado ----------|                     |
  |                 |                              |-- Notificar (async) ----------------------->|
  |                 |<-- 201 Created + Turno DTO --|                                             |
  |<- Turno agendado|                                                                            |
```

### Casos de error:
- Horario ocupado concurrentemente por otro usuario → Responde `409 Conflict` ("El turno seleccionado ya no se encuentra disponible").
- Fecha u hora en el pasado → Responde `400 Bad Request` ("No se pueden agendar turnos en fechas pasadas").

---

## Flujo 3: Atención Odontológica y Carga de Evolución Clínica

**Disparador**: El paciente asiste a su consulta y el odontólogo registra el tratamiento realizado.  
**Actor**: Odontólogo (`ROLE_ODONTOLOGO`).

### Pasos:
1. El odontólogo accede a su panel de "Citas de Hoy" (`GET /api/v1/turnos?odontologoId=me&fecha=today`).
2. Selecciona al paciente y abre su ficha médica (`GET /api/v1/historias-clinicas/paciente/{id}`).
3. Revisa antecedentes y evoluciones previas registradas.
4. Tras realizar la atención clínica, redacta el diagnóstico y tratamiento en el formulario de evolución.
5. El frontend envía `POST /api/v1/historias-clinicas/paciente/{id}/evoluciones` con los datos clínicos y el `turnoId`.
6. El backend valida que el odontólogo autenticado sea quien firma la evolución, guarda la `EvolucionClinica` en la BD y actualiza el estado del turno a `ATENDIDO`.
7. Retorna `201 Created` y el frontend refresca la vista del historial clínico.

### Casos de error:
- Usuario no posee rol de odontólogo → Responde `403 Forbidden`.
- Intento de editar una evolución previa → Operación rechazada (inmutabilidad clínica: solo se permiten nuevas evoluciones).
