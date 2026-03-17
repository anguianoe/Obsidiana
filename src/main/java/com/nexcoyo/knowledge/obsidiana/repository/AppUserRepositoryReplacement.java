package com.nexcoyo.knowledge.obsidiana.repository;

/*
 Reference patch only. Your previous AppUserRepository already works for this bundle.
 If you want a ready @Query helper for richer user cards, replace it with:

 public interface AppUserRepository extends JpaRepository<AppUser, UUID>, JpaSpecificationExecutor<AppUser> {
     Optional<AppUser> findByEmail(String email);
     Optional<AppUser> findByUsername(String username);
     boolean existsByEmail(String email);
     boolean existsByUsername(String username);

     @Query("""
             select u
             from AppUser u
             where u.id = :userId
             """)
     Optional<AppUser> findDetailedById(@Param("userId") UUID userId);
 }
 */
public final class AppUserRepositoryReplacement {
    private AppUserRepositoryReplacement() {
    }
}
