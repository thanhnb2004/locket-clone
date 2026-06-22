package com.locket.clone.reaction.repository;

import com.locket.clone.reaction.entity.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReactionRepository extends JpaRepository<Reaction, UUID> {

    List<Reaction> findByMomentIdOrderByCreatedAtAsc(UUID momentId);

    List<Reaction> findByMomentIdInOrderByCreatedAtAsc(Collection<UUID> momentIds);

    Optional<Reaction> findByMomentIdAndUserId(UUID momentId, UUID userId);
}
