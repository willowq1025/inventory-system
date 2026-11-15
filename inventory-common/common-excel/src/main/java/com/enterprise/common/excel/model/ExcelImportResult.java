package com.enterprise.common.excel.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** 导入结果：成功条数 + 逐行错误清单（前端可下载错误报告）。 */
@Data
public class ExcelImportResult {

    private int total;
    private int successCount;
    private int failCount;
    private List<RowError> errors = new ArrayList<>();

    public record RowError(int rowIndex, String message) {}

    public void addError(int rowIndex, String message) {
        errors.add(new RowError(rowIndex, message));
        failCount++;
    }

    public void addSuccess(int count) { successCount += count; }

    public static ExcelImportResult empty() { return new ExcelImportResult(); }
}
