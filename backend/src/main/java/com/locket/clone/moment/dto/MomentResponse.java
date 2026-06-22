package com.locket.clone.moment.dto;

import com.locket.clone.moment.entity.Moment;
import com.locket.clone.reaction.dto.ReactionResponse;
import com.locket.clone.reaction.entity.Reaction;
import com.locket.clone.user.dto.UserResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MomentResponse(
        UUID id,
        UserResponse owner,
        String caption,
        String imageUrl,
        Instant createdAt,
        List<ReactionResponse> reactions,
        String myReaction
) {
    public static MomentResponse from(Moment moment) {
        return from(moment, List.of(), null);
    }

    /**
     * Builds a response enriched with the moment's reactions and, if {@code myUserId} is given,
     * the emoji the current user reacted with (or {@code null} if they have not reacted).
     */
    public static MomentResponse from(Moment moment, List<Reaction> reactions, UUID myUserId) {
        String myReaction = null;
        if (myUserId != null) {
            myReaction = reactions.stream()
                    .filter(r -> r.getUser().getId().equals(myUserId))
                    .map(Reaction::getEmoji)
                    .findFirst()
                    .orElse(null);
        }
        return new MomentResponse(
                moment.getId(),
                UserResponse.from(moment.getOwner()),
                moment.getCaption(),
                "/api/moments/" + moment.getId() + "/image",
                moment.getCreatedAt(),
                reactions.stream().map(ReactionResponse::from).toList(),
                myReaction
        );
    }
}
