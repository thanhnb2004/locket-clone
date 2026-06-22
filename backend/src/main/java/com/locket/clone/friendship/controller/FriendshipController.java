package com.locket.clone.friendship.controller;

import com.locket.clone.friendship.dto.FriendRequestResponse;
import com.locket.clone.friendship.dto.SendFriendRequest;
import com.locket.clone.friendship.entity.Friendship;
import com.locket.clone.friendship.service.IFriendshipService;
import com.locket.clone.security.CurrentUser;
import com.locket.clone.user.entity.User;
import com.locket.clone.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/friends")
public class FriendshipController {

    private final IFriendshipService friendshipService;
    private final CurrentUser currentUser;

    public FriendshipController(IFriendshipService friendshipService, CurrentUser currentUser) {
        this.friendshipService = friendshipService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<UserResponse> friends() {
        return friendshipService.listFriends(currentUser.get()).stream()
                .map(UserResponse::from)
                .toList();
    }

    @PostMapping("/requests")
    public ResponseEntity<FriendRequestResponse> sendRequest(@Valid @RequestBody SendFriendRequest body) {
        Friendship f = friendshipService.sendRequest(currentUser.get(), body.username());
        return ResponseEntity.status(HttpStatus.CREATED).body(FriendRequestResponse.from(f));
    }

    @GetMapping("/requests/incoming")
    public List<FriendRequestResponse> incoming() {
        return friendshipService.incomingRequests(currentUser.get()).stream()
                .map(FriendRequestResponse::from)
                .toList();
    }

    @GetMapping("/requests/outgoing")
    public List<FriendRequestResponse> outgoing() {
        return friendshipService.outgoingRequests(currentUser.get()).stream()
                .map(FriendRequestResponse::from)
                .toList();
    }

    @PostMapping("/requests/{id}/accept")
    public FriendRequestResponse accept(@PathVariable UUID id) {
        User me = currentUser.get();
        return FriendRequestResponse.from(friendshipService.respond(me, id, true));
    }

    @PostMapping("/requests/{id}/reject")
    public FriendRequestResponse reject(@PathVariable UUID id) {
        User me = currentUser.get();
        return FriendRequestResponse.from(friendshipService.respond(me, id, false));
    }
}
