# Tasks

## 1. Seguridad y errores (TDD rojo → verde)

- [x] 1.1 Escribir el test fallido `OdontologoControllerTest` (slice `@WebMvcTest` con `@Import({SecurityConfig.class, GlobalExceptionHandler.class})` y `@MockitoBean JwtAuthenticationFilter`) que verifique `GET /api/v1/public/odontologos` sin token responde 401 con la configuración actual; verificar que el test falla antes de tocar `SecurityConfig`.
- [x] 1.2 Escribir el test fallido de RBAC: un usuario con `@WithMockUser(roles = "PACIENTE")` recibe 403 con `ErrorResponseDTO` en `POST`, `PUT` y `DELETE /api/v1/admin/odontologos/**`; verificar que falla porque `@PreAuthorize` está inerte.
- [x] 1.3 Agregar `@EnableMethodSecurity` en `SecurityConfig` y `.requestMatchers(HttpMethod.GET, "/api/v1/public/odontologos").permitAll()`; verificar que el test de catálogo sin token pasa y que el de RBAC ya no ejecuta la operación (puede llegar 500 hasta 1.4).
- [x] 1.4 Agregar `@ExceptionHandler(AccessDeniedException.class)` en `GlobalExceptionHandler` devolviendo 403 con `ErrorResponseDTO` (timestamp, status, error, message, path); verificar `$.status == 403` en los tres verbos admin y que la suite existente de `AuthControllerTest`/`AuthIntegrationTest` sigue verde.

## 2. Modelo y persistencia

- [x] 2.1 Escribir el test fallido de `OdontologoRepositoryTest` sobre H2: persistir un odontólogo con su `Usuario`, recuperarlo por matrícula, por especialidad y por estado activo, y comprobar que tras `delete()` queda `activo = false` en la fila (consulta nativa) y desaparece de las consultas derivadas; verificar rojo antes de crear la entidad.
- [x] 2.2 Implementar `Odontologo extends AuditableEntity` con `@SQLDelete`/`@SQLRestriction`, `@ManyToOne` a `Usuario` con `unique = true`, `@Column(unique = true)` en `matricula`, y `OdontologoRepository` con `findByMatricula`, `existsByMatricula`, `findByEspecialidad`, `findByActivoTrue` y query nativa `findByMatriculaIncludingInactive`; verificar que el test 2.1 pasa.
- [x] 2.3 Agregar el test de constraints H2 que rechaza matrícula y `usuario_id` duplicados, y verifica que la query nativa `IncludingInactive` sí ve registros dados de baja; verificar que both assertion pasan.

## 3. DTOs y servicio

- [x] 3.1 Escribir los tests fallidos de validación de `OdontologoRequestDTO`: `password` ausente/vacío/corto y campos obligatorios faltantes producen 400 con `details` en `ErrorResponseDTO`; verificar rojo.
- [x] 3.2 Crear `OdontologoRequestDTO`, `OdontologoResponseDTO` y `OdontologoPublicDTO` con validaciones Jakarta, sin exponer entidades JPA ni `password` en las respuestas; verificar que los tests 3.1 pasan.
- [x] 3.3 Escribir el test fallido de `OdontologoServiceTest`: el alta crea `Usuario` con `Rol.ODONTOLOGO` y `passwordHash` BCrypt(10) vinculado al odontólogo, en una sola transacción (rollback si falla el segundo insert); verificar rojo.
- [x] 3.4 Implementar el alta en `OdontologoService` con `@Transactional`, reutilizando el bean `PasswordEncoder`; verificar que el test 3.3 pasa y que la respuesta no contiene la contraseña.
- [x] 3.5 Escribir los tests fallidos de duplicados: matrícula repetida y `username`/`email` de cuenta inactiva (usando `findByUsernameIncludingInactive`/`findByEmailIncludingInactive`) lanzan `ConflictException` sin dejar registros parciales; verificar rojo.
- [x] 3.6 Implementar las validaciones de duplicado en el servicio y verificar que los tests 3.5 pasan con 409 mapeado por `GlobalExceptionHandler`.

## 4. Endpoints administrativos

