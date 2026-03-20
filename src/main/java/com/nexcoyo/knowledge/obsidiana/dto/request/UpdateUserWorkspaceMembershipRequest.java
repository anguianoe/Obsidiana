package com.nexcoyo.knowledge.obsidiana.dto.request;

public record UpdateUserWorkspaceMembershipRequest(
        String role,
        String status
) {
}
