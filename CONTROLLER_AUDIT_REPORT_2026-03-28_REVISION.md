# Auditoría de Controllers — 2026-03-28 (WikiPageRevision — Profunda)

## Scope

| Item | Valor |
|---|---|
| Controllers auditados | `WikiPageRevisionController`, `WikiPageRevisionAdminController` |
| Modo | **Sin editar / con propuestas** |
| Archivos trazados | `WikiPageRevisionFacade`, `WikiPageRevisionServiceImpl`, `WikiPageRevisionCryptoService`, `WikiPageRevisionCreateRequest`, `WikiPageRevisionDecryptRequest`, `WikiPageRevisionResponse`, `RestExceptionHandler`, `SecurityConfig` |
| Fecha | 2026-03-28 |

## Validación técnica (baseline)

| Tarea | Resultado |
|---|---|
| `./gradlew test` | ✅ BUILD SUCCESSFUL |
| `./gradlew build` | ✅ BUILD SUCCESSFUL |
| Tests de controladores | 6 tests `WikiPageRevisionControllerTest` ✅ · 6 tests `WikiPageRevisionAdminControllerTest` ✅ |
| Tests cubiertos | `getById`, `latest`, `summary`, `create`, `update`, `classRole` — en ambos |
| Tests **no cubiertos** | `restore`, `decrypt` — en **ambos** controllers |

---

## Hallazgos por Severidad

---

### 🔴 Crítico

---

#### C-1 · `null` como señal de bypass admin — contrato frágil sin garantías de tipo

**Archivos:** `WikiPageRevisionFacade.java` — todos los métodos públicos

**Descripción:**  
El facade implementa el contrato `userId == null → contexto admin, sin validación de acceso por página`. Esta convención **no tiene respaldo de tipo**:
- Un bug en `GeneralService.getIdUserFromSession()` que retorne `null` en lugar de lanzar excepción silenciaría todos los checks de autorización para usuarios normales.
- No hay tests que verifiquen que una sesión inválida produce 401 en lugar de silencio.
- La convención está sólo en un JavaDoc de texto libre — no hay `@Nullable`/`@NonNull`, no hay enum `AccessContext`, nada que el compilador pueda verificar.

**Evidencia:**
```java
// WikiPageRevisionFacade.java
/** Contract: userId=null means admin context and skips page-scoped checks. */
public WikiPageRevisionResponse getById(UUID revisionId, UUID userId) {
    WikiPageRevision revision = revisionService.getRequired(revisionId);
    if (userId != null) {                        // ← silently skipped if null
        wikiPageService.assertUserPageAccess(revision.getPage().getId(), userId);
    }
    return ApiMapper.toResponse(revision);
}
```

**Propuesta:**  
Reemplazar el parámetro `UUID userId` con un enum o record de contexto:
```java
public sealed interface AccessContext {
    record UserContext(UUID userId) implements AccessContext {}
    record AdminContext() implements AccessContext {}
}
// Uso:
public WikiPageRevisionResponse getById(UUID revisionId, AccessContext access) {
    WikiPageRevision revision = revisionService.getRequired(revisionId);
    if (access instanceof AccessContext.UserContext ctx) {
        wikiPageService.assertUserPageAccess(revision.getPage().getId(), ctx.userId());
    }
    return ApiMapper.toResponse(revision);
}
```
Alternativamente: fachada admin dedicada (`WikiPageRevisionAdminFacade`) que no acepta `userId`.

---

#### C-2 · `decrypt` admin no captura identidad del actor — cero auditoría

**Archivo:** `WikiPageRevisionAdminController.java` — método `decrypt()`

**Descripción:**  
El endpoint `POST /api/v1/admin/page-revisions/{revisionId}/decrypt` descifra contenido potencialmente sensible sin registrar **quién** realizó la operación. Comparar con `restore()` que sí captura el actor:

