# Auditoria de Controllers - 2026-04-04 (Scope solicitado)

## Scope solicitado
Controllers solicitados:
- `AlertController`
- `AuthController`
- `CompanyController`
- `EngineerController`
- `ExcelFileController`
- `OrchardController`
- `PackagingController`
- `ReportsController`
- `RunsController`
- `SuppliersController`
- `UserController`
- `UserNormalController`
- `UserProfileController`

Modo: **sin editar / con propuestas**.

## Resultado de descubrimiento de scope
Controllers encontrados en el proyecto para este request:
- `AuthController`
- `UserProfileController`

Controllers **no encontrados** en este repo (12/13):
- `AlertController`, `CompanyController`, `EngineerController`, `ExcelFileController`, `OrchardController`, `PackagingController`, `ReportsController`, `RunsController`, `SuppliersController`, `UserController`, `UserNormalController`

Nota: el proyecto contiene otros controllers distintos (`WorkspaceController`, `WikiPageController`, etc.), pero no forman parte del scope solicitado.

## Validacion tecnica
- `./gradlew test` ✅
- `./gradlew build` ✅

---

## Hallazgos (critica -> media -> baja)

### Critica
1. **C-1 - Scope incompleto por controllers faltantes**
   - Evidencia: solo existen `AuthController` y `UserProfileController` dentro de `src/main/java/com/nexcoyo/knowledge/obsidiana/controller`.
   - Riesgo: no se puede certificar seguridad/operacion de 11 controllers solicitados porque no existen en este codigo fuente.
   - Propuesta:
     - Confirmar si el scope correcto era este repo.
     - Si pertenecen a otro servicio, correr auditoria en ese workspace.
     - Si fueron renombrados, mapear equivalencias de nombre antes de cerrar auditoria.

2. **C-2 - Posible IDOR en lectura de perfil por path param**
   - Evidencia: `UserProfileController.getProfile(@PathVariable UUID userId)` retorna perfil del `userId` recibido sin comparar contra el usuario autenticado (`src/main/java/com/nexcoyo/knowledge/obsidiana/controller/UserProfileController.java:27-30`).
   - Riesgo: cualquier usuario autenticado con rol `USER` podria consultar perfil de otro usuario si conoce su UUID.
   - Propuesta:
     - Hacer endpoint self-only (`/api/v1/users/profile`) usando `generalService.getIdUserFromSession()`.
     - O validar `userId == sessionUserId` (excepto rutas admin separadas).

### Media
1. **M-1 - Desalineacion de seguridad de lectura/escritura en perfil**
   - Evidencia: `updateProfile` usa `generalService.getIdUserFromSession()` (self-only), pero `getProfile` acepta cualquier `userId` (`UserProfileController.java:27-37`).
   - Riesgo: comportamiento inconsistente y facil de romper en clientes/front.
   - Propuesta: unificar contrato (ambos self-only para controller user).

2. **M-2 - `AuthController` expone respuestas no tipadas (`Map<String,Object>`)**
   - Evidencia: `login`, `refresh`, `forgot-password`, `reset-password` retornan `ResponseEntity<Map<String, Object>>` (`src/main/java/com/nexcoyo/knowledge/obsidiana/controller/AuthController.java:29-52`).
   - Riesgo: cambios de contrato no detectados en compilacion y mayor fragilidad en clientes.
   - Propuesta: definir DTOs de respuesta tipados para auth.

### Baja
1. **L-1 - Convencion de salida de `logout` no uniforme**
   - Evidencia: `logout` responde `{"logOut":"ok"}` (`AuthController.java:40-43`).
   - Riesgo: inconsistencia de naming y contrato frente a otros endpoints.
   - Propuesta: estandarizar a `{"status":"ok"}` o DTO comun de resultado.

2. **L-2 - Falta de anotaciones de documentacion API en endpoints auditados**
   - Evidencia: controllers sin anotaciones OpenAPI/Swagger.
   - Riesgo: menor trazabilidad de contrato en auditorias futuras.
   - Propuesta: agregar `@Operation`, `@ApiResponse` y ejemplos de errores comunes.

---

## Resumen ejecutivo del corte
- Scope solicitado: 13 controllers.
- Auditables en este repo: 2 (`AuthController`, `UserProfileController`).
- Hallazgos abiertos:
  - Critica: 2
  - Media: 2
  - Baja: 2
- No se editaron controllers en esta ejecucion (modo solicitado: sin editar / con propuestas).

