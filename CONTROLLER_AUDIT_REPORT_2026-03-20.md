# Auditoría de Controllers — 2026-03-20 (Post-remediación)

## Scope
Controllers auditados:
- `WorkspaceController`
- `WorkspaceAdminController`
- `UserAdminController`
- `AuthController`

Modo: **sin editar / con propuestas** en auditoría inicial, luego **remediación de hallazgos medios y bajos**.

## Validación técnica
- `./gradlew test` ✅
- `./gradlew build` ✅

---

## Estado actual de hallazgos

### Crítico (pendiente)
1. **Auth forgot-password loggea token de reseteo en claro**
   - Estado: **PENDIENTE** (se trabajará mañana, como indicaste).

### Medios
1. `UserAdminController` sin seguridad declarativa de método/clase
   - Estado: ✅ **RESUELTO** (`@PreAuthorize("hasRole('SUPER_ADMIN')")` agregado).
2. `UserAdminController` actor de tag assignment controlado por cliente
   - Estado: ✅ **RESUELTO** (actor se toma desde sesión en controller y se fuerza en facade).
3. `AuthController` inconsistencia 400/401 en credenciales inválidas
   - Estado: ✅ **RESUELTO** (password inválido ahora retorna `401 Unauthorized`).
4. Falta de pruebas para controllers auditados
   - Estado: ✅ **ATENDIDO** con pruebas unitarias nuevas para comportamiento y seguridad declarativa.

### Bajos
1. `WorkspaceAdminController.search` recibía `createdBy` pero no se aplicaba
   - Estado: ✅ **RESUELTO** (criterio y specification actualizados).
2. `WorkspaceAdminController.create` usaba semántica no-admin (`save(..., false)`)
   - Estado: ✅ **RESUELTO** (`save(..., true)`).
3. Ruta admin de alta de membresía inconsistente (`add-to-workspaces`)
   - Estado: ✅ **RESUELTO** con compatibilidad: soporta `/{userId}/workspaces` y `/{userId}/add-to-workspaces`.
4. `AuthController.logout` retornaba JSON manual en string
   - Estado: ✅ **RESUELTO** (respuesta tipada `Map<String, String>`).

---

## Cambios implementados

### Código de aplicación
- `src/main/java/com/nexcoyo/knowledge/obsidiana/controller/UserAdminController.java`
- `src/main/java/com/nexcoyo/knowledge/obsidiana/facade/UserAdminFacade.java`
- `src/main/java/com/nexcoyo/knowledge/obsidiana/controller/WorkspaceAdminController.java`
- `src/main/java/com/nexcoyo/knowledge/obsidiana/facade/WorkspaceFacade.java`
- `src/main/java/com/nexcoyo/knowledge/obsidiana/service/dto/search/WorkspaceSearchCriteria.java`
- `src/main/java/com/nexcoyo/knowledge/obsidiana/service/specification/WorkspaceSpecifications.java`
- `src/main/java/com/nexcoyo/knowledge/obsidiana/controller/AuthController.java`
- `src/main/java/com/nexcoyo/knowledge/obsidiana/service/security/AuthService.java`

### Pruebas agregadas/actualizadas
- `src/test/java/com/nexcoyo/knowledge/obsidiana/controller/UserAdminControllerTest.java`
- `src/test/java/com/nexcoyo/knowledge/obsidiana/controller/WorkspaceAdminControllerTest.java`
- `src/test/java/com/nexcoyo/knowledge/obsidiana/controller/WorkspaceControllerTest.java`
- `src/test/java/com/nexcoyo/knowledge/obsidiana/service/security/AuthServiceTest.java`
- `src/test/java/com/nexcoyo/knowledge/obsidiana/facade/WorkspaceFacadeTest.java` (nuevo caso para `createdBy`)

---

## Pendiente explícito
- **No corregido por petición tuya:** token de reset en logs dentro de `AuthService.forgotPassword(...)`.

---

## Cierre
- Hallazgos **medios y bajos**: corregidos.
- Hallazgo **crítico**: pendiente, separado para siguiente intervención.
