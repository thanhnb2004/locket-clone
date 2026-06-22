package com.locket.clone.friendship.dto;

import com.locket.clone.friendship.entity.FriendRequestStatus;
import com.locket.clone.friendship.entity.Friendship;
import com.locket.clone.user.dto.UserResponse;

import java.time.Instant;
import java.util.UUID;

public record FriendRequestResponse(
        UUID id,
        UserResponse requester,
        UserResponse addressee,
        FriendRequestStatus status,
        Instant createdAt
) {
    public static FriendRequestResponse from(Friendship f) {
        return new FriendRequestResponse(
                f.getId(),
                UserResponse.from(f.getRequester()),
                UserResponse.from(f.getAddressee()),
                f.getStatus(),
                f.getCreatedAt()
        );
    }
}
