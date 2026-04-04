# Auditoría de Controllers — 2026-04-04 (Trash scope)

## Scope
Controllers auditados:
- `TrashController`
- `TrashAdminController`

Modo: **sin editar / con propuestas**, seguido de **remediación de hallazgos medios y bajos**.

## Validación técnica
- `./gradlew -q test --tests "com.nexcoyo.knowledge.obsidiana.controller.TrashControllerTest" --tests "com.nexcoyo.knowledge.obsidiana.controller.TrashAdminControllerTest" --tests "com.nexcoyo.knowledge.obsidiana.service.impl.TrashServiceImplTest"` ✅
- `./gradlew -q test` ✅
- `./gradlew -q build` ✅
- Revalidación post-remediación ✅

---

## Resumen ejecutivo
Estado general del corte:
- `TrashController`: **OK / endurecido**
- `TrashAdminController`: **OK / endurecido**

Resultado por severidad:
- Crítica: **0**
- Media: **0**
- Baja: **0**

---

## Hallazgos (crítica -> media -> baja)

### Crítica
- **Sin hallazgos críticos en este corte.**
  - Evidencia:
    - `TrashController` exige `@PreAuthorize("hasRole('USER')")` en `src/main/java/com/nexcoyo/knowledge/obsidiana/controller/TrashController.java:20-24`.
    - `TrashAdminController` exige `@PreAuthorize("hasRole('SUPER_ADMIN')")` en `src/main/java/com/nexcoyo/knowledge/obsidiana/controller/TrashAdminController.java:19-23`.
    - El baseline global está protegido por JWT + `anyRequest().authenticated()` en `src/main/java/com/nexcoyo/knowledge/obsidiana/config/SecurityConfig.java:63-91`.

### Media
1. **`RestoreTrashRequest.restoredBy` obligatorio en USER aunque el server lo ignoraba**
   - Evidencia:
     - `RestoreTrashRequest` declara `@NotNull UUID restoredBy` en `src/main/java/com/nexcoyo/knowledge/obsidiana/dto/request/RestoreTrashRequest.java:7`.
   - Estado: ✅ **RESUELTO**
   - Cambio aplicado:
     - nuevo DTO user-only `UserRestoreTrashRequest` sin `restoredBy`.
     - `TrashController.restore(...)` usa el nuevo DTO y mantiene actor derivado de sesión.

2. **`TrashRecordCreateRequest.deletedBy` expuesto en USER aunque el server lo sobrescribía**
   - Evidencia:
     - `TrashRecordCreateRequest` expone `UUID deletedBy` en `src/main/java/com/nexcoyo/knowledge/obsidiana/dto/request/TrashRecordCreateRequest.java:11-24`.
   - Estado: ✅ **RESUELTO**
   - Cambio aplicado:
     - nuevo DTO user-only `UserTrashRecordCreateRequest` sin `deletedBy`.
     - `TrashController.moveToTrash(...)` usa el nuevo DTO y mantiene actor derivado de sesión.

### Baja
1. **Protección de rutas Trash no estaba explicitada en `SecurityConfig`**
   - Evidencia:
     - `SecurityConfig` enumera `/users`, `/workspaces`, `/pages`, `/page-revisions`, `/tags`, pero no `/trash` ni `/admin/trash` en `src/main/java/com/nexcoyo/knowledge/obsidiana/config/SecurityConfig.java:64-77`.
   - Estado: ✅ **RESUELTO**
   - Cambio aplicado:
      - reglas explícitas agregadas para `/api/v1/trash/**` y `/api/v1/admin/trash/**` en `SecurityConfig`.

2. **Semántica de acceso entre endpoints USER no era homogénea y requería documentación formal**
   - Evidencia:
     - `search(...)` usa visibilidad amplia (`deletedBy` OR relación por workspace/page/comment/asset) vía `TrashRecordSpecifications.visibleToUser(...)` en `src/main/java/com/nexcoyo/knowledge/obsidiana/service/specification/TrashRecordSpecifications.java:43-52,81-120`.
     - `getById(...)` y `overdue(...)` son más restrictivos: solo `deletedBy` en `src/main/java/com/nexcoyo/knowledge/obsidiana/service/impl/TrashServiceImpl.java:111-115,140-143`.
     - `restore(...)` vuelve a ser más amplio por relación o autoría en `src/main/java/com/nexcoyo/knowledge/obsidiana/service/impl/TrashServiceImpl.java:166-177`.
   - Estado: ✅ **RESUELTO**
   - Cambio aplicado:
      - JavaDoc agregado en `TrashController` y `TrashAdminController`.
      - documento nuevo: `TRASH_ACCESS_CONTRACT.md`.

---

## Remediación aplicada — 2026-04-04

### Código actualizado
- `src/main/java/com/nexcoyo/knowledge/obsidiana/controller/TrashController.java`
- `src/main/java/com/nexcoyo/knowledge/obsidiana/controller/TrashAdminController.java`
- `src/main/java/com/nexcoyo/knowledge/obsidiana/facade/TrashFacade.java`
- `src/main/java/com/nexcoyo/knowledge/obsidiana/config/SecurityConfig.java`
- `src/main/java/com/nexcoyo/knowledge/obsidiana/dto/request/UserTrashRecordCreateRequest.java`
- `src/main/java/com/nexcoyo/knowledge/obsidiana/dto/request/UserRestoreTrashRequest.java`
- `TRASH_ACCESS_CONTRACT.md`

### Pruebas validadas
- `src/test/java/com/nexcoyo/knowledge/obsidiana/controller/TrashControllerTest.java`
- `src/test/java/com/nexcoyo/knowledge/obsidiana/controller/TrashAdminControllerTest.java`
- `src/test/java/com/nexcoyo/knowledge/obsidiana/service/impl/TrashServiceImplTest.java`

---

## Validación de separación admin/user

### `TrashController`
- Usa `generalService.getIdUserFromSession()` en todos los endpoints.
- No expone bypass tipo `userId == null` en controller.
- Delega la autorización fina al service/facade.
- Comportamiento validado por:
  - `src/test/java/com/nexcoyo/knowledge/obsidiana/controller/TrashControllerTest.java`
  - `src/test/java/com/nexcoyo/knowledge/obsidiana/service/impl/TrashServiceImplTest.java`

### `TrashAdminController`
- Expone operaciones completas sin restricciones de ownership/membership del usuario objetivo.
- Mantiene separación por rol `SUPER_ADMIN` en clase.
- Comportamiento validado por:
  - `src/test/java/com/nexcoyo/knowledge/obsidiana/controller/TrashAdminControllerTest.java`

---

## Conclusión
- La separación **USER vs ADMIN** quedó endurecida y formalizada.
- No quedan hallazgos abiertos en severidad **crítica, media o baja** para este corte.
- El scope puede considerarse **cerrado y validado** en esta iteración.

