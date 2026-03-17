DROP SCHEMA IF EXISTS obsidiana;

CREATE SCHEMA IF NOT EXISTS obsidiana;

SET search_path TO obsidiana;

-- =========================================================
-- EXTENSIONS
-- =========================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- =========================================================
-- FUNCTIONS
-- =========================================================

CREATE OR REPLACE FUNCTION obsidiana.set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION obsidiana.prevent_encrypted_page_publication()
RETURNS TRIGGER AS $$
DECLARE
v_is_encrypted BOOLEAN;
    v_is_publicable BOOLEAN;
BEGIN
SELECT is_encrypted, is_publicable
INTO v_is_encrypted, v_is_publicable
FROM obsidiana.wiki_page
WHERE id = NEW.page_id;

IF v_is_encrypted THEN
        RAISE EXCEPTION 'Encrypted pages cannot be published publicly';
END IF;

    IF v_is_publicable = FALSE THEN
        RAISE EXCEPTION 'This page is not publicable';
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION obsidiana.validate_workspace_approval()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.kind = 'PRIVATE' THEN
        NEW.approval_status := 'APPROVED';
        NEW.approved_at := COALESCE(NEW.approved_at, NOW());
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION obsidiana.validate_page_revision_encryption()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.is_encrypted = FALSE THEN
        IF NEW.content_html IS NULL THEN
            RAISE EXCEPTION 'Non-encrypted revision must have content_html';
END IF;

        IF NEW.content_ciphertext IS NOT NULL
           OR NEW.content_iv IS NOT NULL
           OR NEW.content_auth_tag IS NOT NULL THEN
            RAISE EXCEPTION 'Non-encrypted revision cannot have encrypted payload fields';
END IF;
ELSE
        IF NEW.content_html IS NOT NULL THEN
            RAISE EXCEPTION 'Encrypted revision cannot store content_html in plain text';
END IF;

        IF NEW.content_ciphertext IS NULL
           OR NEW.content_iv IS NULL
           OR NEW.content_auth_tag IS NULL THEN
            RAISE EXCEPTION 'Encrypted revision requires ciphertext, iv, and auth_tag';
END IF;
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION obsidiana.ensure_single_live_publication()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.publication_status = 'LIVE' THEN
UPDATE obsidiana.public_page_publication
SET publication_status = 'UNPUBLISHED',
    unpublished_at = NOW()
WHERE page_id = NEW.page_id
  AND id <> COALESCE(NEW.id, uuid_generate_v4())
  AND publication_status = 'LIVE';
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION obsidiana.validate_page_hierarchy_not_self()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.parent_page_id = NEW.child_page_id THEN
        RAISE EXCEPTION 'A page cannot be parent/child of itself';
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION obsidiana.validate_trash_restore_target()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.entity_type NOT IN ('PAGE', 'ASSET', 'COMMENT', 'WORKSPACE') THEN
        RAISE EXCEPTION 'Unsupported trash entity_type: %', NEW.entity_type;
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- =========================================================
-- USERS
-- =========================================================

CREATE TABLE app_user (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          email VARCHAR(255) NOT NULL UNIQUE,
                          username VARCHAR(120) NOT NULL UNIQUE,
                          password_hash TEXT NOT NULL,
                          roles VARCHAR(150) NULL,
                          system_role VARCHAR(30) NOT NULL DEFAULT 'USER',
                          status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
                          first_login_at TIMESTAMPTZ NULL,
                          has_completed_onboarding BOOLEAN NOT NULL DEFAULT FALSE,
                          onboarding_version VARCHAR(20) NULL,
                          last_login_at TIMESTAMPTZ NULL,
                          created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                          updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                          deleted_at TIMESTAMPTZ NULL,
                          CONSTRAINT chk_app_user_system_role
                              CHECK (system_role IN ('USER', 'SUPER_ADMIN')),
                          CONSTRAINT chk_app_user_status
                              CHECK (status IN ('ACTIVE', 'BLOCKED', 'DELETED'))
);

