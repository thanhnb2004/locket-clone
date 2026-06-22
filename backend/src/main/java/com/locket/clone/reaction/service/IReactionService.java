package com.locket.clone.reaction.service;

import com.locket.clone.reaction.entity.Reaction;
import com.locket.clone.user.entity.User;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface IReactionService {

    /** Add or update the current user's reaction on a moment they are allowed to see. */
    Reaction react(User me, UUID momentId, String emoji);

    /** Remove the current user's reaction from a moment, if any. */
    void removeReaction(User me, UUID momentId);

    /** All reactions on a single moment, oldest first. */
    List<Reaction> reactionsFor(UUID momentId);

    /** All reactions across several moments, used to enrich a feed in one query. */
    List<Reaction> reactionsFor(Collection<UUID> momentIds);
}
