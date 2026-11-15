package com.enterprise.common.excel.util;

import com.alibaba.excel.EasyExcel;
import com.enterprise.common.core.BizException;
import com.enterprise.common.core.ResultCode;
import com.enterprise.common.core.util.JsonUtils;
import com.enterprise.common.excel.listener.BatchImportListener;
import com.enterprise.common.excel.model.ExcelImportResult;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/** EasyExcel 导入导出统一封装。 */
@Slf4j
public final class ExcelUtils {

    private static final String XLSX_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private ExcelUtils() {}

    public static <T> void export(HttpServletResponse response, List<T> data, Class<T> clazz,
                                  String fileName, String sheetName) {
        try {
            prepareResponse(response, fileName);
            EasyExcel.write(response.getOutputStream(), clazz)
                    .sheet(sheetName == null ? "Sheet1" : sheetName)
                    .doWrite(data == null ? List.of() : data);
        } catch (Exception e) {
            log.error("[excel] 导出失败 fileName={}", fileName, e);
            throw new BizException(ResultCode.FILE_EXPORT_ERROR, "导出失败: " + e.getMessage());
        }
    }

    public static <T> ExcelImportResult importExcel(MultipartFile file, Class<T> clazz,
                                                    Function<T, String> validator,
                                                    Consumer<List<T>> batchConsumer) {
        return importExcel(file, clazz, validator, batchConsumer, 500);
    }

    public static <T> ExcelImportResult importExcel(MultipartFile file, Class<T> clazz,
                                                    Function<T, String> validator,
                                                    Consumer<List<T>> batchConsumer,
                                                    int batchSize) {
        if (file == null || file.isEmpty()) {
            throw new BizException(ResultCode.FILE_IMPORT_ERROR, "上传文件不能为空");
        }
        String name = file.getOriginalFilename();
        if (name == null || !(name.endsWith(".xlsx") || name.endsWith(".xls"))) {
            throw new BizException(ResultCode.FILE_IMPORT_ERROR, "仅支持 .xlsx / .xls 文件");
        }
        ExcelImportResult result = ExcelImportResult.empty();
        try (InputStream in = file.getInputStream()) {
            EasyExcel.read(in, clazz, new BatchImportListener<>(validator, batchConsumer, result, batchSize))
                    .sheet()
                    .doRead();
        } catch (Exception e) {
            log.error("[excel] 导入失败 fileName={}", name, e);
            throw new BizException(ResultCode.FILE_IMPORT_ERROR, "导入失败: " + e.getMessage());
        }
        return result;
    }

    /** 导出错误清单（错误行 + 原因），便于用户修正后重新导入 */
    public static void exportErrors(HttpServletResponse response, ExcelImportResult result, String fileName) {
        List<String[]> rows = result.getErrors().stream()
                .map(e -> new String[]{"第 " + e.rowIndex() + " 行", e.message()})
                .toList();
        try {
            prepareResponse(response, fileName);
            EasyExcel.write(response.getOutputStream())
                    .head(List.of(List.of("行号"), List.of("错误原因")))
                    .sheet("错误清单")
                    .doWrite(rows);
        } catch (Exception e) {
            throw new BizException(ResultCode.FILE_EXPORT_ERROR, "错误清单导出失败", e);
        }
    }

    private static void prepareResponse(HttpServletResponse response, String fileName) {
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        response.setContentType(XLSX_CONTENT_TYPE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded + ".xlsx");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setHeader("Cache-Control", "no-store");
    }
}
