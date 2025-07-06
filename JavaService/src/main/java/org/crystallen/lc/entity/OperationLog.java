package org.crystallen.lc.entity;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class OperationLog {
    private Long id; // 日志主键
    private String username; // 用户名
    private Long userId; // 用户ID
    private String ip; // 用户IP地址
    private String method; // 调用方法
    private String requestUrl; // 请求URL
    private String requestType; // 请求类型 (GET, POST等)
    private String requestParams; // 请求参数
    private String responseResult; // 返回结果
    private String operation; // 操作描述
    private Boolean isSuccess; // 操作是否成功
    private String errorMessage; // 异常信息
    private LocalDateTime operationTime; // 操作时间
}
