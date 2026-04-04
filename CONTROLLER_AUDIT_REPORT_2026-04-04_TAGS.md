# Auditoria de Controllers — 2026-04-04 (Tag scope)

## Scope
Controllers auditados:
- `TagController`
- `TagAdminController`

Modo: **sin editar / con propuestas**, seguido de **remediación de hallazgos media y baja**.

## Validacion tecnica
- `./gradlew -q test --tests "com.nexcoyo.knowledge.obsidiana.controller.TagControllerTest" --tests "com.nexcoyo.knowledge.obsidiana.controller.TagAdminControllerTest" --tests "com.nexcoyo.knowledge.obsidiana.service.impl.TagServiceImplTest"` ✅
- `./gradlew -q test` ✅
- `./gradlew -q build` ✅

---

## Hallazgos (critica -> media -> baja)

### Critica
- **Sin hallazgos criticos en este corte.**
  - Evidencia:
    - `TagController` protegido con `@PreAuthorize("hasRole('USER')")` en `src/main/java/com/nexcoyo/knowledge/obsidiana/controller/TagController.java:19`.
    - `TagAdminController` protegido con `@PreAuthorize("hasRole('SUPER_ADMIN')")` en `src/main/java/com/nexcoyo/knowledge/obsidiana/controller/TagAdminController.java:26`.
    - `SecurityConfig` protege `/api/v1/tags/**` y `/api/v1/admin/tags/**` por rol en `src/main/java/com/nexcoyo/knowledge/obsidiana/config/SecurityConfig.java:64-79`.

### Media
1. **Integridad de auditoria en flujo admin dependía de campos actor recibidos del cliente**
   - Evidencia:
     - `TagAdminController` ahora deriva actor de sesión para `create`, `update` y `assign` en `src/main/java/com/nexcoyo/knowledge/obsidiana/controller/TagAdminController.java`.
   - Estado: ✅ **RESUELTO**

### Baja
1. **Posible costo N+1 en evaluacion de relacion por pagina en workspace**
   - Evidencia:
     - se agregó `WikiPageRepository.existsAccessibleByWorkspaceIdAndUserId(...)` y `TagServiceImpl.hasAccessiblePageInWorkspace(...)` usa query única.
   - Estado: ✅ **RESUELTO**

2. **Cobertura de pruebas valida forwarding y reglas base, pero faltan escenarios positivos de relacion por page en algunas rutas**
   - Evidencia:
     - `TagServiceImplTest` ahora incluye escenarios positivos page-based para `saveTag(user)`, `assignTag(user)` y `getPageAssignments(user)`.
   - Estado: ✅ **RESUELTO**

---

## Evaluacion de cumplimiento de reglas solicitadas

- `activeTags`: ✅ implementado (createdBy o relacion por workspace/page/membership).
- `create/update`: ✅ implementado (relacion o createdBy en update).
- `assign`: ✅ implementado (relacion por workspace o page accesible + validacion page-workspace link).
- `assignments`: ✅ implementado (relacion o fallback a createdBy).

---

## Conclusion
- El scope `TagController`/`TagAdminController` queda funcionalmente alineado a reglas user/admin y sin hallazgos criticos.
- Hallazgos media y baja de este corte quedaron aplicados y revalidados.

