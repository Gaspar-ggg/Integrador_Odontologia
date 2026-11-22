# Funcionalidades

Las funcionalidades están organizadas por épica y detalladas en historias de usuario (*User Stories*) bajo el estándar `US-{NNN}`.

---

## Épica 1: Autenticación y Perfiles

### US-001 — Inicio de Sesión y Autenticación con JWT
**Como** usuario del sistema (Admin, Recepcionista, Odontólogo o Paciente)  
**Quiero** ingresar con mis credenciales (usuario/email y contraseña)  
**Para** acceder a las pantallas y recursos correspondientes a mi rol.

**Criterios de aceptación**:
- [ ] Retorna un token JWT válido ante credenciales correctas.
- [ ] Responde `401 Unauthorized` ante credenciales inválidas.
- [ ] Bloquea el acceso a usuarios inactivos (`activo = false`).

**Reglas relacionadas**: RN-AU-01, RN-AU-02, RN-AU-04.

---

### US-002 — Registro Autónomo de Pacientes
**Como** paciente nuevo  
**Quiero** registrarme ingresando mis datos personales y credenciales  
**Para** poder reservar turnos online sin tener que llamar por teléfono.

**Criterios de aceptación**:
- [ ] Valida unicidad de DNI, email y nombre de usuario.
- [ ] Crea automáticamente la ficha de paciente y su historia clínica inicial.
- [ ] Asigna por defecto el rol `ROLE_PACIENTE`.

**Reglas relacionadas**: RN-AU-03, RN-PA-01, RN-PA-02.

---

## Épica 2: Gestión de Pacientes

### US-003 — Alta y Búsqueda de Pacientes en Mesa de Entrada
**Como** recepcionista  
**Quiero** dar de alta y buscar pacientes por DNI, apellido o nombre  
**Para** agendar citas o consultar sus datos rápidamente.

**Criterios de aceptación**:
- [ ] Formulario de búsqueda con filtrado en tiempo real o por campo clave.
- [ ] Capacidad de registrar pacientes que no tienen cuenta de usuario online.
- [ ] Visualización de ficha con datos de contacto actualizados.

**Reglas relacionadas**: RN-PA-01, RN-PA-05.

---

## Épica 3: Agenda y Gestión de Turnos

### US-004 — Consulta de Disponibilidad de Odontólogos
**Como** paciente o recepcionista  
**Quiero** ver los días y bloques horarios disponibles de un odontólogo  
**Para** seleccionar un turno que no colisione con citas existentes.

**Criterios de aceptación**:
- [ ] Muestra únicamente bloques horarios libres según la duración estándar (ej. 30 o 45 min).
- [ ] Excluye turnos ocupados (`PENDIENTE`, `CONFIRMADO` o `ATENDIDO`).
- [ ] Impide seleccionar fechas u horas pasadas.

**Reglas relacionadas**: RN-TU-01, RN-TU-02, RN-TU-03.

---

### US-005 — Agendamiento de Turno
**Como** paciente o recepcionista  
**Quiero** confirmar la reserva de un turno para una fecha y horario disponible  
**Para** garantizar la atención del paciente con el profesional deseado.

**Criterios de aceptación**:
- [ ] Verifica a nivel transaccional que el bloque horario siga disponible al momento de guardar.
- [ ] Asocia el paciente y odontólogo al turno en estado `PENDIENTE` o `CONFIRMADO`.
- [ ] Dispara la notificación de confirmación al paciente.

**Reglas relacionadas**: RN-TU-01, RN-NT-01, RN-NT-02.

---

### US-006 — Cancelación y Reprogramación de Turnos
**Como** paciente o recepcionista  
**Quiero** cancelar un turno agendado  
**Para** liberar el bloque horario en la agenda del profesional.

**Criterios de aceptación**:
- [ ] Transición del estado del turno a `CANCELADO`.
- [ ] El horario cancelado queda inmediatamente disponible para nuevas reservas.
- [ ] Un turno previamente atendido no puede cancelarse.

**Reglas relacionadas**: RN-TU-04, RN-TU-05.

---

## Épica 4: Atención Clínica e Historia Médica

### US-007 — Registro de Evolución Odontológica
**Como** odontólogo  
**Quiero** redactar las notas de evolución, diagnóstico y tratamiento realizado tras atender un turno  
**Para** conservar el historial clínico y la trazabilidad del paciente.

**Criterios de aceptación**:
- [ ] Solo accesible por el profesional logueado con rol `ROLE_ODONTOLOGO`.
- [ ] Registra fecha, diagnóstico, tratamiento y observaciones vinculadas a la historia del paciente.
- [ ] Permite marcar el turno correspondiente como `ATENDIDO`.

**Reglas relacionadas**: RN-PA-03, RN-PA-04, RN-TU-04.

---

## Épica 5: Notificaciones y Recordatorios

### US-008 — Envío de Notificaciones por Email y Mensajería
**Como** paciente  
**Quiero** recibir un mensaje de confirmación por email y WhatsApp cuando mi turno es creado  
**Para** tener presente la fecha, hora y datos del profesional que me atenderá.

**Criterios de aceptación**:
- [ ] Disparo asíncrono o desacoplado tras la creación exitosa del turno.
- [ ] Contenido claro con fecha, hora, nombre del profesional y consultorio.
- [ ] Si el servicio de mensajería falla, el agendamiento del turno no se revierte.

**Reglas relacionadas**: RN-NT-01, RN-NT-02, RN-NT-03.