```java
// AdminController — restore() CORRECTO: captura actor
@PostMapping("/{pageId}/restore/{revisionId}")
public WikiPageRevisionResponse restore(...) {
    UUID userId = generalService.getIdUserFromSession();   // ← actor capturado
    return revisionFacade.restore(pageId, revisionId, userId, null);
}

// AdminController — decrypt() INCORRECTO: no captura actor
@PostMapping("/{revisionId}/decrypt")
public WikiPageRevisionDecryptedResponse decrypt(...) {
    return revisionFacade.decrypt(revisionId, request.encryptionKey(), null);
    // ↑ actorId no se extrae ni se pasa — operación sin traza
}
```

Cualquier `SUPER_ADMIN` puede descifrar cualquier revisión de cualquier página sin ningún registro.

**Propuesta:**  
```java
@PostMapping("/{revisionId}/decrypt")
public WikiPageRevisionDecryptedResponse decrypt(
        @PathVariable UUID revisionId,
        @Valid @RequestBody WikiPageRevisionDecryptRequest request) {
    UUID actorId = generalService.getIdUserFromSession(); // ← agregar
    // Pasar actorId a facade/servicio para log de auditoría
    return revisionFacade.decrypt(revisionId, request.encryptionKey(), null, actorId);
}
```
El facade/servicio debe persistir o loggear `(actorId, revisionId, timestamp)` como evento de auditoría de seguridad.

---

#### C-3 · `anyRequest().permitAll()` — toda ruta no listada es pública

**Archivo:** `SecurityConfig.java` — línea 89

**Descripción:**  
La cadena de filtros de seguridad termina con:
```java
.anyRequest().permitAll()
```
Esto significa que **cualquier ruta nueva** añadida al proyecto que no coincida con un `requestMatcher` explícito es automáticamente accesible sin autenticación. El patrón correcto es denegar-por-defecto.

**Propuesta:**
```java
// Cambiar:
.anyRequest().permitAll()
// Por:
.anyRequest().authenticated()
// O más restrictivo:
.anyRequest().denyAll()
```

---

### 🟠 Alto

---

#### H-1 · Clave de cifrado transmitida al servidor — rompe el modelo E2E

**Archivos:** `WikiPageRevisionCreateRequest.java`, `WikiPageRevisionDecryptRequest.java`, `WikiPageRevisionCryptoService.java`

**Descripción:**  
El campo `encryptionKey` viaja en el cuerpo HTTP y el servidor ejecuta PBKDF2 + AES-GCM. Esto implica:
1. Si cualquier logger HTTP está activo (Actuator trace, debug logging), la clave queda en logs en claro.
2. El servidor tiene acceso al contenido en claro durante el procesamiento — no es E2E real.
3. Una comprometida del servidor expone todas las claves.

**Propuesta:** Decisión arquitectónica necesaria:
- **Opción A (E2E verdadero):** El cifrado ocurre sólo en el cliente. El servidor almacena únicamente `contentCiphertext`, `contentIv`, `contentAuthTag`. La clave **nunca llega al servidor**. Eliminar `encryptionKey` de todos los request DTOs.
- **Opción B (cifrado servidor controlado):** Si se acepta que el servidor cifra, asegurar que `encryptionKey` se limpie de logs (`@JsonIgnore` en serialización de logs, sanitización en filtros HTTP).

---

#### H-2 · Sin límite de tamaño en `contentHtml`/`contentText` — vector DoS

**Archivo:** `WikiPageRevisionCreateRequest.java`

**Descripción:**  
Los campos de contenido no tienen restricción de tamaño a nivel aplicación. El límite de Tomcat por defecto (2 MB) aplica al body completo, pero un payload de ~1.9 MB de `contentHtml` desencadena PBKDF2 (costoso por diseño) + AES-GCM sobre un buffer grande, pudiendo agotar heap.

**Propuesta:**
```java
// WikiPageRevisionCreateRequest.java
@Size(max = 1_048_576, message = "contentHtml exceeds 1 MB limit")
String contentHtml,

@Size(max = 524_288, message = "contentText exceeds 512 KB limit")
String contentText,
```
Agregar también en `application.yml`:
```yaml
spring:
  servlet:
    multipart:
      max-request-size: 2MB
server:
  tomcat:
    max-http-form-post-size: 2MB
```

---

