# Auditoria de Controllers — 2026-04-04 (Search scope)

## Scope
Controllers auditados:
- `SearchController`
- `SearchAdminController`

Modo: **sin editar / con propuestas**, seguido de **remediación de hallazgos media y baja**.

## Validacion tecnica
- `./gradlew -q test --tests "com.nexcoyo.knowledge.obsidiana.controller.SearchControllerTest" --tests "com.nexcoyo.knowledge.obsidiana.controller.SearchAdminControllerTest"` ✅
- `./gradlew -q build` ✅

---

## Hallazgos (critica -> media -> baja)

### Critica
- **Sin hallazgos criticos en este corte.**
  - Evidencia:
    - `SearchController` protegido con `@PreAuthorize("hasRole('USER')")` en `src/main/java/com/nexcoyo/knowledge/obsidiana/controller/SearchController.java:20`.
    - `SearchAdminController` protegido con `@PreAuthorize("hasRole('SUPER_ADMIN')")` en `src/main/java/com/nexcoyo/knowledge/obsidiana/controller/SearchAdminController.java:19`.
    - `SecurityConfig` protege `/api/v1/search/**` y `/api/v1/admin/search/**` en `src/main/java/com/nexcoyo/knowledge/obsidiana/config/SecurityConfig.java:70,79`.

### Media
1. **Contrato admin no homogeneo para filtros por usuario objetivo**
   - Evidencia:
     - `SearchAdminController.accessiblePages(...)` exige `userId` (`src/main/java/com/nexcoyo/knowledge/obsidiana/controller/SearchAdminController.java:25-33`).
     - `SearchAdminController.commentThread(...)` y `SearchAdminController.orphanAssets(...)` no aceptan `userId` y operan sobre dataset global admin (`src/main/java/com/nexcoyo/knowledge/obsidiana/controller/SearchAdminController.java:35-47`).
   - Riesgo:
     - Para trazabilidad operativa/auditoria, el admin no puede reproducir de forma consistente una vista "as-user" en todos los endpoints de Search.
   - Estado: ✅ **RESUELTO**
   - Cambio aplicado:
     - `SearchAdminController.commentThread(...)` ahora acepta `userId` opcional con fallback global cuando no se informa.
     - `SearchAdminController.orphanAssets(...)` ahora acepta `userId` opcional con fallback global cuando no se informa.

### Baja
1. **No hay limite explicito de tamano de pagina en endpoints search**
   - Evidencia:
     - `SearchController.accessiblePages(...)` y `SearchController.orphanAssets(...)` reciben `Pageable` libre (`src/main/java/com/nexcoyo/knowledge/obsidiana/controller/SearchController.java:26-50`).
     - `SearchAdminController` mantiene el mismo patron (`src/main/java/com/nexcoyo/knowledge/obsidiana/controller/SearchAdminController.java:24-46`).
   - Riesgo:
     - Solicitudes con `size` excesivo pueden elevar costo de memoria/DB en escenarios de alta concurrencia.
   - Estado: ✅ **RESUELTO**
   - Cambio aplicado:
     - `@PageableDefault(size = 50)` agregado en endpoints con `Pageable` de `SearchController` y `SearchAdminController`.
     - Cap de tamaño máximo (`200`) aplicado en `SearchFacade` antes de delegar al service.

2. **Cobertura de pruebas centrada en delegacion; faltan pruebas negativas de contrato**
   - Evidencia:
     - `SearchControllerTest` y `SearchAdminControllerTest` validan `@PreAuthorize` y forwarding, pero no escenarios de parametros nulos/limites de paginacion ni contract tests de endpoints (`src/test/java/com/nexcoyo/knowledge/obsidiana/controller/SearchControllerTest.java`, `src/test/java/com/nexcoyo/knowledge/obsidiana/controller/SearchAdminControllerTest.java`).
   - Estado: ✅ **RESUELTO (alcance unitario)**
   - Cambio aplicado:
     - `SearchAdminControllerTest` amplía cobertura para ramas `userId` presente/ausente en `commentThread` y `orphanAssets`.
     - `SearchControllerTest` y `SearchAdminControllerTest` validan presencia de `@PageableDefault(size = 50)` en endpoints paginados.

---

## Cumplimiento de reglas solicitadas (USER)

- `accessiblePages`: ✅ cumple regla (creador u asociado por membership activo) via `WikiPageRepository.searchAccessiblePages(...)` en `src/main/java/com/nexcoyo/knowledge/obsidiana/repository/WikiPageRepository.java:54-85`.
- `commentThread`: ✅ cumple regla via `PageCommentRepository.findThreadForUser(...)` en `src/main/java/com/nexcoyo/knowledge/obsidiana/repository/PageCommentRepository.java:33-62`.
- `orphanAssets`: ✅ cumple regla (solo creador/subidor) via `StoredAssetRepository.findOrphanCandidatesByUploadedBy(...)` en `src/main/java/com/nexcoyo/knowledge/obsidiana/repository/StoredAssetRepository.java:43-53`.
- `SearchAdminController`: ✅ mantiene endpoints sin restricciones por ownership/membership (solo rol SUPER_ADMIN).

---

## Conclusiones
- El scope `SearchController`/`SearchAdminController` queda funcional y alineado con las reglas USER solicitadas.
- No se detectaron riesgos criticos activos.
- Hallazgos media y baja de este corte quedaron aplicados y revalidados.

