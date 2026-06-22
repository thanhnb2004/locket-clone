package com.locket.clone.user.controller;

import com.locket.clone.security.CurrentUser;
import com.locket.clone.user.dto.RegisterRequest;
import com.locket.clone.user.dto.UserResponse;
import com.locket.clone.user.entity.User;
import com.locket.clone.user.service.IUserService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final IUserService userService;
    private final CurrentUser currentUser;

    public UserController(IUserService userService, CurrentUser currentUser) {
        this.userService = userService;
        this.currentUser = currentUser;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    @GetMapping("/me")
    public UserResponse me() {
        return UserResponse.from(currentUser.get());
    }

    @GetMapping("/{username}")
    public UserResponse byUsername(@PathVariable String username) {
        return UserResponse.from(userService.getByUsername(username));
    }
}