#### H-3 · Validación de cifrado triplicada — riesgo de deriva

**Archivos:** `WikiPageRevisionController.java` (`create`, `update`), `WikiPageRevisionAdminController.java` (`create`, `update`), `WikiPageRevisionFacade.java` (`save`)

**Descripción:**  
El mismo bloque de validación existe en 5 lugares:
```java
if(Boolean.TRUE.equals(request.isEncrypted()) &&
        (request.encryptionKey() == null || request.encryptionKey().isBlank())) {
    throw new IllegalArgumentException("encryptionKey is required when isEncrypted=true");
}
```
La copia en los controllers es redundante dado que el facade **ya valida**. Si la lógica de validación evoluciona (e.g., también validar `contentIv` y `contentAuthTag`), los controllers quedarán desactualizados.

**Propuesta:**  
Eliminar las copias de los controllers. El facade es el lugar canónico. Complementariamente, mover la validación al DTO con una restricción declarativa:
```java
// Agregar en WikiPageRevisionCreateRequest.java
@AssertTrue(message = "encryptionKey is required when isEncrypted=true")
default boolean isEncryptionKeyPresentWhenRequired() {
    return !Boolean.TRUE.equals(isEncrypted()) ||
           (encryptionKey() != null && !encryptionKey().isBlank());
}
```

---

#### H-4 · `handleGeneric` expone mensajes de excepción internos — fuga de información

**Archivo:** `RestExceptionHandler.java` — método `handleGeneric()`

**Descripción:**  
Cualquier excepción no manejada explícitamente retorna `ex.getMessage()` como 500. Excepciones JPA pueden incluir nombres de tablas, columnas y constraints. `NullPointerException` puede revelar stack frames.

**Propuesta:**
```java
@ExceptionHandler(Exception.class)
public ResponseEntity<Map<String, String>> handleGeneric(Exception ex, HttpServletRequest req) {
    log.error("Unhandled exception [{}] {}", req.getMethod(), req.getRequestURI(), ex);
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Map.of("error", "Internal server error"));  // mensaje genérico al cliente
}
```

---

#### H-5 · `IllegalArgumentException` del servicio expone UUIDs internos

**Archivos:** `WikiPageRevisionServiceImpl.java`, `RestExceptionHandler.java`

**Descripción:**  
Mensajes como `"Revision does not belong to page: <UUID>"` y `"Cannot modify an old revision"` se retornan directamente en el cuerpo de la respuesta 400. Revelan UUIDs internos del modelo de datos y detalles de la lógica de negocio.

**Propuesta:**  
Introducir `ErrorCode` tipados:
```java
public class RevisionAccessDeniedException extends RuntimeException {
    public RevisionAccessDeniedException() {
        super("REVISION_ACCESS_DENIED");
    }
}
```
Y en el `RestExceptionHandler`:
```java
@ExceptionHandler(RevisionAccessDeniedException.class)
public ResponseEntity<Map<String, String>> handleRevisionAccess(RevisionAccessDeniedException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(Map.of("error", "ACCESS_DENIED", "code", "REVISION_ACCESS_DENIED"));
}
```

---

#### H-6 · `updatePagePointer` nullable sin `@NotNull` — NPE silencioso

**Archivo:** `WikiPageRevisionController.java` — método `update()`

**Descripción:**  
`request.updatePagePointer()` puede ser `null` si el cliente omite el campo. Se pasa directamente al DTO reconstructo y luego se evalúa con `Boolean.TRUE.equals(...)` — lo que trata `null` como `false` silenciosamente, evitando el avance del puntero de página sin ningún error.

**Propuesta:**
```java
// WikiPageRevisionCreateRequest.java
@NotNull(message = "updatePagePointer is required")
Boolean updatePagePointer,
```

---

### 🟡 Medio

---

#### M-1 · `POST create` y `POST restore` retornan `200 OK` en lugar de `201 Created`

**Archivos:** Ambos controllers — `create()`, `restore()`

