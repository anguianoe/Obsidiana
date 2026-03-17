package com.nexcoyo.knowledge.obsidiana.facade.support;


import com.nexcoyo.knowledge.obsidiana.entity.AppUser;
import com.nexcoyo.knowledge.obsidiana.entity.PageComment;
import com.nexcoyo.knowledge.obsidiana.entity.StoredAsset;
import com.nexcoyo.knowledge.obsidiana.entity.TrashRecord;
import com.nexcoyo.knowledge.obsidiana.entity.WikiPage;
import com.nexcoyo.knowledge.obsidiana.entity.WikiPageRevision;
import com.nexcoyo.knowledge.obsidiana.entity.Workspace;
import com.nexcoyo.knowledge.obsidiana.entity.WorkspaceTag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class EntityReferenceResolver {

    @PersistenceContext
    private EntityManager entityManager;

    public AppUser user( UUID id) { return reference(AppUser.class, id); }
    public Workspace workspace( UUID id) { return reference(Workspace.class, id); }
    public WikiPage page( UUID id) { return reference(WikiPage.class, id); }
    public WikiPageRevision revision( UUID id) { return reference(WikiPageRevision.class, id); }
    public StoredAsset asset( UUID id) { return reference(StoredAsset.class, id); }
    public WorkspaceTag tag( UUID id) { return reference(WorkspaceTag.class, id); }
    public PageComment comment( UUID id) { return reference(PageComment.class, id); }
    public TrashRecord trash( UUID id) { return reference(TrashRecord.class, id); }

    private <T> T reference(Class<T> type, UUID id) {
        if (id == null) {
            return null;
        }
        return entityManager.getReference(type, id);
    }
}
