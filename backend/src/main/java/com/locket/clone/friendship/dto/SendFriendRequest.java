package com.locket.clone.friendship.dto;

import jakarta.validation.constraints.NotBlank;

public record SendFriendRequest(
        @NotBlank String username
) {
}
