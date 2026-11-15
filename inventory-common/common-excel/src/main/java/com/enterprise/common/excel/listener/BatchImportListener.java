package com.enterprise.common.excel.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.enterprise.common.excel.model.ExcelImportResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 通用导入监听器：逐行校验 → 分批落库 → 收集错误行，避免大文件一次性载入内存。
 *
 * @param validator      返回 null 表示校验通过，否则返回错误原因
 * @param batchConsumer  业务侧批次处理（应在一个事务内）
 */
@Slf4j
@RequiredArgsConstructor
public class BatchImportListener<T> implements ReadListener<T> {

    private static final int DEFAULT_BATCH_SIZE = 500;

    private final Function<T, String> validator;
    private final Consumer<List<T>> batchConsumer;
    private final ExcelImportResult result;
    private final int batchSize;
    private final List<T> buffer = new ArrayList<>();

    public BatchImportListener(Function<T, String> validator, Consumer<List<T>> batchConsumer,
                               ExcelImportResult result) {
        this(validator, batchConsumer, result, DEFAULT_BATCH_SIZE);
    }

    @Override
    public void invoke(T data, AnalysisContext context) {
        result.setTotal(result.getTotal() + 1);
        int row = context.readRowHolder().getRowIndex() + 1;
        try {
            String error = validator == null ? null : validator.apply(data);
            if (error != null) {
                result.addError(row, error);
                return;
            }
            buffer.add(data);
            if (buffer.size() >= batchSize) flush(row);
        } catch (Exception e) {
            result.addError(row, "校验异常: " + e.getMessage());
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        flush(0);
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) {
        int row = context.readRowHolder() == null ? 0 : context.readRowHolder().getRowIndex() + 1;
        result.addError(row, "解析失败: " + exception.getMessage());
    }

    private void flush(int row) {
        if (buffer.isEmpty()) return;
        try {
            batchConsumer.accept(new ArrayList<>(buffer));
            result.addSuccess(buffer.size());
        } catch (Exception e) {
            log.warn("[excel] 批次写入失败 size={}", buffer.size(), e);
            for (int i = 0; i < buffer.size(); i++) {
                result.addError(row, "写入失败: " + e.getMessage());
            }
        } finally {
            buffer.clear();
        }
    }
}