CREATE TABLE user_profile (
                              user_id UUID PRIMARY KEY REFERENCES app_user(id) ON DELETE CASCADE,
                              display_name VARCHAR(255) NULL,
                              avatar_asset_id UUID NULL,
                              bio TEXT NULL,
                              locale VARCHAR(20) NULL,
                              timezone VARCHAR(100) NULL,
                              city VARCHAR(120) NULL,
                              region VARCHAR(120) NULL,
                              country VARCHAR(120) NULL,
                              created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                              updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE user_preference (
                                 user_id UUID PRIMARY KEY REFERENCES app_user(id) ON DELETE CASCADE,
                                 theme VARCHAR(30) NULL,
                                 sidebar_collapsed BOOLEAN NOT NULL DEFAULT FALSE,
                                 show_private_first BOOLEAN NOT NULL DEFAULT TRUE,
                                 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                 updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- =========================================================
-- AUTH / SESSION
-- =========================================================

CREATE TABLE user_session (
                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
                              refresh_token_hash TEXT NOT NULL,
                              session_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
                              ip_address VARCHAR(64) NULL,
                              user_agent TEXT NULL,
                              device_type VARCHAR(60) NULL,
                              os_name VARCHAR(120) NULL,
                              browser_name VARCHAR(120) NULL,
                              city_name VARCHAR(120) NULL,
                              region_name VARCHAR(120) NULL,
                              country_name VARCHAR(120) NULL,
                              login_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                              expires_at TIMESTAMPTZ NULL,
                              revoked_at TIMESTAMPTZ NULL,
                              created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                              CONSTRAINT chk_user_session_status
                                  CHECK (session_status IN ('ACTIVE', 'REVOKED', 'EXPIRED'))
);

CREATE INDEX idx_user_session_user_id ON user_session(user_id);
CREATE INDEX idx_user_session_status ON user_session(session_status);

create table obsidiana.refresh_tokens (
                                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                          user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
                                          token_hash varchar(128) not null unique,
                                          issued_at timestamptz not null,
                                          expires_at timestamptz not null,
                                          revoked_at timestamptz null,
                                          rotated_to uuid null,
                                          ip varchar(64) null,
                                          user_agent varchar(512) null
);

create index refresh_tokens_user_id_idx on refresh_tokens(user_id);
create index refresh_tokens_expires_idx on refresh_tokens(expires_at);

create table obsidiana.password_reset_tokens (
                                                 id uuid primary key,
                                                 user_id uuid not null references app_user(id) on delete cascade,
                                                 token_hash varchar(128) not null unique,
                                                 issued_at timestamptz not null,
                                                 expires_at timestamptz not null,
                                                 used_at timestamptz null
);

create index password_reset_tokens_user_id_idx on password_reset_tokens(user_id);
create index password_reset_tokens_expires_idx on password_reset_tokens(expires_at);

-- =========================================================
-- WORKSPACES
-- =========================================================

CREATE TABLE workspace (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           name VARCHAR(200) NOT NULL,
                           slug VARCHAR(160) NOT NULL UNIQUE,
                           kind VARCHAR(30) NOT NULL,
                           status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
                           approval_status VARCHAR(30) NOT NULL DEFAULT 'APPROVED',
                           created_by UUID NOT NULL REFERENCES app_user(id),
                           approved_by UUID NULL REFERENCES app_user(id),
                           approved_at TIMESTAMPTZ NULL,
                           description TEXT NULL,
                           created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                           updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                           deleted_at TIMESTAMPTZ NULL,
                           CONSTRAINT chk_workspace_kind
                               CHECK (kind IN ('PRIVATE', 'GROUP')),
                           CONSTRAINT chk_workspace_status
                               CHECK (status IN ('ACTIVE', 'INACTIVE', 'PENDING', 'ARCHIVED')),
                           CONSTRAINT chk_workspace_approval_status
                               CHECK (approval_status IN ('APPROVED', 'PENDING', 'REJECTED'))
);

CREATE TABLE workspace_membership (
                                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                      workspace_id UUID NOT NULL REFERENCES workspace(id) ON DELETE CASCADE,
                                      user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
                                      role VARCHAR(30) NOT NULL,
                                      status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
                                      joined_at TIMESTAMPTZ NULL,
                                      invited_at TIMESTAMPTZ NULL,
                                      created_by UUID NULL REFERENCES app_user(id),
                                      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                      updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                      UNIQUE(workspace_id, user_id),
                                      CONSTRAINT chk_workspace_membership_role
                                          CHECK (role IN ('OWNER', 'ADMIN', 'EDITOR', 'VIEWER')),
                                      CONSTRAINT chk_workspace_membership_status
                                          CHECK (status IN ('ACTIVE', 'INVITED', 'REMOVED'))
);

CREATE INDEX idx_workspace_membership_workspace_id ON workspace_membership(workspace_id);
CREATE INDEX idx_workspace_membership_user_id ON workspace_membership(user_id);

CREATE TABLE workspace_invitation (
                                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                      workspace_id UUID NOT NULL REFERENCES workspace(id) ON DELETE CASCADE,
                                      invited_email VARCHAR(255) NOT NULL,
                                      invited_user_id UUID NULL REFERENCES app_user(id) ON DELETE SET NULL,
                                      role VARCHAR(30) NOT NULL,
                                      invitation_token_hash TEXT NOT NULL,
                                      status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
                                      invited_by UUID NOT NULL REFERENCES app_user(id),
                                      expires_at TIMESTAMPTZ NOT NULL,
                                      accepted_at TIMESTAMPTZ NULL,
                                      rejected_at TIMESTAMPTZ NULL,
                                      revoked_at TIMESTAMPTZ NULL,
                                      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                      updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                      CONSTRAINT chk_workspace_invitation_role
                                          CHECK (role IN ('OWNER', 'ADMIN', 'EDITOR', 'VIEWER')),
                                      CONSTRAINT chk_workspace_invitation_status
                                          CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'REVOKED', 'EXPIRED'))
);

