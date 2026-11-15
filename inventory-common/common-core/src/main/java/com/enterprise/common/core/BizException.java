package com.enterprise.common.core;

import java.io.Serial;

/**
 * 业务异常：由业务代码主动抛出，GlobalExceptionHandler 统一转为 Result，不打印堆栈。
 */
public class BizException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final int code;

    public BizException(String message) {
        super(message);
        this.code = ResultCode.BIZ_ERROR.getCode();
    }

    public BizException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BizException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(ResultCode resultCode, String message, Throwable cause) {
        super(message, cause);
        this.code = resultCode.getCode();
    }

    public int getCode() { return code; }

    @Override
    public synchronized Throwable fillInStackTrace() {
        // 业务异常无需采集堆栈，降低高并发下的开销
        return this;
    }
}
