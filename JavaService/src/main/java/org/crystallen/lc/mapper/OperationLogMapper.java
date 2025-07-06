package org.crystallen.lc.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.crystallen.lc.entity.OperationLog;

@Mapper
public interface OperationLogMapper {
    void insertLog(OperationLog log);

}