CREATE INDEX idx_workspace_invitation_workspace_id ON workspace_invitation(workspace_id);
CREATE INDEX idx_workspace_invitation_invited_email ON workspace_invitation(invited_email);
CREATE INDEX idx_workspace_invitation_status ON workspace_invitation(status);

-- =========================================================
-- WIKI PAGE
-- =========================================================

CREATE TABLE wiki_page (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           public_uuid UUID NOT NULL DEFAULT gen_random_uuid(),
                           owner_user_id UUID NOT NULL REFERENCES app_user(id),
                           title VARCHAR(255) NOT NULL,
                           slug VARCHAR(180) NOT NULL,
                           edit_mode VARCHAR(30) NOT NULL DEFAULT 'SHARED',
                           page_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
                           is_encrypted BOOLEAN NOT NULL DEFAULT FALSE,
                           is_publicable BOOLEAN NOT NULL DEFAULT TRUE,
                           current_revision_id UUID NULL,
                           created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                           updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                           deleted_at TIMESTAMPTZ NULL,
                           CONSTRAINT chk_wiki_page_edit_mode
                               CHECK (edit_mode IN ('SHARED', 'OWNER_ONLY')),
                           CONSTRAINT chk_wiki_page_status
                               CHECK (page_status IN ('ACTIVE', 'ARCHIVED', 'DELETED'))
);

CREATE INDEX idx_wiki_page_owner_user_id ON wiki_page(owner_user_id);
CREATE UNIQUE INDEX uq_wiki_page_public_uuid ON wiki_page(public_uuid);

CREATE TABLE page_workspace_link (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                     page_id UUID NOT NULL REFERENCES wiki_page(id) ON DELETE CASCADE,
                                     workspace_id UUID NOT NULL REFERENCES workspace(id) ON DELETE CASCADE,
                                     linked_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                     linked_by UUID NULL REFERENCES app_user(id),
                                     UNIQUE(page_id, workspace_id)
);

CREATE INDEX idx_page_workspace_link_page_id ON page_workspace_link(page_id);
CREATE INDEX idx_page_workspace_link_workspace_id ON page_workspace_link(workspace_id);
CREATE INDEX idx_page_workspace_link_workspace_page ON page_workspace_link(workspace_id, page_id);

CREATE TABLE page_hierarchy (
                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                parent_page_id UUID NOT NULL REFERENCES wiki_page(id) ON DELETE CASCADE,
                                child_page_id UUID NOT NULL REFERENCES wiki_page(id) ON DELETE CASCADE,
                                workspace_id UUID NOT NULL REFERENCES workspace(id) ON DELETE CASCADE,
                                sort_order INT NOT NULL DEFAULT 0,
                                created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                UNIQUE(parent_page_id, child_page_id, workspace_id)
);

