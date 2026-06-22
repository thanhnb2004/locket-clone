package com.locket.clone.friendship.service.iplm;

import com.locket.clone.common.ApiException;
import com.locket.clone.friendship.entity.FriendRequestStatus;
import com.locket.clone.friendship.entity.Friendship;
import com.locket.clone.friendship.repository.FriendshipRepository;
import com.locket.clone.friendship.service.IFriendshipService;
import com.locket.clone.user.entity.User;
import com.locket.clone.user.service.IUserService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class FriendshipService implements IFriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final IUserService userService;

    public FriendshipService(FriendshipRepository friendshipRepository, IUserService userService) {
        this.friendshipRepository = friendshipRepository;
        this.userService = userService;
    }

    @Override
    @Transactional
    public Friendship sendRequest(User me, String targetUsername) {
        User target = userService.getByUsername(targetUsername);
        if (target.getId().equals(me.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You cannot add yourself");
        }

        Friendship existing = friendshipRepository.findBetween(me, target).orElse(null);
        if (existing != null) {
            switch (existing.getStatus()) {
                case ACCEPTED -> throw new ApiException(HttpStatus.CONFLICT, "You are already friends");
                case PENDING -> throw new ApiException(HttpStatus.CONFLICT, "A friend request is already pending");
                case REJECTED -> {
                    // allow re-requesting after a rejection: reopen the existing row
                    existing.setStatus(FriendRequestStatus.PENDING);
                    existing.setRespondedAt(null);
                    return friendshipRepository.save(existing);
                }
            }
        }

        return friendshipRepository.save(new Friendship(me, target));
    }

    @Override
    @Transactional
    public Friendship respond(User me, UUID requestId, boolean accept) {
        Friendship friendship = friendshipRepository.findById(requestId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Friend request not found"));

        if (!friendship.getAddressee().getId().equals(me.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only the addressee can respond to this request");
        }
        if (friendship.getStatus() != FriendRequestStatus.PENDING) {
            throw new ApiException(HttpStatus.CONFLICT, "This request is no longer pending");
        }

        friendship.setStatus(accept ? FriendRequestStatus.ACCEPTED : FriendRequestStatus.REJECTED);
        friendship.setRespondedAt(Instant.now());
        return friendshipRepository.save(friendship);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> listFriends(User me) {
        return friendshipRepository.findAcceptedForUser(me).stream()
                .map(f -> f.getRequester().getId().equals(me.getId()) ? f.getAddressee() : f.getRequester())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Friendship> incomingRequests(User me) {
        return friendshipRepository.findByAddresseeAndStatus(me, FriendRequestStatus.PENDING);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Friendship> outgoingRequests(User me) {
        return friendshipRepository.findByRequesterAndStatus(me, FriendRequestStatus.PENDING);
    }
}
