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

