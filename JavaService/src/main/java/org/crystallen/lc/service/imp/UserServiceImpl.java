package org.crystallen.lc.service.imp;


import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.crystallen.lc.dto.UserLoginDTO;
import org.crystallen.lc.dto.UserRegisterDTO;
import org.crystallen.lc.entity.Users;
import org.crystallen.lc.mapper.UserMapper;
import org.crystallen.lc.mapper.UserRoleMapper;
import org.crystallen.lc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;

    @Autowired
    public UserServiceImpl(UserMapper userMapper,UserRoleMapper userRoleMapper) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
    }

    @Override
    public Users registerUser(UserRegisterDTO registrationDto) {
        if (userMapper.findByUsername(registrationDto.getUsername()) != null) {
            throw new IllegalArgumentException("Username is already taken");
        }
        if (userMapper.findByEmail(registrationDto.getEmail()) != null) {
            throw new IllegalArgumentException("Email is already registered");
        }

        Users users = new Users();
        users.setUsername(registrationDto.getUsername());
        users.setEmail(registrationDto.getEmail());

        users.setPassword( BCrypt.hashpw(registrationDto.getPassword()));
        users.setCreatedAt(LocalDateTime.now());


        userMapper.insertUser(users);
        log.info(users.getId().toString());
        userRoleMapper.insertUserRole(users.getId(),registrationDto.getRole().getType());
        return users;
    }

    @Override
    public boolean authenticateUser(UserLoginDTO loginDto) {
        Users users = userMapper.findByUsernameOrEmail(loginDto.getUsernameOrEmail());
        if (users != null && BCrypt.checkpw(loginDto.getPassword(), users.getPassword())) {
            StpUtil.login(users.getId());
//            log.info(StpUtil.getRoleList().toString());
//            log.info(StpUtil.getPermissionList().toString());
//            String token = JwtTokenUtil.generateToken(user.getUsername());
//
//            // 将 Token 存入 Redis，设置过期时间（如 1 天）
//            redisTemplate.opsForValue().set("TOKEN_" + user.getUsername(), token, 1, TimeUnit.DAYS);
//
//            return token;
            return true;
        } else {
            throw new IllegalArgumentException("Invalid username/email or password");
        }
    }

    @Override
    public Users selectUserByUsername(String username) {
        return userMapper.findByUsername(username);
    }
}