CREATE INDEX idx_page_hierarchy_parent_page_id ON page_hierarchy(parent_page_id);
CREATE INDEX idx_page_hierarchy_child_page_id ON page_hierarchy(child_page_id);

-- =========================================================
-- PAGE REVISION
-- =========================================================

CREATE TABLE wiki_page_revision (
                                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                    page_id UUID NOT NULL REFERENCES wiki_page(id) ON DELETE CASCADE,
                                    revision_number INT NOT NULL,
                                    title_snapshot VARCHAR(255) NOT NULL,
                                    editor_type VARCHAR(30) NOT NULL DEFAULT 'CKEDITOR',
                                    content_html TEXT NULL,
                                    content_text TEXT NULL,
                                    change_summary VARCHAR(500) NULL,
                                    is_encrypted BOOLEAN NOT NULL DEFAULT FALSE,
                                    content_ciphertext BYTEA NULL,
                                    content_iv VARCHAR(255) NULL,
                                    content_auth_tag VARCHAR(255) NULL,
                                    encryption_kdf VARCHAR(255) NULL,
                                    is_pinned BOOLEAN NOT NULL DEFAULT FALSE,
                                    created_by UUID NOT NULL REFERENCES app_user(id),
                                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                    UNIQUE(page_id, revision_number),
                                    CONSTRAINT chk_wiki_page_revision_editor_type
                                        CHECK (editor_type IN ('CKEDITOR'))
);

CREATE INDEX idx_wiki_page_revision_page_id ON wiki_page_revision(page_id);
CREATE INDEX idx_wiki_page_revision_created_at ON wiki_page_revision(created_at DESC);

ALTER TABLE wiki_page
    ADD CONSTRAINT fk_wiki_page_current_revision
        FOREIGN KEY (current_revision_id) REFERENCES wiki_page_revision(id);

-- =========================================================
-- PAGE ENCRYPTION METADATA
-- =========================================================

CREATE TABLE page_encryption_metadata (
                                          page_id UUID PRIMARY KEY REFERENCES wiki_page(id) ON DELETE CASCADE,
                                          encryption_algorithm VARCHAR(50) NOT NULL DEFAULT 'AES-256-GCM',
                                          encryption_scope VARCHAR(30) NOT NULL DEFAULT 'CONTENT',
                                          is_search_indexed BOOLEAN NOT NULL DEFAULT FALSE,
                                          public_publishing_blocked BOOLEAN NOT NULL DEFAULT TRUE,
                                          updated_by UUID NOT NULL REFERENCES app_user(id),
                                          updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                          CONSTRAINT chk_page_encryption_scope
                                              CHECK (encryption_scope IN ('CONTENT'))
);

-- =========================================================
-- PUBLIC PUBLICATION
-- =========================================================

CREATE TABLE public_page_publication (
                                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                         page_id UUID NOT NULL REFERENCES wiki_page(id) ON DELETE CASCADE,
                                         revision_id UUID NOT NULL REFERENCES wiki_page_revision(id) ON DELETE CASCADE,
                                         public_slug VARCHAR(180) NOT NULL UNIQUE,
                                         public_title VARCHAR(255) NOT NULL,
                                         public_html TEXT NOT NULL,
                                         publication_status VARCHAR(30) NOT NULL DEFAULT 'LIVE',
                                         published_by UUID NOT NULL REFERENCES app_user(id),
                                         published_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                         unpublished_at TIMESTAMPTZ NULL,
                                         UNIQUE(page_id, revision_id),
                                         CONSTRAINT chk_public_page_publication_status
                                             CHECK (publication_status IN ('LIVE', 'UNPUBLISHED'))
);

CREATE INDEX idx_public_page_publication_page_id ON public_page_publication(page_id);

CREATE UNIQUE INDEX uq_public_page_live_per_page
    ON public_page_publication(page_id)
    WHERE publication_status = 'LIVE';

CREATE TABLE public_page_asset (
                                   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                   publication_id UUID NOT NULL REFERENCES public_page_publication(id) ON DELETE CASCADE,
                                   asset_id UUID NOT NULL,
                                   asset_role VARCHAR(30) NOT NULL DEFAULT 'INLINE',
                                   created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                   CONSTRAINT chk_public_page_asset_role
                                       CHECK (asset_role IN ('INLINE', 'ATTACHMENT'))
);

