package org.crystallen.lc.enums;

public enum CommonError implements Err{
    SUCCESS("0000", "成功"),
    INVALID_PARAM("0004", "参数错误"),

    /** 参数错误 */
    PARAM_ERR("400","参数错误"),
    PERMISSION_ERR("403","没有权限"),
    SERVICE_NOT_EXIST("0006", "服务不存在"),
    SERVICE_UNAVAILABLE("0007", "服务暂不可用"),
    UNDEFINED("9999", "系统错误");

    ;
    final String code;

    final String msg;

    CommonError(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getMsg() {
        return this.msg;
    }
}
