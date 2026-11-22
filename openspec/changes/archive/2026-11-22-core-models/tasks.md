# Tasks

## 1. Auditoría Base y Modelo de Dominio de Usuarios

- [x] 1.1 Crear clase abstracta `AuditableEntity` con `@MappedSuperclass`, `@CreationTimestamp`, `@UpdateTimestamp` y campo `activo`, verificando compilación limpia
- [x] 1.2 Crear enum `Rol` con valores `ADMIN`, `RECEPCION`, `ODONTOLOGO`, `PACIENTE`
- [x] 1.3 Crear entidad JPA `Usuario` extendiendo de `AuditableEntity` con restricciones de unicidad en `username` y `email`, campo `passwordHash`, rol y configuración de soft delete (`@SQLDelete`, `@SQLRestriction`)
- [x] 1.4 Crear repositorio `UsuarioRepository` extendiendo `JpaRepository<Usuario, Long>` con métodos de consulta por username y email

## 2. Manejo Global de Excepciones y DTOs de Error

- [x] 2.1 Crear clases de excepción personalizadas (`ResourceNotFoundException`, `BadRequestException`, `ConflictException`)
- [x] 2.2 Crear `ErrorResponseDTO` estructurado con `timestamp`, `status`, `error`, `message`, `path` y detalles de validación
- [x] 2.3 Implementar `GlobalExceptionHandler` anotado con `@RestControllerAdvice` capturando excepciones de negocio y errores de validación Jakarta

## 3. Semilla Inicial y Configuración de Seguridad

- [x] 3.1 Registrar bean `PasswordEncoder` (`BCryptPasswordEncoder` con costo 10) en `SecurityConfig`
- [x] 3.2 Implementar componente `DataInitializer` (`CommandLineRunner`) para sembrar usuarios base `admin` y `recepcion` con contraseña cifrada si no existen en BD

## 4. Pruebas Automatizadas y Verificación

- [x] 4.1 Escribir y ejecutar `UsuarioRepositoryTest` sobre H2 verificando persistencia, restricciones de unicidad y soft delete mediante `./gradlew test`
- [x] 4.2 Escribir y ejecutar `GlobalExceptionHandlerTest` verificando payload uniforme de error ante excepciones 400, 404 y 409 mediante `./gradlew test`
- [x] 4.3 Ejecutar verificación completa con `./gradlew clean test build` confirmando 0 fallos

## Workflow follow-up

- Iniciar implementación de tasks con `/opsx:apply core-models`
- Archivar el change mediante `/opsx:archive core-models` y marcar `[x]` en `CHANGES.md`
