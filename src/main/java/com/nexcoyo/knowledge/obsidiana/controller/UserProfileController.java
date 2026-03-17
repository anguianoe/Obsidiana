package com.nexcoyo.knowledge.obsidiana.controller;

import com.nexcoyo.knowledge.obsidiana.dto.request.UpdateMyProfileRequest;
import com.nexcoyo.knowledge.obsidiana.dto.response.MyProfileResponse;
import com.nexcoyo.knowledge.obsidiana.facade.UserProfileFacade;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileFacade userProfileFacade;

    @GetMapping("/{userId}/profile")
    public MyProfileResponse getProfile( @PathVariable UUID userId) {
        return userProfileFacade.getMyProfile(userId);
    }

    @PutMapping("/{userId}/profile")
    public MyProfileResponse updateProfile(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateMyProfileRequest request
    ) {
        return userProfileFacade.updateMyProfile(userId, request);
    }
}
