# Decisiones y Supuestos

## Decisiones documentadas

### DD-01 — Uso de TypeScript Vanilla sin framework SPA
- **Decisión**: Utilizar TypeScript directo compilado con `tsc` hacia JavaScript estándar moderno, acompañado de HTML5 semántico y CSS3 responsivo nativo.
- **Contexto**: El proyecto requiere un frontend rápido, responsivo y sin el overhead de frameworks pesados (como React o Angular), cumpliendo estrictamente con los requerimientos académicos del integrador.
- **Alternativas consideradas**: React, Angular, Vue, Vite + React.
- **Justificación**: Máximo control sobre el DOM, código portable, rápida carga y cumplimiento exacto de la directiva técnica ("TypeScript con vanilla y diseño responsivo").
- **Trade-offs aceptados**: La gestión de estado y renderizado dinámico de vistas debe implementarse manualmente con funciones helper y componentes bien estructurados en TypeScript.

### DD-02 — Base de datos relacional PostgreSQL con Docker Compose
- **Decisión**: Utilizar PostgreSQL para entornos de desarrollo y producción local vía contenedor Docker, y H2 en memoria para suites de pruebas automatizadas.
- **Contexto**: Se necesita garantizar integridad referencial estricta, soporte transaccional para reservas concurrentes y facilidad para levantar el entorno sin instalaciones manuales complejas.
- **Alternativas consideradas**: MySQL, MongoDB, SQLite.
- **Justificación**: PostgreSQL ofrece excelente rendimiento transaccional, soporte nativo de tipos fecha/hora avanzados e índices parciales para validación de solapamiento. Docker Compose garantiza reproducibilidad en cualquier máquina.
- **Trade-offs aceptados**: Requiere tener Docker instalado y corriendo localmente para el entorno de desarrollo.

### DD-03 — Política de Soft Delete (Baja Lógica)
- **Decisión**: Toda entidad clave (Pacientes, Usuarios, Odontólogos, Turnos) implementa borrado lógico mediante la bandera `activo = true/false`.
- **Contexto**: En el ámbito de la salud, las fichas médicas, historiales y registros de atención odontológica tienen carácter legal y de auditoría; la eliminación física de registros médicos está prohibida por normativas.
- **Alternativas consideradas**: Borrado físico en cascada (*hard delete*).
- **Justificación**: Mantiene la integridad histórica y permite recuperación ante bajas accidentales.
- **Trade-offs aceptados**: Todas las consultas de lectura habituales deben filtrar por `activo = true` (gestionado mediante convenciones en repositorios JPA o `@Where(clause = "activo = true")`).

---

## Supuestos inferidos

### SU-01 — Duración fija estándar de los turnos
- **Supuesto**: Todos los turnos de la primera versión tendrán una duración fija configurable (ej. 30 o 45 minutos por defecto), simplificando el cálculo de disponibilidad de la agenda.
- **Origen**: Respuesta y validación en la sesión de Discovery Q&A.
- **Riesgo si es falso**: Si ciertos tratamientos requieren bloques variables (ej. cirugía de 2 horas vs revisión de 15 min), el cálculo de slots debe admitir multiplicadores de bloques.
- **Cómo validar**: Confirmar con la cátedra o los usuarios si la duración fija cubre todos los casos del MVP.

### SU-02 — Pacientes sin usuario web registrado
- **Supuesto**: Existirán pacientes que acuden al consultorio o llaman por teléfono y no tienen cuenta web creada; la recepcionista podrá dar de alta turnos a su nombre registrando únicamente sus datos personales.
- **Origen**: Necesidad operativa de mesa de entrada en clínicas.
- **Riesgo si es falso**: Si todo turno exigiera un `usuario_id` con login, se impediría atender a pacientes sin acceso digital.
- **Cómo validar**: El campo `usuario_id` en la tabla `Paciente` se mantiene como opcional (nullable).
