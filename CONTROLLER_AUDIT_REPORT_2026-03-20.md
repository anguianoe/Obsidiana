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

---

## Auditoría incremental — 2026-03-28 (WikiPage scope)

### Scope
Controllers auditados:
- `WikiPageController`
- `WikiPageAdminController`

Modo: **sin editar / con propuestas**.

### Validación técnica ejecutada
- `./gradlew test` ✅ (BUILD SUCCESSFUL)
- `./gradlew build` ✅ (BUILD SUCCESSFUL)

### Hallazgos (crítica -> media -> baja)

#### Crítica
- **Sin hallazgos críticos en este corte.**
  - Evidencia de hardening aplicado:
    - `WikiPageController` ahora declara `@PreAuthorize("hasRole('USER')")` en `src/main/java/com/nexcoyo/knowledge/obsidiana/controller/WikiPageController.java:23`.
    - `SecurityConfig` protege `API + "/pages/**"` y `API_ADMIN + "/pages/**"` por rol en `src/main/java/com/nexcoyo/knowledge/obsidiana/config/SecurityConfig.java:64-74`.

#### Media
1. **Inconsistencia de semántica de acceso entre `search` y `searchAccessible`**
   - Evidencia:
     - `search` aplica predicado propietario OR membresía activa (`WikiPageSpecifications.accessibleToUser`) en `src/main/java/com/nexcoyo/knowledge/obsidiana/service/specification/WikiPageSpecifications.java:34-54`.
     - `searchAccessible` usa query repository solo por membresía activa (`src/main/java/com/nexcoyo/knowledge/obsidiana/repository/WikiPageRepository.java:37-54`).
   - Riesgo: un usuario puede ver páginas propias en `GET /api/v1/pages`, pero no necesariamente en `GET /api/v1/pages/accessible` si no están vinculadas a workspace.
   - Propuesta: alinear `searchAccessible` al mismo contrato (owner OR membership), o documentar explícitamente que `accessible` es solo por workspace.

2. **Cobertura de pruebas específica de controllers de WikiPage ausente**
   - Evidencia:
     - No existen `WikiPageControllerTest` ni `WikiPageAdminControllerTest` en `src/test/java`.
   - Riesgo: regresiones de seguridad de rutas (401/403), extracción de actor desde sesión, y separación admin/user podrían pasar sin detección temprana.
   - Propuesta: agregar pruebas unitarias de controller (anotaciones de seguridad, forwarding de `userId` de sesión, y ausencia de endpoint de create en admin).

#### Baja
1. **Acoplamiento de control de acceso en capa facade por convención implícita**
   - Evidencia: comportamiento dual por `userId == null` / `isAdmin` en `src/main/java/com/nexcoyo/knowledge/obsidiana/facade/WikiPageFacade.java:48-99`.
   - Riesgo: futuros cambios pueden introducir bypass accidental si no se mantiene la convención de `null = admin`.
   - Propuesta: documentar contrato en JavaDoc de métodos públicos del facade y en guía de endpoints.

### Resultado del modo solicitado
- **Sin editar código de controllers en esta auditoría.**
- Se entregan propuestas accionables para remediación posterior sin cambiar la lógica actual en ejecución.

### Remediación aplicada (2026-03-28)
Se aplicaron las propuestas solicitadas para severidades media y baja:

1. **Media: unificación `search` vs `searchAccessible`**
   - Estado: ✅ **RESUELTO**
   - Cambio: `WikiPageServiceImpl.searchAccessible(...)` ahora usa `WikiPageSearchCriteria` + `search(...)` compartido con regla owner OR membership.

2. **Media: pruebas de controllers WikiPage**
   - Estado: ✅ **RESUELTO**
   - Nuevas pruebas:
     - `src/test/java/com/nexcoyo/knowledge/obsidiana/controller/WikiPageControllerTest.java`
     - `src/test/java/com/nexcoyo/knowledge/obsidiana/controller/WikiPageAdminControllerTest.java`

