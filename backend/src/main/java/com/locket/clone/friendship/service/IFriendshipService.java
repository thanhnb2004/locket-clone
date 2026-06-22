package com.locket.clone.friendship.service;

import com.locket.clone.friendship.entity.Friendship;
import com.locket.clone.user.entity.User;

import java.util.List;
import java.util.UUID;

public interface IFriendshipService {

    Friendship sendRequest(User me, String targetUsername);

    Friendship respond(User me, UUID requestId, boolean accept);

    List<User> listFriends(User me);

    List<Friendship> incomingRequests(User me);

    List<Friendship> outgoingRequests(User me);
}
