package com.nexcoyo.knowledge.obsidiana.repository;

import java.util.Optional;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.entity.AppUser;
import com.nexcoyo.knowledge.obsidiana.util.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AppUserRepository extends JpaRepository<AppUser, UUID>, JpaSpecificationExecutor< AppUser > {
    Optional<AppUser> findByEmailAndStatus(String email, UserStatus status);
    Optional<AppUser> findByUsernameAndStatus(String username, UserStatus status);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