**Propuesta:**
```java
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public WikiPageRevisionResponse create(...) { ... }

@PostMapping("/{pageId}/restore/{revisionId}")
@ResponseStatus(HttpStatus.CREATED)
public WikiPageRevisionResponse restore(...) { ... }
```

---

#### M-2 · `pageId` redundante en `POST /{pageId}/restore/{revisionId}` — superficie de mismatch

**Archivos:** Ambos controllers — `restore()`

**Descripción:**  
El `pageId` en la URL es re-validado contra la revisión en el servicio. Un cliente puede enviar un `pageId` que no coincide con el `revisionId` para obtener información sobre la existencia de recursos (el error 400 revela que la revisión existe).

**Propuesta:**  
Simplificar a `POST /api/v1/page-revisions/{revisionId}/restore`. El `pageId` se deriva del `revisionId` en el servicio.

---

#### M-3 · Sin validación de formato en campos de metadatos criptográficos

**Archivo:** `WikiPageRevisionCreateRequest.java`

**Descripción:**  
`contentIv`, `contentAuthTag`, `encryptionKdf` no tienen restricciones de formato (`@Pattern`, `@Size`). Valores malformados causan `GeneralSecurityException` en la capa de crypto, que se propaga como `IllegalArgumentException` → 400 con detalles internos.

**Propuesta:**
```java
@Pattern(regexp = "^[A-Za-z0-9+/=]{16,64}$", message = "contentIv must be Base64, 16-64 chars")
String contentIv,

@Pattern(regexp = "^[A-Za-z0-9+/=]{24,88}$", message = "contentAuthTag must be Base64, 24-88 chars")
String contentAuthTag,

@Pattern(regexp = "^(PBKDF2|ARGON2)$", message = "encryptionKdf must be PBKDF2 or ARGON2")
String encryptionKdf,
```

---

#### M-4 · `PUT /{revisionId}` puede crear un recurso con ID diferente al path — viola semántica PUT

**Archivos:** Ambos controllers — `update()`

**Descripción:**  
Cuando `updatePagePointer=true`, `saveRevision()` puede crear una nueva revisión con un `id` diferente al `revisionId` de la URL. La respuesta tendrá un `id` distinto al que se usó en el path, lo que viola la semántica idempotente de PUT.

**Propuesta:**  
Renombrar a `POST /{revisionId}/publish` o `POST /{revisionId}/new-version` para reflejar que se crea un nuevo recurso.

---

#### M-5 · `revisionNumber = 1` hardcodeado en `create()` — responsabilidad del servidor ignorada

**Archivos:** Ambos controllers — `create()`

**Descripción:**  
```java
return revisionFacade.save(new WikiPageRevisionCreateRequest(
    null, request.pageId(), 1, ...   // ← 1 hardcodeado
), userId);
```
El servicio calcula el número real. El `1` en el controller es engañoso. Si el servicio deja de calcular el número automáticamente, el controller podría enmascarar el bug.

**Propuesta:**  
Eliminar `revisionNumber` de `WikiPageRevisionCreateRequest` para peticiones de creación, o documentar explícitamente que el valor es ignorado y el server lo calcula.

---

#### M-6 · Campos de metadatos criptográficos expuestos en `WikiPageRevisionResponse`

**Archivo:** `WikiPageRevisionResponse.java`

**Descripción:**  
`contentIv`, `contentAuthTag`, `encryptionKdf` se devuelven en cada GET de revisión. Para revisiones no cifradas son campos nulos ruidosos. Para revisiones cifradas, exponen IV, auth tag y algoritmo KDF en respuestas GET estándar, facilitando ataques de fuerza bruta sobre la clave.

**Propuesta:**  
Crear dos proyecciones: `WikiPageRevisionResponse` (sin campos crypto) y `WikiPageRevisionEncryptedMetaResponse` (con campos crypto, devuelta sólo cuando sea necesario).

---

### 🟢 Bajo

---

#### L-1 · `editorType` hardcodeado a `EditorType.CKEDITOR` — valor del cliente ignorado

**Archivos:** Ambos controllers — `create()`, `update()`

```java
// El cliente puede enviar editorType=MARKDOWN, pero se ignora silenciosamente
EditorType.CKEDITOR,   // ← siempre CKEDITOR
```

