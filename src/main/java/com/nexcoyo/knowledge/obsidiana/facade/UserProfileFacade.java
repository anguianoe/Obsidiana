package com.nexcoyo.knowledge.obsidiana.facade;

import com.nexcoyo.knowledge.obsidiana.dto.request.UpdateMyProfileRequest;
import com.nexcoyo.knowledge.obsidiana.dto.response.MyProfileResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.UserPreferenceResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.UserProfileResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.UserSummaryResponse;
import com.nexcoyo.knowledge.obsidiana.entity.AppUser;
import com.nexcoyo.knowledge.obsidiana.entity.UserPreference;
import com.nexcoyo.knowledge.obsidiana.entity.UserProfile;
import com.nexcoyo.knowledge.obsidiana.repository.AppUserRepository;
import com.nexcoyo.knowledge.obsidiana.service.UserProfileService;
import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProfileFacade {

    private final AppUserRepository appUserRepository;
    private final UserProfileService userProfileService;

    public MyProfileResponse getMyProfile( UUID userId) {
        AppUser user = appUserRepository.findById(userId)
                                        .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        UserProfile profile = userProfileService.getOrCreateProfile(userId);
        UserPreference preference = userProfileService.getOrCreatePreference(userId);
        return new MyProfileResponse(toUser(user), toProfile(profile), toPreference(preference));
    }

    @Transactional
    public MyProfileResponse updateMyProfile(UUID userId, UpdateMyProfileRequest request) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        UserProfile profile = userProfileService.updateProfile(userId, request == null ? null : request.profile());
        UserPreference preference = userProfileService.updatePreference(userId, request == null ? null : request.preference());
        return new MyProfileResponse(toUser(user), toProfile(profile), toPreference(preference));
    }

    public UserSummaryResponse toUser( AppUser user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getSystemRole() == null ? null : user.getSystemRole().name(),
                user.getStatus() == null ? null : user.getStatus().name(),
                user.getFirstLoginAt(),
                user.getHasCompletedOnboarding(),
                user.getOnboardingVersion(),
                user.getLastLoginAt(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public UserProfileResponse toProfile(UserProfile profile) {
        return new UserProfileResponse(
                profile.getUser().getId(),
                profile.getDisplayName(),
                profile.getAvatarAsset() == null ? null : profile.getAvatarAsset().getId(),
                profile.getBio(),
                profile.getLocale(),
                profile.getTimezone(),
                profile.getCity(),
                profile.getRegion(),
                profile.getCountry(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }

    public UserPreferenceResponse toPreference(UserPreference preference) {
        return new UserPreferenceResponse(
                preference.getUser().getId(),
                preference.getTheme(),
                preference.getSidebarCollapsed(),
                preference.getShowPrivateFirst(),
                preference.getCreatedAt(),
                preference.getUpdatedAt()
        );
    }
}
