package com.locket.clone.user.service;

import com.locket.clone.user.dto.RegisterRequest;
import com.locket.clone.user.entity.User;

public interface IUserService {

    User register(RegisterRequest request);

    User getByUsername(String username);
}
