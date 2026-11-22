# Reglas de Negocio

Cada regla posee un identificador único con nomenclatura `RN-{DOMINIO}-{NN}` para facilitar su trazabilidad en el diseño y en los tests.

## Dominio: Autenticación y Cuentas (RN-AU)

- **RN-AU-01**: Las contraseñas deben almacenarse encriptadas utilizando el algoritmo BCrypt con factor de costo recomendado (>= 10).
- **RN-AU-02**: El token JWT debe contener los claims de identidad (`username`, `id`, `rol`) y una expiración no mayor a 24 horas.
- **RN-AU-03**: El nombre de usuario y el email deben ser únicos en la tabla de usuarios.
- **RN-AU-04**: Todo usuario inactivo (`activo = false`) no puede autenticarse ni generar nuevos tokens JWT.

## Dominio: Pacientes e Historias Clínicas (RN-PA)

- **RN-PA-01**: Cada paciente debe tener un número de documento (DNI) único e irrepetible.
- **RN-PA-02**: Al dar de alta un paciente en el sistema, se crea automáticamente su `HistoriaClinica` asociada.
- **RN-PA-03**: Las notas de evolución clínica son inmutables una vez asentadas: no pueden ser eliminadas físicamente; correcciones deben registrarse como una nueva evolución.
- **RN-PA-04**: Solo los usuarios con rol `ROLE_ODONTOLOGO` pueden registrar nuevas evoluciones clínicas en la historia de un paciente.
- **RN-PA-05**: La baja de un paciente aplica borrado lógico (`activo = false`); sus historias clínicas y turnos históricos se conservan para fines legales y de auditoría.

## Dominio: Turnos y Disponibilidad (RN-TU)

- **RN-TU-01 (Sin Solapamiento)**: Un odontólogo no puede tener dos turnos activos (estados `PENDIENTE`, `CONFIRMADO` o `ATENDIDO`) cuyas franjas horarias se superpongan total o parcialmente.
- **RN-TU-02 (Duración Fija)**: Los turnos se configuran en bloques fijos estándar (30 minutos o 45 minutos según la franja horaria configurada).
- **RN-TU-03 (Horario Válido)**: Un turno solo puede agendarse en fechas y horas futuras dentro del horario de atención habilitado para el profesional odontólogo.
- **RN-TU-04 (Transición de Estados)**:
  - De `PENDIENTE` puede pasar a `CONFIRMADO` o `CANCELADO`.
  - De `CONFIRMADO` puede pasar a `ATENDIDO` o `CANCELADO`.
  - Un turno `ATENDIDO` o `CANCELADO` es terminal y no puede cambiar de estado.
- **RN-TU-05 (Cancelación)**: La cancelación de un turno libera inmediatamente el bloque horario para que pueda ser reservado por otro paciente.

## Dominio: Notificaciones (RN-NT)

- **RN-NT-01**: Al confirmar la creación de un nuevo turno, el sistema debe disparar un correo electrónico de confirmación con los datos del turno (fecha, hora, profesional, dirección del consultorio).
- **RN-NT-02**: Las fallas en el envío de notificaciones (email o WhatsApp) deben ser capturadas sin abortar la transacción de reserva del turno (falla silenciosa controlada con logging de error).
- **RN-NT-03**: El envío de WhatsApp debe abstraerse tras una interfaz de servicio para permitir modo simulado (*mock logger*) en entornos de desarrollo y pruebas locales.

## Dominio: Excepciones Globales (RN-EX)

- **RN-EX-01**: La eliminación de cualquier entidad del sistema (Usuario, Paciente, Odontólogo, Turno) se implementa mediante baja lógica (*soft delete*) preservando la integridad referencial histórica.
- **RN-EX-02**: Toda excepción de negocio debe responder con un payload HTTP estructurado uniforme (código de error, mensaje descriptivo, timestamp y path del recurso).
