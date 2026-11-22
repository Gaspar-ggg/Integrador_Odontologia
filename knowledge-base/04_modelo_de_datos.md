# Modelo de Datos

## Dominios

1. **Usuarios y Seguridad**: Cuentas, roles (`Role`), credenciales encriptadas y autenticación JWT.
2. **Pacientes**: Información demográfica, de contacto y ficha médica general.
3. **Profesionales**: Odontólogos, especialidades, matrículas y horarios de atención.
4. **Turnos y Agenda**: Intervalos temporales, asignación paciente-odontólogo y estados de la cita.
5. **Historial Clínico**: Ficha histórica de cada paciente con notas de evolución fechadas y firmadas por el odontólogo.

## ERD (Entity Relationship Diagram)

```
       +-------------------+                  +---------------------+
       |      Usuario      | 1              1 |     Odontologo      |
       |-------------------|------------------|---------------------|
       | id (PK)           |                  | id (PK)             |
       | username          |                  | usuario_id (FK)     |
       | password_hash     |                  | matricula           |
       | rol (ENUM)        |                  | especialidad        |
       | activo (BOOLEAN)  |                  | activo (BOOLEAN)    |
       +-------------------+                  +---------------------+
                 | 1                                     | 1
                 |                                       |
                 | 1                                     |
       +-------------------+                             |
       |     Paciente      | 1                           |
       |-------------------|                             |
       | id (PK)           |                             |
       | usuario_id (FK)   |                             |
       | dni (UNIQUE)      |                             |
       | nombre, apellido  |                             |
       | telefono, email   |                             |
       | activo (BOOLEAN)  |                             |
       +-------------------+                             |
          | 1             | 1                            |
          |               |                              |
          |               | N                            | N
          |        +----------------------------------------+
          |        |                 Turno                  |
          |        |----------------------------------------|
          |        | id (PK)                                |
          |        | paciente_id (FK)                       |
          |        | odontologo_id (FK)                     |
          |        | fecha_hora_inicio (TIMESTAMP)          |
          |        | fecha_hora_fin (TIMESTAMP)             |
          |        | estado (ENUM)                          |
          |        | motivo_consulta                        |
          |        | activo (BOOLEAN)                       |
          |        +----------------------------------------+
          |                                              | 0..1
          | 1                                            |
       +-----------------------+                         |
       |    HistoriaClinica    | 1                       |
       |-----------------------|                         |
       | id (PK)               |                         |
       | paciente_id (FK)      |                         |
       | fecha_creacion        |                         |
       +-----------------------+                         |
                 | 1                                     |
                 | N                                     |
       +------------------------------------+            |
       |          EvolucionClinica          |            |
       |------------------------------------|            |
       | id (PK)                            |            |
       | historia_clinica_id (FK)           |            |
       | odontologo_id (FK)                 |            |
       | turno_id (FK, Opcional) -----------+------------+
       | fecha_hora (TIMESTAMP)             |
       | diagnostico                        |
       | tratamiento_realizado              |
       | observaciones                      |
       +------------------------------------+
```

## Entidades

### `Usuario`
- `id`: BIGSERIAL (PK)
- `username`: VARCHAR(50) NOT NULL UNIQUE
- `password_hash`: VARCHAR(255) NOT NULL (BCrypt)
- `email`: VARCHAR(100) NOT NULL UNIQUE
- `rol`: VARCHAR(20) NOT NULL (ADMIN, RECEPCION, ODONTOLOGO, PACIENTE)
- `activo`: BOOLEAN NOT NULL DEFAULT TRUE (Soft delete)
- `fecha_creacion`: TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

### `Odontologo`
- `id`: BIGSERIAL (PK)
- `usuario_id`: BIGINT NOT NULL UNIQUE (FK -> Usuario.id)
- `nombre`: VARCHAR(100) NOT NULL
- `apellido`: VARCHAR(100) NOT NULL
- `matricula`: VARCHAR(50) NOT NULL UNIQUE
- `especialidad`: VARCHAR(100) NOT NULL
- `activo`: BOOLEAN NOT NULL DEFAULT TRUE

### `Paciente`
- `id`: BIGSERIAL (PK)
- `usuario_id`: BIGINT UNIQUE (FK -> Usuario.id, opcional para pacientes sin cuenta web)
- `dni`: VARCHAR(20) NOT NULL UNIQUE
- `nombre`: VARCHAR(100) NOT NULL
- `apellido`: VARCHAR(100) NOT NULL
- `fecha_nacimiento`: DATE
- `telefono`: VARCHAR(30) NOT NULL
- `email`: VARCHAR(100)
- `obra_social`: VARCHAR(100)
- `activo`: BOOLEAN NOT NULL DEFAULT TRUE

### `Turno`
- `id`: BIGSERIAL (PK)
- `paciente_id`: BIGINT NOT NULL (FK -> Paciente.id)
- `odontologo_id`: BIGINT NOT NULL (FK -> Odontologo.id)
- `fecha_hora_inicio`: TIMESTAMP NOT NULL
- `fecha_hora_fin`: TIMESTAMP NOT NULL
- `estado`: VARCHAR(20) NOT NULL (PENDIENTE, CONFIRMADO, ATENDIDO, CANCELADO)
- `motivo_consulta`: TEXT
- `activo`: BOOLEAN NOT NULL DEFAULT TRUE
- *Constraints*: No solapamiento: `INDEX idx_odontologo_fecha (odontologo_id, fecha_hora_inicio, fecha_hora_fin)` donde `estado != 'CANCELADO' AND activo = true`.

### `HistoriaClinica`
- `id`: BIGSERIAL (PK)
- `paciente_id`: BIGINT NOT NULL UNIQUE (FK -> Paciente.id)
- `fecha_creacion`: TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
- `observaciones_generales`: TEXT

### `EvolucionClinica`
- `id`: BIGSERIAL (PK)
- `historia_clinica_id`: BIGINT NOT NULL (FK -> HistoriaClinica.id)
- `odontologo_id`: BIGINT NOT NULL (FK -> Odontologo.id)
- `turno_id`: BIGINT (FK -> Turno.id, opcional)
- `fecha_hora`: TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
- `diagnostico`: TEXT NOT NULL
- `tratamiento_realizado`: TEXT NOT NULL
- `observaciones`: TEXT

## Seed data inicial

Al iniciar la aplicación por primera vez (mediante `DataInitializer` o script SQL):
- **Usuario Administrador**: `admin` / contraseña inicial hasheada con BCrypt (`ROLE_ADMIN`).
- **Usuario Recepción**: `recepcion` (`ROLE_RECEPCION`).
- **Profesional Odontólogo de Prueba**: con matrícula y usuario asignado (`ROLE_ODONTOLOGO`).
- **Paciente de Prueba**: con DNI y usuario asociado (`ROLE_PACIENTE`) y su correspondiente `HistoriaClinica` creada.
