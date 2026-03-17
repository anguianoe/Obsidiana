package com.nexcoyo.knowledge.obsidiana.service.impl;

import com.nexcoyo.knowledge.obsidiana.dto.request.UpsertUserPreferenceRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UpsertUserProfileRequest;
import com.nexcoyo.knowledge.obsidiana.entity.AppUser;
import com.nexcoyo.knowledge.obsidiana.entity.StoredAsset;
import com.nexcoyo.knowledge.obsidiana.entity.UserPreference;
import com.nexcoyo.knowledge.obsidiana.entity.UserProfile;
import com.nexcoyo.knowledge.obsidiana.repository.AppUserRepository;
import com.nexcoyo.knowledge.obsidiana.repository.UserPreferenceRepository;
import com.nexcoyo.knowledge.obsidiana.repository.UserProfileRepository;
import com.nexcoyo.knowledge.obsidiana.service.UserProfileService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProfileServiceImpl implements UserProfileService
{

    private final UserProfileRepository userProfileRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final AppUserRepository appUserRepository;
    private final EntityManager entityManager;

    @Override
    public UserProfile getOrCreateProfile( UUID userId) {
        return userProfileRepository.findById(userId)
                .orElseGet(() -> createEmptyProfile(requireUser(userId)));
    }

    @Override
    public UserPreference getOrCreatePreference( UUID userId) {
        return userPreferenceRepository.findById(userId)
                .orElseGet(() -> createDefaultPreference(requireUser(userId)));
    }

    @Override
    @Transactional
    public UserProfile updateProfile(UUID userId, UpsertUserProfileRequest request) {
        UserProfile profile = getOrCreateProfile(userId);

        if (request == null) {
            return profile;
        }

        profile.setDisplayName(request.displayName());
        profile.setBio(request.bio());
        profile.setLocale(request.locale());
        profile.setTimezone(request.timezone());
        profile.setCity(request.city());
        profile.setRegion(request.region());
        profile.setCountry(request.country());
        profile.setAvatarAsset(resolveAsset(request.avatarAssetId()));

        return userProfileRepository.save(profile);
    }

    @Override
    @Transactional
    public UserPreference updatePreference(UUID userId, UpsertUserPreferenceRequest request) {
        UserPreference preference = getOrCreatePreference(userId);

        if (request == null) {
            return preference;
        }

        preference.setTheme(request.theme());
        preference.setSidebarCollapsed(Boolean.TRUE.equals(request.sidebarCollapsed()));
        preference.setShowPrivateFirst(request.showPrivateFirst() == null || request.showPrivateFirst());

        return userPreferenceRepository.save(preference);
    }

    private AppUser requireUser( UUID userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
    }

    private UserProfile createEmptyProfile(AppUser user) {
        UserProfile profile = new UserProfile();
        profile.setUser(user);
        return userProfileRepository.save(profile);
    }

    private UserPreference createDefaultPreference(AppUser user) {
        UserPreference preference = new UserPreference();
        preference.setUser(user);
        preference.setSidebarCollapsed(false);
        preference.setShowPrivateFirst(true);
        return userPreferenceRepository.save(preference);
    }

    private StoredAsset resolveAsset( UUID assetId) {
        if (assetId == null) {
            return null;
        }
        return entityManager.getReference(StoredAsset.class, assetId);
    }
}