**Propuesta:** Pasar `request.editorType()` directamente. Si sólo se soporta CKEditor, eliminar el campo del DTO y marcarlo como deprecated en la documentación de API.

---

#### L-2 · `summary()` retorna lista sin paginar — riesgo de memoria con muchas revisiones

**Archivos:** Ambos controllers — `summary()`

**Propuesta:**  
```java
@GetMapping("/summary/{pageId}")
public Page<RevisionSummaryResponse> summary(
        @PathVariable UUID pageId,
        @PageableDefault(size = 20, sort = "revisionNumber", direction = Sort.Direction.DESC)
        Pageable pageable) {
    ...
}
```

---

#### L-3 · `IllegalStateException` de sesión inválida retorna `500` en lugar de `401`

**Archivo:** `RestExceptionHandler.java`

**Propuesta:**
```java
@ExceptionHandler(IllegalStateException.class)
public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
    if (ex.getMessage() != null && ex.getMessage().contains("No AuthUser")) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("error", "Authentication required"));
    }
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Map.of("error", "Internal server error"));
}
```

---

#### L-4 · Whitespace inconsistente en anotaciones `@PathVariable`

**Archivos:** Ambos controllers

```java
// Con espacio extra (inconsistente):
public WikiPageRevisionResponse getById( @PathVariable UUID revisionId)
public List< RevisionSummaryResponse > summary( @PathVariable UUID pageId)

// Sin espacio extra (consistente):
public WikiPageRevisionResponse latest(@PathVariable UUID pageId)
```

**Propuesta:** Normalizar sin espacios extra en todos los métodos. Configurar Checkstyle/EditorConfig.

---

#### L-5 · Dos fetches DB adicionales por operación `save` en el servicio

**Archivo:** `WikiPageRevisionServiceImpl.java` — `saveRevision()`

**Descripción:**  
El método recupera la entidad al inicio (para update) y al final (para retornar la proyección). El fetch final puede evitarse si la proyección se construye desde la entidad JPA guardada.

**Propuesta:**  
Usar `@EntityGraph` o construir la respuesta directamente desde la entidad guardada sin re-fetch.

---

#### L-6 · Sin cobertura de tests para `restore()` y `decrypt()` en ningún controller

**Archivos:** `WikiPageRevisionControllerTest.java`, `WikiPageRevisionAdminControllerTest.java`

**Descripción:**  
Los 6 tests de cada controller cubren `getById`, `latest`, `summary`, `create`, `update` y verificación de rol. Faltan tests para:
- `restore()` — verificar que `pageId`, `revisionId` y `actorId` se forwardan correctamente
- `decrypt()` — verificar el forwarding de `encryptionKey`, y en admin: que **no** se captura actor (hallazgo C-2)
- Casos negativos: `isEncrypted=true` sin `encryptionKey`

---

## Resumen de hallazgos

