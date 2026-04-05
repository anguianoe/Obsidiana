# Auditoria de Controllers — 2026-04-05 (Publication scope)

## Scope
Controller auditado:
- `PublicationController`

Modo: **sin editar / con propuestas**, seguido de **remediación de hallazgos media y baja**.

## Validacion tecnica
- `./gradlew -q test --tests "com.nexcoyo.knowledge.obsidiana.controller.PublicationControllerTest" --tests "com.nexcoyo.knowledge.obsidiana.controller.PublicationControllerWebMvcTest" --tests "com.nexcoyo.knowledge.obsidiana.service.impl.PublicationServiceImplTest"` ✅
- `./gradlew -q build` ✅

---

## Hallazgos (critica -> media -> baja)

### Critica
- **Sin hallazgos criticos en este corte.**
  - Evidencia:
    - Endpoints de escritura segregados por rol en `src/main/java/com/nexcoyo/knowledge/obsidiana/controller/PublicationController.java:41-51`.
    - Endpoints publicos live en `src/main/java/com/nexcoyo/knowledge/obsidiana/config/SecurityConfig.java:63-65`.
    - Regla user para publicar por relacion owner/membership en `src/main/java/com/nexcoyo/knowledge/obsidiana/service/impl/PublicationServiceImpl.java:42-47`.

### Media
1. **`publishForUser` con `403` usaba código semánticamente inconsistente**
   - Estado: ✅ **RESUELTO**
   - Cambio aplicado:
     - Se agregó `ErrorCode.FORBIDDEN` y `PublicationServiceImpl.publishForUser(...)` ahora lo usa al denegar acceso.

2. **Faltaba validación `revisionId` pertenece a `pageId`**
   - Estado: ✅ **RESUELTO**
   - Cambio aplicado:
     - Se agregó `WikiPageRevisionRepository.existsByIdAndPageId(...)`.
     - `PublicationServiceImpl` valida consistencia page-revision en flujos user y admin antes de persistir.

3. **`publishAdmin` aceptaba actor (`publishedBy`) desde payload**
   - Estado: ✅ **RESUELTO**
   - Cambio aplicado:
     - `PublicationController.publishAdmin(...)` deriva actor desde sesión.
     - `PublicationFacade.publish(...)` ahora recibe `actorId` explícito y no confía en `request.publishedBy`.

### Baja
1. **Paginacion pública sin cap máximo**
   - Estado: ✅ **RESUELTO**
   - Cambio aplicado:
     - `PublicationFacade.liveSummaries(...)` aplica sanitización con tope máximo `200`.

2. **Faltaban pruebas web de seguridad/contrato HTTP**
   - Estado: ✅ **RESUELTO**
   - Cambio aplicado:
     - Se agregó `PublicationControllerWebMvcTest` con cobertura de rutas públicas y permisos `USER`/`SUPER_ADMIN`.

---

## Evaluacion de cumplimiento de reglas solicitadas

- `liveSummaries` paginado con default 50: ✅ `PublicationController.liveSummaries(...)`.
- `publish` USER solo por relacion owner/membership: ✅ via `WikiPageRepository.existsAccessibleByIdAndUserId(...)` en service.
- `publishAdmin` sin restriccion de ownership: ✅ flujo admin persiste directo sin filtro owner/membership.

---

## Conclusiones
- El controller cumple el objetivo funcional solicitado para acceso publico vs publish user/admin.
- No hay hallazgos criticos activos.
- Hallazgos media y baja de este corte quedaron aplicados y revalidados.