CREATE INDEX idx_public_page_asset_publication_id ON public_page_asset(publication_id);

-- =========================================================
-- ASSETS
-- =========================================================

CREATE TABLE stored_asset (
                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              storage_provider VARCHAR(30) NOT NULL DEFAULT 'S3',
                              bucket_name VARCHAR(255) NOT NULL,
                              object_key TEXT NOT NULL,
                              original_filename VARCHAR(255) NOT NULL,
                              normalized_filename VARCHAR(255) NULL,
                              mime_type VARCHAR(180) NOT NULL,
                              asset_type VARCHAR(30) NOT NULL,
                              file_extension VARCHAR(30) NULL,
                              size_bytes BIGINT NOT NULL,
                              checksum_sha256 VARCHAR(64) NULL,
                              status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
                              uploaded_by UUID NULL REFERENCES app_user(id),
                              created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                              deleted_at TIMESTAMPTZ NULL,
                              CONSTRAINT chk_stored_asset_type
                                  CHECK (asset_type IN ('IMAGE', 'FILE', 'VIDEO', 'CODE')),
                              CONSTRAINT chk_stored_asset_status
                                  CHECK (status IN ('ACTIVE', 'ORPHAN_CANDIDATE', 'DELETED'))
);

CREATE INDEX idx_stored_asset_status ON stored_asset(status);
CREATE INDEX idx_stored_asset_uploaded_by ON stored_asset(uploaded_by);

CREATE TABLE page_asset (
                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            page_id UUID NOT NULL REFERENCES wiki_page(id) ON DELETE CASCADE,
                            asset_id UUID NOT NULL REFERENCES stored_asset(id) ON DELETE CASCADE,
                            asset_role VARCHAR(30) NOT NULL,
                            display_name VARCHAR(255) NULL,
                            sort_order INT NOT NULL DEFAULT 0,
                            created_by UUID NULL REFERENCES app_user(id),
                            created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                            UNIQUE(page_id, asset_id, asset_role),
                            CONSTRAINT chk_page_asset_role
                                CHECK (asset_role IN ('INLINE', 'ATTACHMENT', 'COVER', 'DOWNLOAD'))
);

CREATE INDEX idx_page_asset_page_id ON page_asset(page_id);
CREATE INDEX idx_page_asset_asset_id ON page_asset(asset_id);

CREATE TABLE page_revision_asset_ref (
                                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                         revision_id UUID NOT NULL REFERENCES wiki_page_revision(id) ON DELETE CASCADE,
                                         asset_id UUID NOT NULL REFERENCES stored_asset(id) ON DELETE CASCADE,
                                         reference_type VARCHAR(30) NOT NULL DEFAULT 'CONTENT',
                                         created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                         UNIQUE(revision_id, asset_id, reference_type),
                                         CONSTRAINT chk_page_revision_asset_ref_type
                                             CHECK (reference_type IN ('CONTENT', 'ATTACHMENT', 'INLINE'))
);

CREATE INDEX idx_page_revision_asset_ref_revision_id ON page_revision_asset_ref(revision_id);
CREATE INDEX idx_page_revision_asset_ref_asset_id ON page_revision_asset_ref(asset_id);

-- =========================================================
-- COMMENTS
-- =========================================================

CREATE TABLE page_comment (
                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              page_id UUID NOT NULL REFERENCES wiki_page(id) ON DELETE CASCADE,
                              workspace_id UUID NOT NULL REFERENCES workspace(id) ON DELETE CASCADE,
                              author_user_id UUID NOT NULL REFERENCES app_user(id),
                              parent_comment_id UUID NULL REFERENCES page_comment(id) ON DELETE CASCADE,
                              body TEXT NOT NULL,
                              comment_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
                              created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                              edited_at TIMESTAMPTZ NULL,
                              deleted_at TIMESTAMPTZ NULL,
                              CONSTRAINT chk_page_comment_status
                                  CHECK (comment_status IN ('ACTIVE', 'EDITED', 'DELETED'))
);

CREATE INDEX idx_page_comment_page_id ON page_comment(page_id);
CREATE INDEX idx_page_comment_workspace_id ON page_comment(workspace_id);
CREATE INDEX idx_page_comment_page_workspace ON page_comment(page_id, workspace_id);

