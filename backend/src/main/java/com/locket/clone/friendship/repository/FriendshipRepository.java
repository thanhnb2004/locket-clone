package com.locket.clone.friendship.repository;

import com.locket.clone.friendship.entity.FriendRequestStatus;
import com.locket.clone.friendship.entity.Friendship;
import com.locket.clone.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {

    @Query("""
            SELECT f FROM Friendship f
            WHERE (f.requester = :a AND f.addressee = :b)
               OR (f.requester = :b AND f.addressee = :a)
            """)
    Optional<Friendship> findBetween(@Param("a") User a, @Param("b") User b);

    @Query("""
            SELECT f FROM Friendship f
            WHERE f.status = com.locket.clone.friendship.entity.FriendRequestStatus.ACCEPTED
              AND (f.requester = :user OR f.addressee = :user)
            ORDER BY f.respondedAt DESC
            """)
    List<Friendship> findAcceptedForUser(@Param("user") User user);

    List<Friendship> findByAddresseeAndStatus(User addressee, FriendRequestStatus status);

    List<Friendship> findByRequesterAndStatus(User requester, FriendRequestStatus status);
}
