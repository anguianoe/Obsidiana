# Controller Audit Matrix — 2026-03-20 (Post-remediación)

## Baseline técnico
- `./gradlew test` ✅
- `./gradlew build` ✅

## Estado por controller

| Controller | Estado actual | Pendientes |
|---|---|---|
| `WorkspaceController` | OK | Ninguno |
| `WorkspaceAdminController` | OK (corregidos filtros/admin semantics) | Ninguno |
| `UserAdminController` | OK (hardening aplicado) | Ninguno |
| `AuthController` | Parcial | 1 crítico pendiente (token reset en logs) |

## Cambios de remediación aplicados

| Hallazgo | Estado |
|---|---|
| Seguridad declarativa en `UserAdminController` | ✅ Resuelto |
| Actor de tag assignment tomado de sesión | ✅ Resuelto |
| Inconsistencia 400/401 en login inválido | ✅ Resuelto |
| Cobertura de pruebas de controllers auditados | ✅ Atendido |
| Filtro `createdBy` en admin search workspaces | ✅ Resuelto |
| Semántica admin en `WorkspaceAdminController.create` | ✅ Resuelto |
| Ruta REST de membresía + compatibilidad legacy | ✅ Resuelto |
| Respuesta tipada en `AuthController.logout` | ✅ Resuelto |

## Pendiente explícito (fuera de esta entrega)

| Hallazgo crítico | Estado |
|---|---|
| `forgot-password` loggea token de reset en claro | ⏳ Pendiente para siguiente intervención |

---

## Auditoría incremental — 2026-03-28 (WikiPage scope)

### Baseline técnico validado
- `./gradlew test` ✅
- `./gradlew build` ✅

Última revalidación: 2026-03-28 ✅

### Estado por controller

| Controller | Estado actual | Hallazgos principales |
|---|---|---|
| `WikiPageController` | OK | Ninguno |
| `WikiPageAdminController` | OK | Ninguno |

### Matriz de hallazgos y propuestas

| Severidad | Hallazgo | Evidencia | Propuesta |
|---|---|---|---|
| Crítica | Sin hallazgos críticos en este corte | `WikiPageController` con `@PreAuthorize("hasRole('USER')")` + reglas explícitas en `SecurityConfig` para `/api/v1/pages/**` y `/api/v1/admin/pages/**` | Mantener controles y cubrirlos con tests de seguridad |
| Media | `search` y `searchAccessible` no compartían contrato de acceso | `WikiPageServiceImpl.searchAccessible` ahora usa ruta unificada por `search` | ✅ Resuelto (owner OR membership unificado) |
| Media | No había pruebas unitarias dedicadas para ambos controllers | Se agregaron `WikiPageControllerTest` y `WikiPageAdminControllerTest` | ✅ Resuelto |
| Baja | Convención `userId == null` para admin estaba implícita | JavaDoc explícito en métodos públicos de `WikiPageFacade` | ✅ Resuelto |


---

## Auditoría incremental — 2026-03-28 (WikiPageRevision scope — superficial)

> ⚠️ Este bloque fue reemplazado por la auditoría profunda del mismo día. Ver sección siguiente.

### Hallazgos del corte superficial (superados)

| Severidad | Hallazgo | Estado |
|---|---|---|
| Media | Faltan pruebas del endpoint `restore` | ⏳ Pendiente (confirmado en auditoría profunda, L-6) |
| Media | Faltan pruebas negativas de validación de cifrado | ⏳ Pendiente (confirmado en auditoría profunda, L-6) |
| Baja | Validación de cifrado duplicada entre controllers | ⏳ Pendiente (elevado a Alto H-3 en auditoría profunda) |

---

## Auditoría profunda — 2026-03-28 (WikiPageRevision — completa)

### Baseline técnico validado
- `./gradlew test` ✅  (6 tests `WikiPageRevisionControllerTest` · 6 tests `WikiPageRevisionAdminControllerTest`)
- `./gradlew build` ✅

### Estado por controller

| Controller | Estado | Hallazgos | Documento |
|---|---|---|---|
| `WikiPageRevisionController` | ⚠️ Parcial — 15 hallazgos abiertos | 3C · 6A · 6M · 6B | `CONTROLLER_AUDIT_REPORT_2026-03-28_REVISION.md` |
| `WikiPageRevisionAdminController` | ⚠️ Parcial — 15 hallazgos abiertos | 3C · 6A · 6M · 6B | `CONTROLLER_AUDIT_REPORT_2026-03-28_REVISION.md` |

### Matriz de hallazgos

