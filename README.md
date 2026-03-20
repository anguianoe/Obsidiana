# Flujo Completo de Wikis — Relaciones entre Entidades

## 🔎 Auditoría reciente de controllers

Documentos generados el **2026-03-20**:
- `CONTROLLER_AUDIT_REPORT_2026-03-20.md`
- `CONTROLLER_AUDIT_MATRIX_2026-03-20.md`
- `INDICE_COMPLETO_DOCUMENTACION.md`

Scope auditado:
- `WorkspaceController`
- `WorkspaceAdminController`
- `UserAdminController`
- `AuthController`

Validación técnica ejecutada:
- `./gradlew build` ✅

---

## 🗺️ Diagrama de Relaciones

```
AppUser ──────────────────────────────────────────────────────────────┐
   │                                                                   │
   │ createdBy / approvedBy                                            │
   ▼                                                                   │
Workspace ◄──────── WorkspaceMembership (workspace_id, user_id)       │
   │                       [role, status, joinedAt]                   │
   │                                                                   │
   │ workspace_id                                                      │
   ▼                                                                   │
WorkspaceTag ◄──── PageTagAssignment (page_id, workspace_id, tag_id)  │
                                                                       │
                                                                       │
AppUser ──────── ownerUser                                             │
   │                  │                                                │
   │                  ▼                                                │
   │            WikiPage ──────────────────────────────────────────┐  │
   │              │   │  publicUuid, title, slug                   │  │
   │              │   │  editMode, pageStatus                      │  │
   │              │   │  isEncrypted, isPublicable                 │  │
   │              │   │                                            │  │
   │              │   └──► currentRevisionId (FK nullable) ─────┐  │  │
   │              │                                              │  │  │
   │              │  page_id                                     │  │  │
   │              ▼                                              │  │  │
   │        WikiPageRevision ◄────────────────────────────────── ┘  │  │
   │              │   revisionNumber, titleSnapshot                  │  │
   │              │   editorType, contentHtml, contentText           │  │
   │              │   isEncrypted, contentCiphertext (bytea)         │  │
   │              │   contentIv, contentAuthTag, encryptionKdf       │  │
   │              │   isPinned                                       │  │
   │              │   createdBy → AppUser                            │  │
   │              │                                                  │  │
   │              └──► PageRevisionAssetRef (revision_id, asset_id)  │  │
   │                        [referenceType]                          │  │
   │                              │                                  │  │
   │                              ▼                                  │  │
   │                        StoredAsset ◄── PageAsset (page_id)      │  │
   │                                          [assetRole, sortOrder] │  │
   │                                                                  │  │
   │         page_id + workspace_id                                  │  │
   └──────► PageWorkspaceLink ◄────────────────────────────────────── ┘  │
                  [linkedAt, linkedBy]                                    │
                                                                          │
   WikiPage ──► PageHierarchy (parent_page_id, child_page_id, workspace_id)
                   [sortOrder] — árbol de páginas dentro de un workspace
                                                                          
   WikiPage + Workspace ──► PageComment (page_id, workspace_id)          │
                                 │ authorUser → AppUser                   │
                                 │ parentComment → PageComment (hilos)    │
                                 │                                        │
                                 └──► PageCommentReaction (comment_id)    │
                                          user → AppUser                  │
                                          [reactionType]                  │
                                                                          │
   WikiPage (1:1) ──► PageEncryptionMetadata                              │
                          [algorithm, scope, isSearchIndexed]             │
                          updatedBy → AppUser                             │
                                                                          │
   WikiPage + WikiPageRevision ──► PublicPagePublication                  │
                                       [publicSlug, publicHtml]           │
                                       publishedBy → AppUser              │
                                                                          │
   AppUser + Workspace + WikiPage ──► UserPageNavPreference               │
                                          [sortOrder, pinned, collapsed]  │
                                                                          │
   WikiPage / Workspace / StoredAsset / PageComment ──► TrashRecord       │
                                          deletedBy → AppUser             │
```

---

## 📦 Entidades y su rol

| Entidad | Rol |
|---|---|
| `AppUser` | Raíz. Todo parte de un usuario |
| `Workspace` | Contenedor de páginas y miembros |
| `WorkspaceMembership` | Controla quién pertenece a qué workspace y con qué rol |
| `WorkspaceTag` | Tags definidos dentro de un workspace |
| `WikiPage` | La página wiki en sí. Pertenece a un owner (AppUser) |
| `WikiPageRevision` | Versiones/revisiones del contenido de una WikiPage |
| `PageWorkspaceLink` | Tabla pivote que vincula una WikiPage a un Workspace |
| `PageHierarchy` | Define la estructura de árbol (padre/hijo) de páginas dentro de un workspace |
| `PageTagAssignment` | Asigna un WorkspaceTag a una WikiPage dentro de un workspace |
| `PageComment` | Comentarios en una página (con soporte a hilos via parentComment) |
| `PageCommentReaction` | Reacciones (emoji/tipo) a un comentario |
| `PageAsset` | Assets adjuntos a nivel de página (portada, adjuntos generales) |
| `PageRevisionAssetRef` | Assets referenciados dentro del contenido de una revisión específica |
| `StoredAsset` | El asset físico almacenado (S3, disco, etc.) |
| `PageEncryptionMetadata` | Metadatos de cifrado de una página (1:1 con WikiPage) |
| `PublicPagePublication` | Snapshot público de una revisión específica (blog/publicación) |
| `UserPageNavPreference` | Preferencias de navegación del sidebar por usuario/workspace/página |
| `TrashRecord` | Registro de eliminación lógica de páginas, workspaces, assets, comentarios |

---

## 🔄 Flujo de creación paso a paso

```
1. AppUser existe (auth/registro)
        │
        ▼
2. POST /api/v1/workspaces
   → Crea Workspace (createdBy = AppUser)
        │
        ▼
3. POST /api/v1/pages
   → Crea WikiPage (ownerUserId, sin currentRevisionId aún)
        │
        ▼
4. POST /api/v1/page-revisions
   → Crea WikiPageRevision (pageId, createdBy)
   → Si updatePagePointer=true, actualiza WikiPage.currentRevisionId automáticamente
        │
        ▼
5. POST /api/v1/pages/link-workspace
   → Crea PageWorkspaceLink (pageId + workspaceId)
   → La página ahora es visible dentro del workspace
        │
        ├─► (Opcional) POST /api/v1/tags → Crear WorkspaceTag
        │       └─► Asignar PageTagAssignment
        │
        ├─► (Opcional) Crear PageHierarchy → Árbol padre/hijo
        │
        ├─► (Opcional) POST /api/v1/assets → Subir StoredAsset
        │       └─► Crear PageAsset o PageRevisionAssetRef
        │
        ├─► (Opcional) POST /api/v1/comments → PageComment
        │       └─► PageCommentReaction
        │
        ├─► (Opcional) Publicar → PublicPagePublication
        │
        └─► (Opcional) Eliminar → TrashRecord
```

---

## 🔑 Relación circular WikiPage ↔ WikiPageRevision

`WikiPage.currentRevisionId` → `WikiPageRevision` (nullable, FK)  
`WikiPageRevision.page_id` → `WikiPage` (not null, FK)

Esto es una **referencia circular controlada**:
- La página puede existir sin revisión (borrador vacío)
- La revisión siempre necesita una página
- El puntero `currentRevisionId` se actualiza con cada nueva revisión aprobada (flag `updatePagePointer=true`)
