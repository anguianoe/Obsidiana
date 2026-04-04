package com.nexcoyo.knowledge.obsidiana.controller;

import com.nexcoyo.knowledge.obsidiana.dto.request.UpdateMyProfileRequest;
import com.nexcoyo.knowledge.obsidiana.dto.response.MyProfileResponse;
import com.nexcoyo.knowledge.obsidiana.facade.UserProfileFacade;
import com.nexcoyo.knowledge.obsidiana.service.GeneralService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class UserProfileController {

    private final UserProfileFacade userProfileFacade;
    private final GeneralService generalService;

    @GetMapping("/profile")
    public MyProfileResponse getProfile( ) {
        UUID userId = generalService.getIdUserFromSession();
        return userProfileFacade.getMyProfile(userId);
    }

    @PutMapping("/profile")
    public MyProfileResponse updateProfile(
            @Valid @RequestBody UpdateMyProfileRequest request
    ) {
        UUID userId = generalService.getIdUserFromSession();
        return userProfileFacade.updateMyProfile(userId, request);
    }
}
