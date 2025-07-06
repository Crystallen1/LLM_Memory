package org.crystallen.lc.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.crystallen.lc.entity.Users;

@Mapper
public interface UserMapper {
    void insertUser(Users users);
    Users findByUsername(String username);
    Users findByEmail(String email);
    Users findByUsernameOrEmail(String usernameOrEmail);
}