- [ ] 4.1 Escribir los tests fallidos de `POST /api/v1/admin/odontologos` con `@WithMockUser(roles = "ADMIN")`: 201 con datos del profesional y sin `password`; verificar rojo.
- [ ] 4.2 Implementar el `POST` en `OdontologoController` con `@PreAuthorize("hasRole('ADMIN')")` y `@Valid`; verificar que el test 4.1 pasa y que el rol `PACIENTE` sigue recibiendo 403 (test 1.2).
- [ ] 4.3 Escribir los tests fallidos de `PUT /api/v1/admin/odontologos/{id}`: modificación exitosa conserva la cuenta vinculada e id inexistente/inactivo responde 404; verificar rojo.
- [ ] 4.4 Implementar el `PUT` en controller y servicio con `@Transactional`; verificar que los tests 4.3 pasan, incluido el 409 por matrícola ajena del escenario de la spec.
- [ ] 4.5 Escribir el test fallido de `DELETE /api/v1/admin/odontologos/{id}`: odontólogo y usuario quedan con `activo = false`, las filas siguen existiendo y el catálogo ya no los lista; verificar rojo.
- [ ] 4.6 Implementar la baja lógica en el servicio (sin `delete()` físico, `desactivar()` de ambos registros en la misma transacción); verificar que el test 4.5 pasa y que el login con esa cuenta responde 403 según RN-AU-04.

## 5. Catálogo público

- [ ] 5.1 Escribir el test fallido de integración sin token: `GET /api/v1/public/odontologos` responde 200 y cada elemento contiene exactamente `id`, `nombre`, `apellido`, `matricula`, `especialidad`, sin `username`, `email` ni contraseña; verificar rojo.
- [ ] 5.2 Implementar el endpoint público devolviendo `OdontologoPublicDTO` desde `findByActivoTrue()`; verificar que el test 5.1 pasa con la whitelist de 1.3.
- [ ] 5.3 Agregar el test que confirma que un profesional dado de baja no aparece en el catálogo y verificar que pasa por `@SQLRestriction`.

## 6. Seed y documentación

- [ ] 6.1 Escribir el test fallido de idempotencia: ejecutar `DataInitializer.run()` dos veces sobre H2 y verificar que existe un único profesional de prueba con su única cuenta `ODONTOLOGO`; verificar rojo.
- [ ] 6.2 Implementar el seed idempotente (guardas `existsByUsername` y `existsByMatricula`, password hasheada con el `PasswordEncoder` existente); verificar que el test 6.1 pasa y que los tests de auth siguen verdes.
- [ ] 6.3 Actualizar `docs/api-requests.http`: agregar `"password"` al alta de odontólogo y ejemplos de `PUT /api/v1/admin/odontologos/{id}` y `DELETE /api/v1/admin/odontologos/{id}` con token admin; verificar que los tres bloques están presentes y que el body del alta incluye el campo obligatorio.

## 7. Verificación final

- [ ] 7.1 Ejecutar `./gradlew test` contra H2 y verificar que toda la suite pasa sin conexión a PostgreSQL ni a la BD de desarrollo.
- [ ] 7.2 Ejecutar `./gradlew build` y verificar compilación limpia de Java 17 sin advertencias de dependencias nuevas.
- [ ] 7.3 Verificar uno por uno los escenarios de `specs/profesionales/spec.md` (alta exitosa, password inválido, matrícula duplicada, duplicado inactivo, modificación, 404, baja, login denegado, catálogo sin token, sin datos de cuenta, inactivos excluidos, 403, 401 y seed idempotente) y registrar cuáles quedan cubiertos por tests automatizados.
- [ ] 7.4 Verificar las reglas duras del proyecto: controllers sólo con DTOs, excepciones centralizadas en `ErrorResponseDTO`, sin borrado físico de registros clínicos ni usuarios, y sin cambios visuales ni estilos hardcodeados.

## Workflow follow-up

- Archivar el change con `/opsx:archive profesionales-service` una vez satisfecha la revisión, sincronizando `specs/profesionales/spec.md` al spec principal.
- Marcar `[x]` el change C-04 en `CHANGES.md` tras el archive.
