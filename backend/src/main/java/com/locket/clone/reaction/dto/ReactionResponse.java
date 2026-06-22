package com.locket.clone.reaction.dto;

import com.locket.clone.reaction.entity.Reaction;
import com.locket.clone.user.dto.UserResponse;

import java.time.Instant;
import java.util.UUID;

public record ReactionResponse(
        UUID id,
        UserResponse user,
        String emoji,
        Instant createdAt
) {
    public static ReactionResponse from(Reaction reaction) {
        return new ReactionResponse(
                reaction.getId(),
                UserResponse.from(reaction.getUser()),
                reaction.getEmoji(),
                reaction.getCreatedAt()
        );
    }
}
