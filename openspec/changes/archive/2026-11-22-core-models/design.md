# Design

## Context

El change `core-models` establece el modelo de datos relacional primario y las convenciones de excepciones para toda la aplicación backend. Se apoya en la infraestructura creada en `foundation-setup`. Ver `proposal.md` para la motivación general.

## Goals / Non-Goals

**Goals:**
- Implementar la clase abstracta `@MappedSuperclass AuditableEntity` con timestamps de creación y actualización, y bandera `activo`.
- Implementar la entidad JPA `Usuario` con restricciones únicas en `username` y `email`.
- Crear el enum `Rol` (`ADMIN`, `RECEPCION`, `ODONTOLOGO`, `PACIENTE`).
- Implementar `UsuarioRepository` heredando de `JpaRepository` con filtros activos.
- Implementar `GlobalExceptionHandler` con `@RestControllerAdvice` respondiendo `ErrorResponseDTO`.
- Crear excepciones de negocio (`ResourceNotFoundException`, `BadRequestException`, `ConflictException`).
- Implementar `DataInitializer` mediante `CommandLineRunner` para sembrar cuentas `admin` y `recepcion` usando `BCryptPasswordEncoder` (costo >= 10 según RN-AU-01).
- Cubrir con tests unitarios y de integración sobre base H2 en memoria.

**Non-Goals:**
- Configuración de filtros JWT y endpoints de login `/api/v1/auth/login` (alcance de C-03).
- Gestión de entidades de Odontólogos o Pacientes (alcance de C-04 y C-05).

## Decisions

### Decisión 1: Implementación de Soft Delete en Hibernate 6+
- **Elección**: Uso de anotaciones estándar `@SQLDelete(sql = "UPDATE usuarios SET activo = false WHERE id = ?")` y `@SQLRestriction("activo = true")`.
- **Razón**: Es el reemplazo canónico en Spring Boot 3 / Hibernate 6 para la anotación deprecada `@Where`, asegurando que las consultas estándar descarten registros dados de baja sin modificar el código de los repositorios.

### Decisión 2: Formato de Excepciones Globales
- **Elección**: DTO inmutable `ErrorResponseDTO` conteniendo `timestamp`, `status`, `error`, `message` y `path`.
- **Razón**: Da certeza al frontend para manejar errores de forma uniforme en todas las llamadas API (RN-EX-02).

### Decisión 3: Cifrado de Contraseñas
- **Elección**: Bean `PasswordEncoder` implementado con `BCryptPasswordEncoder(10)` en la configuración de seguridad.
- **Razón**: Cumple estrictamente con el estándar de seguridad OWASP y la regla de negocio RN-AU-01.

## UI Mockups
Decision: no (project policy)

## Risks / Trade-offs

- **[Búsquedas de usuarios inactivos para auditoría administrativa]** → *Mitigación*: Se definen métodos de consulta explícitos en `UsuarioRepository` con queries nativas o personalizadas cuando se requiera consultar el historial de bajas.
- **[Inicialización de datos en múltiples entornos]** → *Mitigación*: `DataInitializer` verifica previamente `usuarioRepository.existsByUsername("admin")` antes de persistir, evitando inserciones duplicadas al reiniciar la aplicación.
