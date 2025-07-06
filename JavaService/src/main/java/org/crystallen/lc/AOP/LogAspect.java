package org.crystallen.lc.AOP;

import cn.dev33.satoken.stp.StpUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.crystallen.lc.annotation.Log;
import org.crystallen.lc.entity.OperationLog;
import org.crystallen.lc.mapper.OperationLogMapper;
import org.crystallen.lc.util.IpUtils;
import org.crystallen.lc.util.ServletUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;


@Aspect
@Component
public class LogAspect {
    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);
    @Autowired
    private OperationLogMapper operationLogMapper;
    @AfterReturning(pointcut = "@annotation(controllerLog)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Log controllerLog, Object jsonResult) {
        handleLog(joinPoint, controllerLog, null, jsonResult);
    }

    @AfterThrowing(value = "@annotation(controllerLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint,Log controllerLog, Exception e){
        handleLog(joinPoint, controllerLog, e, null);
    }

    private void handleLog(JoinPoint joinPoint, Log controllerLog, Exception e, Object returnJson) {
        try {
            // 获取用户信息
            Long userId = Long.valueOf((String) StpUtil.getLoginId());
            String username = StpUtil.getLoginIdAsString();
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());


            // 获取方法信息
            String methodName = joinPoint.getSignature().toShortString();
            String requestParams = Arrays.toString(joinPoint.getArgs());


            // 构造日志对象
            OperationLog operationLog = new OperationLog();

            operationLog.setUserId(userId);
            operationLog.setUsername(username);
            operationLog.setIp(ip);
            operationLog.setMethod(methodName);
            operationLog.setRequestUrl(ServletUtils.getRequest().getRequestURI());
            operationLog.setRequestType(ServletUtils.getRequest().getMethod());
            operationLog.setRequestParams(requestParams);
            operationLog.setResponseResult(returnJson != null ? returnJson.toString() : null);
            operationLog.setOperation(controllerLog.description()); // 从注解中获取
            operationLog.setIsSuccess(e == null);
            operationLog.setErrorMessage(e != null ? e.getMessage() : null);
            operationLog.setOperationTime(LocalDateTime.now());
            // 插入日志
            operationLogMapper.insertLog(operationLog);

            log.info("操作日志已记录: {}", operationLog);
        } catch (Exception ex) {
            log.error("记录操作日志失败: {}", ex.getMessage());
        }
    }
}