CREATE TABLE page_comment_reaction (
                                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                       comment_id UUID NOT NULL REFERENCES page_comment(id) ON DELETE CASCADE,
                                       user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
                                       reaction_type VARCHAR(20) NOT NULL,
                                       created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                       UNIQUE(comment_id, user_id),
                                       CONSTRAINT chk_page_comment_reaction_type
                                           CHECK (reaction_type IN ('LIKE', 'DISLIKE'))
);

CREATE INDEX idx_page_comment_reaction_comment_id ON page_comment_reaction(comment_id);

-- =========================================================
-- TAGS
-- =========================================================

CREATE TABLE workspace_tag (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               workspace_id UUID NOT NULL REFERENCES workspace(id) ON DELETE CASCADE,
                               name VARCHAR(120) NOT NULL,
                               tag_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
                               created_by UUID NULL REFERENCES app_user(id),
                               created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                               UNIQUE(workspace_id, name),
                               CONSTRAINT chk_workspace_tag_status
                                   CHECK (tag_status IN ('ACTIVE', 'INACTIVE'))
);

CREATE TABLE page_tag_assignment (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                     page_id UUID NOT NULL REFERENCES wiki_page(id) ON DELETE CASCADE,
                                     workspace_id UUID NOT NULL REFERENCES workspace(id) ON DELETE CASCADE,
                                     tag_id UUID NOT NULL REFERENCES workspace_tag(id) ON DELETE CASCADE,
                                     assignment_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
                                     created_by UUID NULL REFERENCES app_user(id),
                                     created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                     UNIQUE(page_id, workspace_id, tag_id),
                                     CONSTRAINT chk_page_tag_assignment_status
                                         CHECK (assignment_status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX idx_page_tag_assignment_workspace_id ON page_tag_assignment(workspace_id);

CREATE TABLE user_tag_assignment (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                     target_user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
                                     workspace_id UUID NOT NULL REFERENCES workspace(id) ON DELETE CASCADE,
                                     tag_id UUID NOT NULL REFERENCES workspace_tag(id) ON DELETE CASCADE,
                                     assignment_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
                                     created_by UUID NULL REFERENCES app_user(id),
                                     created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                     UNIQUE(target_user_id, workspace_id, tag_id),
                                     CONSTRAINT chk_user_tag_assignment_status
                                         CHECK (assignment_status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX idx_user_tag_assignment_workspace_id ON user_tag_assignment(workspace_id);

-- =========================================================
-- NAVIGATION PREFERENCES
-- =========================================================

CREATE TABLE user_page_nav_preference (
                                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                          user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
                                          workspace_id UUID NOT NULL REFERENCES workspace(id) ON DELETE CASCADE,
                                          parent_page_id UUID NULL REFERENCES wiki_page(id) ON DELETE CASCADE,
                                          page_id UUID NOT NULL REFERENCES wiki_page(id) ON DELETE CASCADE,
                                          sort_order INT NOT NULL DEFAULT 0,
                                          pinned BOOLEAN NOT NULL DEFAULT FALSE,
                                          collapsed BOOLEAN NOT NULL DEFAULT FALSE,
                                          created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                          updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                          UNIQUE(user_id, workspace_id, page_id)
);

CREATE INDEX idx_user_page_nav_preference_user_id ON user_page_nav_preference(user_id);

-- =========================================================
-- AUDIT
-- =========================================================

CREATE TABLE audit_event (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             event_type VARCHAR(100) NOT NULL,
                             entity_type VARCHAR(100) NOT NULL,
                             entity_id UUID NULL,
                             actor_user_id UUID NULL REFERENCES app_user(id),
                             workspace_id UUID NULL REFERENCES workspace(id),
                             event_payload JSONB NULL,
                             created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_event_entity_type ON audit_event(entity_type);
CREATE INDEX idx_audit_event_actor_user_id ON audit_event(actor_user_id);
CREATE INDEX idx_audit_event_created_at ON audit_event(created_at DESC);

-- =========================================================
-- TRASH / RESTORE / SOFT DELETE AUDIT
-- =========================================================

CREATE TABLE trash_record (
                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              entity_type VARCHAR(30) NOT NULL,
                              entity_id UUID NOT NULL,
                              workspace_id UUID NULL REFERENCES workspace(id),
                              page_id UUID NULL REFERENCES wiki_page(id),
                              asset_id UUID NULL REFERENCES stored_asset(id),
                              comment_id UUID NULL REFERENCES page_comment(id),
                              deleted_by UUID NULL REFERENCES app_user(id),
                              delete_reason VARCHAR(255) NULL,
                              snapshot_payload JSONB NULL,
                              deleted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                              restore_deadline_at TIMESTAMPTZ NULL,
                              restored_at TIMESTAMPTZ NULL,
                              purge_scheduled_at TIMESTAMPTZ NULL,
                              status VARCHAR(30) NOT NULL DEFAULT 'TRASHED',
                              CONSTRAINT chk_trash_record_entity_type
                                  CHECK (entity_type IN ('PAGE', 'ASSET', 'COMMENT', 'WORKSPACE')),
                              CONSTRAINT chk_trash_record_status
                                  CHECK (status IN ('TRASHED', 'RESTORED', 'PURGED'))
);

CREATE INDEX idx_trash_record_entity ON trash_record(entity_type, entity_id);
CREATE INDEX idx_trash_record_status ON trash_record(status);
CREATE INDEX idx_trash_record_workspace_id ON trash_record(workspace_id);

CREATE TABLE restore_audit (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               trash_record_id UUID NOT NULL REFERENCES trash_record(id) ON DELETE CASCADE,
                               entity_type VARCHAR(30) NOT NULL,
                               entity_id UUID NOT NULL,
                               restored_by UUID NOT NULL REFERENCES app_user(id),
                               restore_reason VARCHAR(255) NULL,
                               restore_payload JSONB NULL,
                               restored_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                               CONSTRAINT chk_restore_audit_entity_type
                                   CHECK (entity_type IN ('PAGE', 'ASSET', 'COMMENT', 'WORKSPACE'))
);

CREATE INDEX idx_restore_audit_trash_record_id ON restore_audit(trash_record_id);
CREATE INDEX idx_restore_audit_entity ON restore_audit(entity_type, entity_id);

-- =========================================================
-- CLEANUP JOBS
-- =========================================================

CREATE TABLE cleanup_job (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             job_type VARCHAR(50) NOT NULL,
                             status VARCHAR(30) NOT NULL,
                             started_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                             finished_at TIMESTAMPTZ NULL,
                             total_scanned INT NOT NULL DEFAULT 0,
                             total_marked INT NOT NULL DEFAULT 0,
                             total_deleted INT NOT NULL DEFAULT 0,
                             error_message TEXT NULL,
                             triggered_by VARCHAR(30) NOT NULL DEFAULT 'SYSTEM',
                             created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                             CONSTRAINT chk_cleanup_job_type
                                 CHECK (job_type IN ('ORPHAN_ASSET_CLEANUP', 'TRASH_PURGE')),
                             CONSTRAINT chk_cleanup_job_status
                                 CHECK (status IN ('RUNNING', 'COMPLETED', 'FAILED', 'PARTIAL')),
                             CONSTRAINT chk_cleanup_job_triggered_by
                                 CHECK (triggered_by IN ('SYSTEM', 'MANUAL'))
);

CREATE TABLE cleanup_job_item (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  job_id UUID NOT NULL REFERENCES cleanup_job(id) ON DELETE CASCADE,
                                  asset_id UUID NULL REFERENCES stored_asset(id) ON DELETE CASCADE,
                                  trash_record_id UUID NULL REFERENCES trash_record(id) ON DELETE CASCADE,
                                  action VARCHAR(30) NOT NULL,
                                  reason VARCHAR(255) NULL,
                                  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                  CONSTRAINT chk_cleanup_job_item_action
                                      CHECK (action IN ('SCANNED', 'MARKED_ORPHAN', 'RESTORED', 'DELETED', 'SKIPPED', 'PURGED'))
    );

CREATE INDEX idx_cleanup_job_item_job_id ON cleanup_job_item(job_id);

-- =========================================================
-- LATE FOREIGN KEYS
-- =========================================================

ALTER TABLE user_profile
    ADD CONSTRAINT fk_user_profile_avatar_asset
        FOREIGN KEY (avatar_asset_id) REFERENCES stored_asset(id) ON DELETE SET NULL;

ALTER TABLE public_page_asset
    ADD CONSTRAINT fk_public_page_asset_asset
        FOREIGN KEY (asset_id) REFERENCES stored_asset(id) ON DELETE CASCADE;

-- =========================================================
-- TRIGGERS
-- =========================================================

CREATE TRIGGER trg_app_user_updated_at
    BEFORE UPDATE ON app_user
    FOR EACH ROW
    EXECUTE FUNCTION obsidiana.set_updated_at();

CREATE TRIGGER trg_user_profile_updated_at
    BEFORE UPDATE ON user_profile
    FOR EACH ROW
    EXECUTE FUNCTION obsidiana.set_updated_at();

CREATE TRIGGER trg_user_preference_updated_at
    BEFORE UPDATE ON user_preference
    FOR EACH ROW
    EXECUTE FUNCTION obsidiana.set_updated_at();

CREATE TRIGGER trg_workspace_updated_at
    BEFORE UPDATE ON workspace
    FOR EACH ROW
    EXECUTE FUNCTION obsidiana.set_updated_at();

CREATE TRIGGER trg_workspace_membership_updated_at
    BEFORE UPDATE ON workspace_membership
    FOR EACH ROW
    EXECUTE FUNCTION obsidiana.set_updated_at();

CREATE TRIGGER trg_workspace_invitation_updated_at
    BEFORE UPDATE ON workspace_invitation
    FOR EACH ROW
    EXECUTE FUNCTION obsidiana.set_updated_at();

CREATE TRIGGER trg_wiki_page_updated_at
    BEFORE UPDATE ON wiki_page
    FOR EACH ROW
    EXECUTE FUNCTION obsidiana.set_updated_at();

CREATE TRIGGER trg_user_page_nav_preference_updated_at
    BEFORE UPDATE ON user_page_nav_preference
    FOR EACH ROW
    EXECUTE FUNCTION obsidiana.set_updated_at();

CREATE TRIGGER trg_validate_workspace_approval
    BEFORE INSERT OR UPDATE ON workspace
                         FOR EACH ROW
                         EXECUTE FUNCTION obsidiana.validate_workspace_approval();

CREATE TRIGGER trg_validate_page_revision_encryption
    BEFORE INSERT OR UPDATE ON wiki_page_revision
                         FOR EACH ROW
                         EXECUTE FUNCTION obsidiana.validate_page_revision_encryption();

CREATE TRIGGER trg_prevent_encrypted_page_publication
    BEFORE INSERT OR UPDATE ON public_page_publication
                         FOR EACH ROW
                         EXECUTE FUNCTION obsidiana.prevent_encrypted_page_publication();

CREATE TRIGGER trg_ensure_single_live_publication
    BEFORE INSERT OR UPDATE ON public_page_publication
                         FOR EACH ROW
                         EXECUTE FUNCTION obsidiana.ensure_single_live_publication();

CREATE TRIGGER trg_validate_page_hierarchy_not_self
    BEFORE INSERT OR UPDATE ON page_hierarchy
                         FOR EACH ROW
                         EXECUTE FUNCTION obsidiana.validate_page_hierarchy_not_self();

CREATE TRIGGER trg_validate_trash_restore_target
    BEFORE INSERT OR UPDATE ON trash_record
                         FOR EACH ROW
                         EXECUTE FUNCTION obsidiana.validate_trash_restore_target();

-- =========================================================
-- COMMENTS
-- =========================================================

COMMENT ON TABLE wiki_page IS 'Main logical wiki page entity';
COMMENT ON TABLE wiki_page_revision IS 'Revision history of each wiki page';
COMMENT ON TABLE public_page_publication IS 'Sanitized public projection of a page';
COMMENT ON TABLE stored_asset IS 'Physical asset metadata stored in S3 or compatible provider';
COMMENT ON TABLE page_comment IS 'Workspace-contextualized comments for wiki pages';
COMMENT ON TABLE workspace_invitation IS 'Invitation flow for joining workspaces';
COMMENT ON TABLE trash_record IS 'Logical trash registry for recoverable deleted entities';
COMMENT ON TABLE restore_audit IS 'Audit trail for restore actions from trash';