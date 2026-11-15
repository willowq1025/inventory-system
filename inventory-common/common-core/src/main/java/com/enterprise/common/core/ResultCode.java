package com.enterprise.common.core;

/**
 * 全局响应码。区间约定：
 * 2xx 成功 / 4xx 客户端 / 5xx 服务端；1000+ 为业务自定义码。
 */
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "登录状态已失效，请重新登录"),
    FORBIDDEN(403, "没有权限访问该资源"),
    NOT_FOUND(404, "请求的资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    CONFLICT(409, "数据状态冲突，请刷新后重试"),
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后再试"),
    INTERNAL_ERROR(500, "系统繁忙，请稍后再试"),

    // ---------------- 业务码 ----------------
    BIZ_ERROR(1000, "业务处理失败"),
    VALIDATION_ERROR(1001, "参数校验失败"),
    DATA_NOT_FOUND(1002, "数据不存在或已被删除"),
    DATA_ALREADY_EXISTS(1003, "数据已存在"),
    REPEAT_SUBMIT(1004, "请勿重复提交"),
    REQUEST_IN_PROGRESS(1005, "请求正在处理中，请稍后重试"),
    CONCURRENT_CONFLICT(1006, "数据已被其他操作修改，请重试"),
    STOCK_NOT_ENOUGH(1007, "库存不足，无法完成出库"),
    STOCK_NEGATIVE(1008, "库存数量不能为负"),
    ORDER_STATUS_ILLEGAL(1009, "订单当前状态不允许该操作"),
    FILE_IMPORT_ERROR(1010, "文件导入失败"),
    FILE_EXPORT_ERROR(1011, "文件导出失败"),
    LOGIN_ERROR(1020, "账号或密码错误"),
    USER_DISABLED(1021, "账号已被停用"),
    TOKEN_EXPIRED(1022, "登录已过期"),
    DATA_SCOPE_DENIED(1030, "无权访问该范围的数据"),
    NOTIFY_ERROR(1040, "通知发送失败");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
}
