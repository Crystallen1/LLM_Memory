package org.crystallen.lc.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserRoleMapper {
    void insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    List<Long> selectRoleList(Long userId);
}
