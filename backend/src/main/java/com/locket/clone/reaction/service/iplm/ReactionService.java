package com.locket.clone.reaction.service.iplm;

import com.locket.clone.common.ApiException;
import com.locket.clone.moment.entity.Moment;
import com.locket.clone.moment.service.IMomentService;
import com.locket.clone.reaction.entity.Reaction;
import com.locket.clone.reaction.repository.ReactionRepository;
import com.locket.clone.reaction.service.IReactionService;
import com.locket.clone.user.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
public class ReactionService implements IReactionService {

    private final ReactionRepository reactionRepository;
    private final IMomentService momentService;

    public ReactionService(ReactionRepository reactionRepository, IMomentService momentService) {
        this.reactionRepository = reactionRepository;
        this.momentService = momentService;
    }

    @Override
    @Transactional
    public Reaction react(User me, UUID momentId, String emoji) {
        String trimmed = emoji == null ? "" : emoji.trim();
        if (trimmed.isEmpty() || trimmed.length() > 16) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Emoji must be 1-16 characters");
        }

        // getVisible enforces that the user owns or is a friend of the moment's owner.
        Moment moment = momentService.getVisible(me, momentId);

        Reaction reaction = reactionRepository
                .findByMomentIdAndUserId(moment.getId(), me.getId())
                .orElseGet(() -> new Reaction(moment, me, trimmed));
        reaction.setEmoji(trimmed);
        return reactionRepository.save(reaction);
    }

    @Override
    @Transactional
    public void removeReaction(User me, UUID momentId) {
        // Confirm the moment is visible to the user before touching reactions.
        momentService.getVisible(me, momentId);
        reactionRepository.findByMomentIdAndUserId(momentId, me.getId())
                .ifPresent(reactionRepository::delete);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reaction> reactionsFor(UUID momentId) {
        return reactionRepository.findByMomentIdOrderByCreatedAtAsc(momentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reaction> reactionsFor(Collection<UUID> momentIds) {
        if (momentIds.isEmpty()) {
            return List.of();
        }
        return reactionRepository.findByMomentIdInOrderByCreatedAtAsc(momentIds);
    }
}
