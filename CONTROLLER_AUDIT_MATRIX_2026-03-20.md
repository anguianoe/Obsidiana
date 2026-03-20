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