| ID | Severidad | Archivo principal | Estado |
|---|---|---|---|
| C-1 | 🔴 Crítico | `WikiPageRevisionFacade` | Propuesta: `AccessContext` sealed interface |
| C-2 | 🔴 Crítico | `WikiPageRevisionAdminController.decrypt()` | Propuesta: capturar actor + log auditoría |
| C-3 | 🔴 Crítico | `SecurityConfig` | Propuesta: `.anyRequest().authenticated()` |
| H-1 | 🟠 Alto | `WikiPageRevisionCryptoService` + DTOs | Decisión arq. requerida: E2E vs server-side |
| H-2 | 🟠 Alto | `WikiPageRevisionCreateRequest` | Propuesta: `@Size` en `contentHtml`/`contentText` |
| H-3 | 🟠 Alto | Ambos controllers + Facade | Propuesta: consolidar en `@AssertTrue` DTO |
| H-4 | 🟠 Alto | `RestExceptionHandler` | Propuesta: mensaje genérico 500 |
| H-5 | 🟠 Alto | `WikiPageRevisionServiceImpl` | Propuesta: `ErrorCode` tipados |
| H-6 | 🟠 Alto | `WikiPageRevisionCreateRequest` | Propuesta: `@NotNull Boolean updatePagePointer` |
| M-1 | 🟡 Medio | Ambos controllers | Propuesta: `@ResponseStatus(CREATED)` |
| M-2 | 🟡 Medio | Ambos controllers | Propuesta: eliminar `pageId` del path de restore |
| M-3 | 🟡 Medio | `WikiPageRevisionCreateRequest` | Propuesta: `@Pattern` en campos crypto |
| M-4 | 🟡 Medio | Ambos controllers | Propuesta: renombrar PUT a POST `/new-version` |
| M-5 | 🟡 Medio | Ambos controllers | Propuesta: eliminar `revisionNumber` de create |
| M-6 | 🟡 Medio | `WikiPageRevisionResponse` | Propuesta: dos proyecciones separadas |
| L-1 | 🟢 Bajo | Ambos controllers | Propuesta: usar `request.editorType()` |
| L-2 | 🟢 Bajo | Ambos controllers | Propuesta: paginación en `summary()` |
| L-3 | 🟢 Bajo | `RestExceptionHandler` | Propuesta: mapear `IllegalStateException` → 401 |
| L-4 | 🟢 Bajo | Ambos controllers | Propuesta: normalizar whitespace |
| L-5 | 🟢 Bajo | `WikiPageRevisionServiceImpl` | Propuesta: reducir fetches DB |
| L-6 | 🟢 Bajo | Tests de ambos controllers | Propuesta: agregar tests `restore` + `decrypt` |

**Total: 3 críticos · 6 altos · 6 medios · 6 bajos**

---

## Nota sobre estado

Este documento es **auditoría sin edición**. Ningún archivo de código fue modificado.  
Los hallazgos están disponibles para priorización e implementación en el siguiente sprint.

Orden de remediación recomendado:
1. **C-3** — cambio de una línea, impacto inmediato en seguridad global
2. **C-2** — agregar captura de actor en `decrypt()` admin
3. **H-4** — sanitizar `handleGeneric` (una línea, alto impacto)
4. **H-3** — eliminar duplicación de validación en controllers
5. **L-6** — agregar tests faltantes de `restore` y `decrypt`
6. Resto según disponibilidad de sprint

---

## Verificación posterior — 2026-03-28 (sin edición / con propuestas)

### Scope
- `WikiPageRevisionController`
- `WikiPageRevisionAdminController`

### Validación técnica
- `./gradlew test` ✅ BUILD SUCCESSFUL
- `./gradlew build` ✅ BUILD SUCCESSFUL

### Hallazgos actuales por severidad

#### 🔴 Crítico
- Sin hallazgos críticos activos en este corte.

#### 🟡 Media
1. **Sin hallazgos medios abiertos en este corte**  
   **Evidencia:** `summary` ahora usa `@PageableDefault(size = 50)` y cap de 200 en facade.

#### 🟢 Baja
1. **Sin hallazgos bajos abiertos en este corte**  
   **Evidencia:** 404 sanitizado (`Resource not found`) y DTOs legacy `decrypt` eliminados.

### Conclusión del corte
- Estado general de ambos controllers: **estable y endurecido**, sin brechas críticas abiertas.
- Remanente: **sin hallazgos abiertos** para el scope auditado en este corte.

---

## Auditoría operativa — 2026-03-28 (sin edición / con propuestas)

### Scope
- `WikiPageRevisionController`
- `WikiPageRevisionAdminController`

### Validación ejecutada
- `./gradlew test` ✅ BUILD SUCCESSFUL
- `./gradlew build` ✅ BUILD SUCCESSFUL

### Hallazgos por severidad (crítica → media → baja)

#### 🔴 Crítica
- Sin hallazgos.

#### 🟡 Media
- Sin hallazgos.

#### 🟢 Baja
- Sin hallazgos.

### Propuestas de hardening futuras (no bloqueantes)
1. Mantener un tope global de paginación consistente para todos los controllers, no solo en revision summary.
2. Revisar periódicamente mensajes públicos de error para conservar política de no exponer identificadores internos.

