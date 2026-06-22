package com.locket.clone.user.service.iplm;

import com.locket.clone.common.ApiException;
import com.locket.clone.user.dto.RegisterRequest;
import com.locket.clone.user.entity.User;
import com.locket.clone.user.repository.UserRepository;
import com.locket.clone.user.service.IUserService;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ApiException(HttpStatus.CONFLICT, "Username already taken");
        }
        User user = new User(
                request.username(),
                passwordEncoder.encode(request.password()),
                request.displayName()
        );
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found: " + username));
    }
}
