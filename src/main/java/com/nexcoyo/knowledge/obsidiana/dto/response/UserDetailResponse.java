package com.nexcoyo.knowledge.obsidiana.dto.response;

import java.util.List;

public record UserDetailResponse(
        UserSummaryResponse user,
        UserProfileResponse profile,
        UserPreferenceResponse preference,
        List<UserWorkspaceMembershipResponse> workspaceMemberships,
        List<UserTagAssignmentResponse> userTags
) {
}
