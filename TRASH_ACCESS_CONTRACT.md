# Trash Access Contract

## Scope
- `TrashController`
- `TrashAdminController`

## USER (`/api/v1/trash`)

### `GET /api/v1/trash`
Returns trash records when at least one of these is true:
- `deletedBy == sessionUserId`
- active relation through `WorkspaceMembership`
- accessible related `WikiPage`
- authored related `PageComment`
- uploaded related `StoredAsset`

### `GET /api/v1/trash/{trashRecordId}`
Returns only records where:
- `deletedBy == sessionUserId`

### `POST /api/v1/trash`
Can move to trash only resources that the session user can operate on:
- own/accessible workspace
- own/accessible wiki page
- own/accessible comment
- uploaded asset or asset linked to accessible page/workspace/comment

Audit actor:
- `deletedBy` is always derived from session.

### `POST /api/v1/trash/{trashRecordId}/restore`
Can restore when at least one is true:
- `deletedBy == sessionUserId`
- active relation through workspace membership
- accessible related wiki page
- authored related comment
- uploaded related asset

Audit actor:
- `restoredBy` is always derived from session.

### `GET /api/v1/trash/overdue`
Returns only records where:
- `deletedBy == sessionUserId`
- `status == TRASHED`
- `restoreDeadlineAt < now`

## ADMIN (`/api/v1/admin/trash`)
All endpoints are unrestricted by ownership/membership rules and remain protected by `SUPER_ADMIN` role.

Actor fields:
- admin requests may still provide explicit `deletedBy` / `restoredBy` for audit operations.

