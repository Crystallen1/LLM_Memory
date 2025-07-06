package org.crystallen.lc.exception;

import org.crystallen.lc.enums.Err;

public class BusinessException extends RuntimeException{
    protected String code;
    protected Object[] args;

    protected BusinessException() {
    }

    public BusinessException(String code, Object[] args, String defaultMessage, Throwable cause) {
        super(defaultMessage, cause);
        this.code = code;
        this.args = args;
    }

    public BusinessException(String code, String defaultMessage, Throwable cause) {
        this(code, (Object[])null, defaultMessage, cause);
    }

    public BusinessException(Err err, String message, Throwable cause) {
        this(err.getCode(), (Object[])null, message, cause);
    }

    public BusinessException(Err err, Throwable cause) {
        this(err.getCode(), (Object[])null, err.getMsg(), cause);
    }

    public BusinessException(int code, Object[] args, String defaultMessage, Throwable cause) {
        this(String.valueOf(code), args, defaultMessage, cause);
    }

    public BusinessException(int code, String defaultMessage, Throwable cause) {
        this(code, (Object[])null, defaultMessage, cause);
    }

    public BusinessException(String code, Object[] args, String defaultMessage) {
        this(code, args, defaultMessage, (Throwable)null);
    }

    public BusinessException(Err err) {
        this(err.getCode(), (Object[])null, err.getMsg(), (Throwable)null);
    }

    public BusinessException(Err err, String message) {
        this(err.getCode(), (Object[])null, message, (Throwable)null);
    }

    public BusinessException(String code, String defaultMessage) {
        this(code, (Object[])null, defaultMessage, (Throwable)null);
    }

    public BusinessException(int code, Object[] args, String defaultMessage) {
        this(String.valueOf(code), args, defaultMessage, (Throwable)null);
    }

    public BusinessException(int code, String defaultMessage) {
        this(code, (Object[])null, defaultMessage, (Throwable)null);
    }

    public BusinessException(String code, Object[] args, Throwable cause) {
        this(code, args, (String)null, cause);
    }

    public BusinessException(String code, Throwable cause) {
        this(code, (Object[])null, (String)null, cause);
    }

    public BusinessException(String code, Object[] args) {
        this(code, args, (String)null, (Throwable)null);
    }

    public BusinessException(String code) {
        this(code, (Object[])null, (String)null, (Throwable)null);
    }

    public BusinessException(int code, Object[] args, Throwable cause) {
        this(String.valueOf(code), args, cause);
    }

    public BusinessException(int code, Throwable cause) {
        this(code, (Object[])null, (String)null, cause);
    }

    public BusinessException(int code, Object[] args) {
        this(String.valueOf(code), args, (String)null, (Throwable)null);
    }

    public BusinessException(int code) {
        this(String.valueOf(code));
    }

    public String getCode() {
        return this.code;
    }

    public String getMessage() {
        return super.getMessage();
    }

    public String getLocalizedMessage() {
        return super.getLocalizedMessage();
    }

    public boolean isErrorOf(String code) {
        return code != null && this.code != null && this.code.equals(code);
    }

    public boolean isErrorOf(int code) {
        return this.isErrorOf(String.valueOf(code));
    }

    public boolean isErrorOf(Err err) {
        return this.isErrorOf(err.getCode());
    }

    public String toString() {
        String s = this.getClass().getName();
        String message = this.getLocalizedMessage();
        return message != null ? s + ": code=" + this.code + ", message=" + message : s + ": " + this.code;
    }
}
