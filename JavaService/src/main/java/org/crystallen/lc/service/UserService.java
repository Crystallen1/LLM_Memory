package org.crystallen.lc.service;

import org.crystallen.lc.dto.UserLoginDTO;
import org.crystallen.lc.dto.UserRegisterDTO;
import org.crystallen.lc.entity.Users;

public interface UserService {
    Users registerUser(UserRegisterDTO registrationDto);
    boolean authenticateUser(UserLoginDTO loginDto);

    Users selectUserByUsername(String username);
}
