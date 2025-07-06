package org.crystallen.lc.util;


import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.crystallen.lc.enums.CommonError;
import org.crystallen.lc.enums.Err;
import org.crystallen.lc.exception.BusinessException;
import org.springframework.util.StringUtils;

import java.util.Iterator;
import java.util.Set;

public class CommonResult<T> extends Result<T> {
    public CommonResult(boolean success, String errcode, String errmsg) {
        super(success, errcode, errmsg);
    }

    public CommonResult(boolean success, String errcode, String errmsg, T result) {
        super(success, errcode, errmsg, result);
    }

    public static final <T> CommonResult<T> failure(String errcode, String errmsg) {
        return failure(errcode, (String)errmsg, -1);
    }

    public static final <T> CommonResult<T> failure(String errcode, String errmsg, int maxWidth) {
        return new CommonResult(CommonError.SUCCESS.getCode().equals(errcode), errcode, maxWidth < 0 ? errmsg : StringUtils.truncate(errmsg, maxWidth));
    }

    public static final <T> CommonResult<T> failure(String errmsg) {
        return failure((String)errmsg, -1);
    }

    public static final <T> CommonResult<T> failure(String errmsg, int maxWidth) {
        return failure(CommonError.INVALID_PARAM.getCode(), errmsg, maxWidth);
    }

    public static final <T> CommonResult<T> failure(Err err) {
        return failure((Err)err, -1);
    }

    public static final <T> CommonResult<T> failure(Err err, int maxWidth) {
        return failure(err.getCode(), err.getMsg(), maxWidth);
    }

    public static final <T> CommonResult<T> failure(Throwable t) {
        return failure((String)null, (Throwable)t, -1);
    }

    public static final <T> CommonResult<T> failure(Throwable t, int maxWidth) {
        return failure((String)null, (Throwable)t, maxWidth);
    }

    public static final <T> CommonResult<T> failure(String message, Throwable t) {
        return failure(message, (Throwable)t, -1);
    }

    public static final <T> CommonResult<T> failure(String message, Throwable t, int maxWidth) {
        String errcode = CommonError.UNDEFINED.getCode();
        String errmsg = StringUtils.isEmpty(message) ? CommonError.UNDEFINED.getMsg() : message;
        if (t instanceof ConstraintViolationException) {
            if (StringUtils.isEmpty(message)) {
                StringBuffer sb = new StringBuffer();
                Set<ConstraintViolation<?>> constraintViolations = ((ConstraintViolationException)t).getConstraintViolations();
                Iterator var7 = constraintViolations.iterator();

                while(var7.hasNext()) {
                    ConstraintViolation constraintViolation = (ConstraintViolation)var7.next();
                    sb.append(StringUtils.isEmpty(constraintViolation.getMessage()) ? constraintViolation.toString() : constraintViolation.getMessage()).append("\r\n");
                }

                errmsg = CommonError.INVALID_PARAM.getMsg() + ": " + sb.toString();
            }

            errcode = CommonError.INVALID_PARAM.getCode();
        } else if (t instanceof IllegalArgumentException) {
            errcode = CommonError.INVALID_PARAM.getCode();
            errmsg = StringUtils.isEmpty(message) ? t.getMessage() : message;
        } else if (t instanceof BusinessException) {
            errcode = ((BusinessException)t).getCode();
            errmsg = StringUtils.isEmpty(message) ? t.getMessage() : message;
        } else if (("com.alibaba.dubbo.rpc.RpcException".equals(t.getClass().getName()))) {
            errcode = CommonError.SERVICE_UNAVAILABLE.getCode();
            errmsg = CommonError.SERVICE_UNAVAILABLE.getMsg();
        }

        return failure(errcode, errmsg, maxWidth);
    }

    public static final <T> CommonResult<T> success() {
        return failure((Err)CommonError.SUCCESS);
    }

    public static final <T> CommonResult<T> success(T data) {
        return new CommonResult(true, CommonError.SUCCESS.getCode(), CommonError.SUCCESS.getMsg(), data);
    }
}

