package com.nexcoyo.knowledge.obsidiana.repository;

import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.entity.PageEncryptionMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PageEncryptionMetadataRepository extends JpaRepository< PageEncryptionMetadata, UUID> {
}
