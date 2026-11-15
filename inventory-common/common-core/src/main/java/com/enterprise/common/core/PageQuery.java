package com.enterprise.common.core;

import com.enterprise.common.core.validation.Create;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 通用分页 / 搜索 / 排序请求基类。
 * 排序字段仅允许 [a-zA-Z0-9_]，从根源上阻断 order by 注入。
 */
public class PageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final Pattern SAFE_COLUMN = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]{0,63}$");

    @Min(value = 1, message = "页码最小为 1", groups = Create.class)
    private long pageNum = 1;

    @Min(value = 1, message = "每页条数最小为 1", groups = Create.class)
    @Max(value = 500, message = "每页最多 500 条", groups = Create.class)
    private long pageSize = 20;

    /** 模糊搜索关键字 */
    private String keyword;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /** 排序列（数据库字段名，下划线风格），需通过 SAFE_COLUMN 校验 */
    private String orderBy;

    private boolean asc = false;

    /** 常用排序字段白名单，子类可覆盖以进一步收窄 */
    protected Set<String> allowedOrderColumns() {
        return Set.of("id", "create_time", "update_time");
    }

    public String safeOrderBy() {
        if (orderBy == null || orderBy.isBlank()) return null;
        String column = orderBy.trim();
        if (!SAFE_COLUMN.matcher(column).matches()) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "非法的排序字段");
        }
        if (!allowedOrderColumns().isEmpty() && !allowedOrderColumns().contains(column)) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "不支持按该字段排序: " + column);
        }
        return column;
    }

    public long offset() { return (pageNum - 1) * pageSize; }

    public long getPageNum() { return pageNum; }
    public void setPageNum(long pageNum) { this.pageNum = pageNum; }
    public long getPageSize() { return pageSize; }
    public void setPageSize(long pageSize) { this.pageSize = pageSize; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getOrderBy() { return orderBy; }
    public void setOrderBy(String orderBy) { this.orderBy = orderBy; }
    public boolean isAsc() { return asc; }
    public void setAsc(boolean asc) { this.asc = asc; }
}