3. **Baja: formalizar contrato admin (`userId=null`)**
   - Estado: ✅ **RESUELTO**
   - Cambio: JavaDoc explícito agregado en `WikiPageFacade` para `search`, `getById`, `linkToWorkspace` y `tree`.

### Revalidación solicitada (2026-03-28)
Scope revalidado:
- `WikiPageController`
- `WikiPageAdminController`

Validación técnica ejecutada:
- `./gradlew test` ✅ (BUILD SUCCESSFUL)
- `./gradlew build` ✅ (BUILD SUCCESSFUL)

Estado de severidad en esta revalidación (crítica -> media -> baja):
- Crítica: 0 hallazgos abiertos
- Media: 0 hallazgos abiertos
- Baja: 0 hallazgos abiertos

Observación: se confirma que la auditoría incremental de WikiPage permanece cerrada tras la unificación de acceso, cobertura de pruebas y formalización del contrato admin.

---

## Auditoría incremental — 2026-03-28 (WikiPageRevision scope)

### Scope
Controllers auditados:
- `WikiPageRevisionController`
- `WikiPageRevisionAdminController`

Modo: **sin editar / con propuestas**.

### Validación técnica ejecutada
- `./gradlew test` ✅ (BUILD SUCCESSFUL)
- `./gradlew build` ✅ (BUILD SUCCESSFUL)

### Hallazgos (crítica -> media -> baja)

#### Crítica
- **Sin hallazgos críticos en este corte.**
  - Evidencia de hardening aplicado:
    - `WikiPageRevisionController` exige `@PreAuthorize("hasRole('USER')")` en `src/main/java/com/nexcoyo/knowledge/obsidiana/controller/WikiPageRevisionController.java:19`.
    - `WikiPageRevisionAdminController` exige `@PreAuthorize("hasRole('SUPER_ADMIN')")` en `src/main/java/com/nexcoyo/knowledge/obsidiana/controller/WikiPageRevisionAdminController.java:26`.
    - `SecurityConfig` protege `/api/v1/page-revisions/**` y `/api/v1/admin/page-revisions/**` por rol en `src/main/java/com/nexcoyo/knowledge/obsidiana/config/SecurityConfig.java:64-76`.

#### Media
1. **Cobertura de pruebas incompleta para endpoint de restore (user/admin)**
   - Evidencia:
     - Existen endpoints `POST /{pageId}/restore/{revisionId}` en ambos controllers.
     - Tests actuales no validan forwarding de `restore(...)` ni contrato `accessUserId` (`userId` en user, `null` en admin).
   - Riesgo: regresión silenciosa en boundary admin/user del restore.
   - Propuesta: agregar casos unitarios en `WikiPageRevisionControllerTest` y `WikiPageRevisionAdminControllerTest` para `restore`.

2. **Cobertura de pruebas incompleta para validación de payload cifrado**
   - Evidencia:
     - Ambos controllers validan `isEncrypted => contentIv/contentAuthTag/encryptionKdf`.
     - Tests actuales no ejercitan rutas negativas (`IllegalArgumentException`) para create/update.
   - Riesgo: cambios futuros podrían aceptar payload cifrado incompleto.
   - Propuesta: agregar tests negativos para create/update en ambos controllers.

#### Baja
1. **Duplicación de validación de cifrado en dos controllers**
   - Evidencia: mismo bloque `if (isEncrypted && missing fields)` en `WikiPageRevisionController` y `WikiPageRevisionAdminController`.
   - Riesgo: drift funcional entre rutas user/admin al evolucionar validaciones.
   - Propuesta: extraer validador común (private helper o componente) y reutilizarlo en ambos controllers.

### Resultado del modo solicitado
- **Sin editar lógica en controllers durante esta auditoría.**
- Se documentan propuestas para cerrar hallazgos de cobertura y mantenimiento.

