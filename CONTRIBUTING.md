# 🤝 Guía de Contribución para el Equipo
### Proyecto Integrador — Clínica Odontológica "La Muelita Cuyana"

> Esta guía establece los estándares de trabajo colaborativo para **Tomas**, **Diego**, **Gaspar**, **Facundo** y **Sebastian**. Seguir estos pasos asegura que nadie tenga conflictos de Git y que el código mantenga la máxima calidad para la entrega académica.

---

## 🔄 1. Flujo de Trabajo en Git (Paso a Paso)

Todos los desarrollos se integran sobre la rama `main` del repositorio central de **Sebastian** a través de **Pull Requests desde sus Forks individuales**.

### Paso 1: Actualizar tu Fork antes de empezar tu Change
Cada vez que Sebastian o un compañero mergee un cambio en `main`, debes actualizar tu copia local:

```bash
# 1. Pararte en main
git checkout main

# 2. Descargar lo último del repositorio principal de Sebastian
git pull upstream main

# 3. Actualizar tu propio repositorio en GitHub (tu fork)
git push origin main
```

---

### Paso 2: Crear tu rama atómica de trabajo
Nunca trabajes directamente en `main`. Crea siempre una rama cuyo nombre identifique el change asignado en [`CHANGES.md`](CHANGES.md):

```bash
# Formato: feature/C-XX-nombre-del-change
git checkout -b feature/C-03-auth-jwt
```

---

### Paso 3: Desarrollar con Antigravity / Claude / OpenSpec
Puedes usar tu asistente de IA preferido (Antigravity, Claude Code, etc.), ya que el repositorio cuenta con [`AGENTS.md`](AGENTS.md) y [`CLAUDE.md`](CLAUDE.md) con las reglas automáticas:

1. **Planificar el change**:
   ```bash
   /opsx:propose <nombre-del-change>
   ```
2. **Implementar las tareas con tests**:
   ```bash
   /opsx:apply <nombre-del-change>
   ```

---

### Paso 4: Checklist OBLIGATORIO antes de subir cambios
Antes de hacer commit y push, verifica localmente que todo compile y los tests pasen:

```bash
# 1. En backend: correr toda la suite de pruebas
cd backend
./gradlew test

# 2. En frontend (si hiciste cambios en UI): verificar tipado estricto
cd ../frontend
npm run build
```
⚠️ **Si algún test falla, corrígelo antes de subir.** No se aceptan Pull Requests con tests rotos.

---

### Paso 5: Subir tu rama y abrir el Pull Request

1. Haz commit con formato claro (ver sección de convenciones abajo):
   ```bash
   git add .
   git commit -m "feat(auth): implement JWT token provider and login endpoint"
   ```
2. Sube la rama a tu Fork:
   ```bash
   git push -u origin feature/C-03-auth-jwt
   ```
3. En GitHub te aparecerá el botón verde **Compare & pull request**:
   * **Base repository**: `Seba0015/Integrador_Odontologia` $\rightarrow$ `main`.
   * **Head repository**: `TU_USUARIO/Integrador_Odontologia` $\rightarrow$ tu rama.
4. Avisa al grupo de WhatsApp/Discord para que Sebastian revise y mergee el PR.

---

## 📝 2. Convenciones de Commits (Conventional Commits)

Usa mensajes descriptivos en minúscula siguiendo este estándar:

* `feat(modulo): ...` $\rightarrow$ Nueva funcionalidad (ej. `feat(odontologo): add CRUD controller and repository`).
* `fix(modulo): ...` $\rightarrow$ Corrección de un error o bug (ej. `fix(turnos): resolve overlap validation on midnight edge case`).
* `test(modulo): ...` $\rightarrow$ Incorporación o mejora de tests (ej. `test(usuario): add soft-delete repository tests`).
* `docs(modulo): ...` $\rightarrow$ Cambios en documentación (ej. `docs(api): add appointment booking HTTP requests`).
* `refactor(modulo): ...` $\rightarrow$ Refactorización de código sin cambio de comportamiento.

---

## 🛡️ 3. Reglas Duras de Arquitectura (Contrato Obligatorio)

Cualquier Pull Request debe respetar las siguientes directivas definidas en [`AGENTS.md`](AGENTS.md):

1. **DTOs Obligatorios**: NUNCA expongas entidades JPA (`@Entity`) directamente en los `@RestController`. Mapea siempre hacia/desde DTOs con validaciones Jakarta (`@Valid`, `@NotNull`, `@NotBlank`).
2. **Manejo de Errores**: NUNCA captures excepciones genéricas silenciándolas con bloques vacíos. Usa las excepciones de `com.odontologia.exception` para que el `GlobalExceptionHandler` responda con `ErrorResponseDTO`.
3. **Persistencia e Integridad**: NUNCA borres físicamente registros médicos ni usuarios. Usa soft delete (`activo = false`). Las evoluciones médicas (`EvolucionClinica`) son legalmente **inmutables** (solo INSERT y SELECT).
4. **Concurrencia de Turnos**: La asignación de turnos debe ser estrictamente transaccional (`@Transactional`) validando que no existan colisiones horarias para el mismo profesional.
5. **Frontend Estricto**: En TypeScript está prohibido usar `any`. Tipa todos los modelos y consume exclusivamente los estilos de `design-system/tokens.css`.
