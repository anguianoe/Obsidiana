package com.nexcoyo.knowledge.obsidiana.service;

import java.util.List;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.entity.RestoreAudit;
import com.nexcoyo.knowledge.obsidiana.entity.TrashRecord;
import com.nexcoyo.knowledge.obsidiana.service.dto.search.TrashRecordSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TrashService {
    Page<TrashRecord> search( TrashRecordSearchCriteria criteria, Pageable pageable);
    TrashRecord moveToTrash(TrashRecord trashRecord);
    TrashRecord getRequired(UUID trashRecordId);
    RestoreAudit restore( UUID trashRecordId, UUID restoredBy, String reason);
    List< TrashRecord > findOverdueTrash();

    Page<TrashRecord> search( TrashRecordSearchCriteria criteria, Pageable pageable, UUID userId);
    TrashRecord moveToTrash(TrashRecord trashRecord,UUID userId);
    TrashRecord getRequired(UUID trashRecordId,UUID userId);
    RestoreAudit restore( UUID trashRecordId, UUID restoredBy, String reason, UUID userId);
    List< TrashRecord > findOverdueTrash(UUID userId);


}
