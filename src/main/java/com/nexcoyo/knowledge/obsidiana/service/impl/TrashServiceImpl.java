package com.nexcoyo.knowledge.obsidiana.service.impl;

import com.nexcoyo.knowledge.obsidiana.entity.AppUser;
import com.nexcoyo.knowledge.obsidiana.entity.RestoreAudit;
import com.nexcoyo.knowledge.obsidiana.entity.TrashRecord;
import com.nexcoyo.knowledge.obsidiana.repository.AppUserRepository;
import com.nexcoyo.knowledge.obsidiana.repository.RestoreAuditRepository;
import com.nexcoyo.knowledge.obsidiana.repository.TrashRecordRepository;
import com.nexcoyo.knowledge.obsidiana.service.TrashService;
import com.nexcoyo.knowledge.obsidiana.service.dto.search.TrashRecordSearchCriteria;
import com.nexcoyo.knowledge.obsidiana.service.specification.TrashRecordSpecifications;
import com.nexcoyo.knowledge.obsidiana.util.enums.TrashStatus;
import jakarta.persistence.EntityNotFoundException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrashServiceImpl implements TrashService {

    private final TrashRecordRepository trashRecordRepository;
    private final RestoreAuditRepository restoreAuditRepository;
    private final AppUserRepository appUserRepository;

    @Override
    public Page< TrashRecord > search( TrashRecordSearchCriteria criteria, Pageable pageable) {
        return trashRecordRepository.findAll(TrashRecordSpecifications.byCriteria(criteria), pageable);
    }

    @Override
    @Transactional
    public TrashRecord moveToTrash(TrashRecord trashRecord) {
        if (trashRecord.getDeletedAt() == null) {
            trashRecord.setDeletedAt(OffsetDateTime.now());
        }
        return trashRecordRepository.save(trashRecord);
    }

    @Override
    public TrashRecord getRequired(UUID trashRecordId) {
        return trashRecordRepository.findById(trashRecordId)
            .orElseThrow(() -> new EntityNotFoundException("Trash record not found: " + trashRecordId));
    }

    @Override
    @Transactional
    public RestoreAudit restore( UUID trashRecordId, UUID restoredBy, String reason) {
        TrashRecord trashRecord = getRequired(trashRecordId);
        AppUser actor = appUserRepository.findById(restoredBy)
                                         .orElseThrow(() -> new EntityNotFoundException("User not found: " + restoredBy));

        trashRecord.setStatus( TrashStatus.RESTORED);
        trashRecord.setRestoredAt(OffsetDateTime.now());
        trashRecordRepository.save(trashRecord);

        RestoreAudit audit = new RestoreAudit();
        audit.setTrashRecord(trashRecord);
        audit.setEntityType(trashRecord.getEntityType());
        audit.setEntityId(trashRecord.getEntityId());
        audit.setRestoredBy(actor);
        audit.setRestoreReason(reason);
        audit.setRestoredAt(OffsetDateTime.now());
        return restoreAuditRepository.save(audit);
    }

    @Override
    public List<TrashRecord> findOverdueTrash() {
        return trashRecordRepository.findAllByStatusAndRestoreDeadlineAtBefore(TrashStatus.TRASHED, OffsetDateTime.now());
    }
}
