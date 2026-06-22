package com.locket.clone.moment.dto;

import com.locket.clone.moment.entity.Moment;
import com.locket.clone.user.dto.UserResponse;

import java.time.Instant;
import java.util.UUID;

public record MomentResponse(
        UUID id,
        UserResponse owner,
        String caption,
        String imageUrl,
        Instant createdAt
) {
    public static MomentResponse from(Moment moment) {
        return new MomentResponse(
                moment.getId(),
                UserResponse.from(moment.getOwner()),
                moment.getCaption(),
                "/api/moments/" + moment.getId() + "/image",
                moment.getCreatedAt()
        );
    }
}
