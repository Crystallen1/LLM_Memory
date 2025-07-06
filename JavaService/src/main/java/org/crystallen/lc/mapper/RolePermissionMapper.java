package org.crystallen.lc.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
@Mapper
public interface RolePermissionMapper {
    List<Long> selectPermissionList(Long roleId);
    List<Long> selectPermissionListByList(List<Long> roleIds);

}
