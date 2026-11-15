package com.enterprise.common.core;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * 统一分页响应（不依赖任何持久层 API，保持 common-core 纯净）。
 */
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private long total;
    private List<T> records;
    private long pageNum;
    private long pageSize;
    private long pages;

    public PageResult() {}

    public PageResult(long total, List<T> records, long pageNum, long pageSize) {
        this.total = total;
        this.records = records == null ? Collections.emptyList() : records;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pages = pageSize <= 0 ? 0 : (total + pageSize - 1) / pageSize;
    }

    public static <T> PageResult<T> of(long total, List<T> records, long pageNum, long pageSize) {
        return new PageResult<>(total, records, pageNum, pageSize);
    }

    public static <T> PageResult<T> empty(PageQuery query) {
        return new PageResult<>(0, Collections.emptyList(), query.getPageNum(), query.getPageSize());
    }

    public <R> PageResult<R> map(Function<T, R> converter) {
        return new PageResult<>(total, records.stream().map(converter).toList(), pageNum, pageSize);
    }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public List<T> getRecords() { return records; }
    public void setRecords(List<T> records) { this.records = records; }
    public long getPageNum() { return pageNum; }
    public void setPageNum(long pageNum) { this.pageNum = pageNum; }
    public long getPageSize() { return pageSize; }
    public void setPageSize(long pageSize) { this.pageSize = pageSize; }
    public long getPages() { return pages; }
    public void setPages(long pages) { this.pages = pages; }
}