| ID | Severidad | Hallazgo | Archivo | Estado |
|---|---|---|---|---|
| C-1 | 🔴 Crítico | `null` como bypass admin — sin tipo | `WikiPageRevisionFacade` | ✅ Resuelto: `AccessContext` tipado (user/admin) |
| C-2 | 🔴 Crítico | `decrypt()` admin sin auditoría de actor | `WikiPageRevisionAdminController` | ✅ Resuelto: endpoint `decrypt` removido (E2E cliente) |
| C-3 | 🔴 Crítico | `anyRequest().permitAll()` global | `SecurityConfig` | ✅ Resuelto: `.anyRequest().authenticated()` |
| H-1 | 🟠 Alto | Clave cifrado enviada al servidor — rompe E2E | `WikiPageRevisionCryptoService` + DTOs | ✅ Resuelto: Opción A E2E (sin llave ni decrypt server-side) |
| H-2 | 🟠 Alto | Sin límite de tamaño en `contentHtml`/`contentText` | `WikiPageRevisionCreateRequest` | ✅ Resuelto: `@Size` aplicado |
| H-3 | 🟠 Alto | Validación cifrado triplicada en controllers+facade | Ambos controllers + Facade | ✅ Resuelto: validación consolidada con `@AssertTrue` |
| H-4 | 🟠 Alto | `handleGeneric` expone mensajes de excepción | `RestExceptionHandler` | ✅ Resuelto: mensaje genérico en 500 |
| H-5 | 🟠 Alto | `IllegalArgumentException` expone UUIDs internos | `WikiPageRevisionServiceImpl` | ✅ Resuelto: `ApiException` + `ErrorCode` tipados |
| H-6 | 🟠 Alto | `updatePagePointer` nullable sin `@NotNull` | `WikiPageRevisionCreateRequest` | ✅ Resuelto: `@NotNull Boolean` |
| M-1 | 🟡 Medio | `create`/`restore` retornan `200 OK` | Ambos controllers | ✅ Resuelto: `@ResponseStatus(CREATED)` aplicado |
| M-2 | 🟡 Medio | `pageId` redundante en path de restore | Ambos controllers | ✅ Resuelto: `restore/{revisionId}` + page derivada server-side |
| M-3 | 🟡 Medio | Sin validación de formato en campos crypto | `WikiPageRevisionCreateRequest` | ✅ Resuelto: `@Pattern` en `contentIv`, `contentAuthTag`, `encryptionKdf` |
| M-4 | 🟡 Medio | `PUT` puede crear recurso con ID diferente al path | Ambos controllers | ✅ Resuelto: reemplazado por `POST /{revisionId}/new-version` |
| M-5 | 🟡 Medio | `revisionNumber=1` hardcodeado en `create()` | Ambos controllers | ✅ Resuelto: create/new-version ya no fuerzan `revisionNumber` |
| M-6 | 🟡 Medio | Campos crypto en `WikiPageRevisionResponse` siempre | `WikiPageRevisionResponse` | ✅ Resuelto: dos proyecciones (`WikiPageRevisionResponse` y `WikiPageRevisionEncryptedResponse`) |
| L-1 | 🟢 Bajo | `editorType` hardcodeado a `CKEDITOR` | Ambos controllers | ✅ Aceptado por decisión de producto (versión actual solo CKEDITOR) |
| L-2 | 🟢 Bajo | `summary()` sin paginación | Ambos controllers | ✅ Resuelto: endpoint con `Pageable` + `PageResponse` |
| L-3 | 🟢 Bajo | `IllegalStateException` retorna 500 en lugar de 401 | `RestExceptionHandler` | ✅ Resuelto: mapeado a `401 Unauthorized` |
| L-4 | 🟢 Bajo | Whitespace inconsistente en `@PathVariable` | Ambos controllers | ✅ Resuelto: normalización de estilo/firmas |
| L-5 | 🟢 Bajo | Dos fetches DB extra por `save` | `WikiPageRevisionServiceImpl` | ✅ Resuelto: query liviana `max(revisionNumber)` + retorno sin re-fetch final |
| L-6 | 🟢 Bajo | Sin tests para `restore()` y `decrypt()` | Tests de ambos controllers | ✅ Resuelto: cobertura de `restore` + pruebas negativas de validación/ciphertext |

**Total: 3 críticos · 6 altos · 6 medios · 6 bajos**


---

## Auditoría de verificación — 2026-03-28 (WikiPageRevision, sin edición / con propuestas)

### Validación técnica
- `./gradlew test` ✅ BUILD SUCCESSFUL
- `./gradlew build` ✅ BUILD SUCCESSFUL

### Hallazgos residuales (orden: crítica → media → baja)

| ID | Severidad | Hallazgo | Evidencia | Propuesta |
|---|---|---|---|---|
| V-1 | 🔴 Crítico | Sin hallazgos críticos en este corte | Controllers con `@PreAuthorize` y `AccessContext` tipado | Mantener cobertura y revisión en cada cambio de contrato |
| V-2 | 🟡 Media | `summary` paginable sin límite explícito de tamaño de página | `WikiPageRevisionController.summary` y `WikiPageRevisionAdminController.summary` | ✅ Resuelto: `@PageableDefault(size=50)` + cap de `200` en facade |
| V-3 | 🟢 Baja | Mensajes `NOT_FOUND` exponen UUID internos en respuestas | `RestExceptionHandler.handleNotFound` | ✅ Resuelto: mensaje público genérico `Resource not found` manteniendo `ErrorCode.NOT_FOUND` |
| V-4 | 🟢 Baja | DTOs legacy de `decrypt` permanecen en código aunque endpoint fue removido | DTOs de request/response decrypt | ✅ Resuelto: DTOs eliminados |

### Estado del scope auditado
- `WikiPageRevisionController`: ✅ Sin riesgos críticos activos; sin residuales abiertos en este corte.
- `WikiPageRevisionAdminController`: ✅ Sin riesgos críticos activos; sin residuales abiertos en este corte.

---

## Ejecución de auditoría solicitada — 2026-03-28

Scope:
- `WikiPageRevisionController`
- `WikiPageRevisionAdminController`

Modo:
- Sin edición / con propuestas

Validación:
- `./gradlew test` ✅
- `./gradlew build` ✅

Resultado por severidad:
- 🔴 Crítica: 0
- 🟡 Media: 0
- 🟢 Baja: 0


