package com.nexcoyo.knowledge.obsidiana.service;


import com.nexcoyo.knowledge.obsidiana.dto.request.UpsertUserPreferenceRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UpsertUserProfileRequest;
import com.nexcoyo.knowledge.obsidiana.entity.UserPreference;
import com.nexcoyo.knowledge.obsidiana.entity.UserProfile;

import java.util.UUID;

public interface UserProfileService {

    UserProfile getOrCreateProfile( UUID userId);

    UserPreference getOrCreatePreference(UUID userId);

    UserProfile updateProfile(UUID userId, UpsertUserProfileRequest request);

    UserPreference updatePreference( UUID userId, UpsertUserPreferenceRequest request);
}
