package com.enterprise.module.system.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.common.core.BizException;
import com.enterprise.common.core.CacheKey;
import com.enterprise.common.core.PageResult;
import com.enterprise.common.core.ResultCode;
import com.enterprise.common.excel.model.ExcelImportResult;
import com.enterprise.common.excel.util.ExcelUtils;
import com.enterprise.common.redis.service.CacheConsistency;
import com.enterprise.module.system.dto.SysOperLogQuery;
import com.enterprise.module.system.entity.SysOperLog;
import com.enterprise.module.system.excel.SysOperLogExcel;
import com.enterprise.module.system.mapper.SysOperLogMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 审计日志服务：分页搜索 / 审核留痕 / 缓存一致性 / Excel 导入导出。
 * 统一继承 ServiceImpl 复用 MyBatis-Plus 通用能力，业务差异只写在此处。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysOperLogService extends ServiceImpl<SysOperLogMapper, SysOperLog> {

    private final CacheConsistency cacheConsistency;

    public PageResult<SysOperLog> page(SysOperLogQuery q) {
        return toResult(baseMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), buildWrapper(q)));
    }

    public List<SysOperLog> listAll(SysOperLogQuery q) {
        return list(buildWrapper(q));
    }

    public SysOperLog detail(Long id) {
        SysOperLog entity = getById(id);
        if (entity == null) throw new BizException(ResultCode.DATA_NOT_FOUND, "审计日志不存在");
        return entity;
    }

    private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysOperLog> buildWrapper(SysOperLogQuery q) {
        var w = Wrappers.lambdaQuery(SysOperLog.class);
        if (StrUtil.isNotBlank(q.getKeyword())) {
            w.and(inner -> inner.like(SysOperLog::getTitle, q.getKeyword()).or().like(SysOperLog::getOperName, q.getKeyword()).or().like(SysOperLog::getRequestUrl, q.getKeyword()).or().like(SysOperLog::getTraceId, q.getKeyword()));
        }
        w.eq(q.getBusinessType() != null, SysOperLog::getBusinessType, q.getBusinessType());
        w.eq(q.getStatus() != null, SysOperLog::getStatus, q.getStatus());
        w.eq(q.getOperId() != null, SysOperLog::getOperId, q.getOperId());
        w.ge(q.getStartTime() != null, SysOperLog::getOperTime, q.getStartTime());
        w.le(q.getEndTime() != null, SysOperLog::getOperTime, q.getEndTime());
        String orderBy = q.safeOrderBy();
        if (orderBy == null) {
            w.orderByDesc(SysOperLog::getOperTime);
        } else {
            // safeOrderBy 已做正则 + 白名单校验，杜绝 order by 注入
            w.last("ORDER BY " + orderBy + (q.isAsc() ? " ASC" : " DESC"));
        }
        return w;
    }

    private PageResult<SysOperLog> toResult(IPage<SysOperLog> page) {
        return PageResult.of(page.getTotal(), page.getRecords(), page.getCurrent(), page.getSize());
    }


    public void export(SysOperLogQuery q, HttpServletResponse response) {
        List<SysOperLogExcel> rows = listAll(q).stream().map(SysOperLogExcel::from).toList();
        ExcelUtils.export(response, rows, SysOperLogExcel.class, "审计日志列表", "审计日志");
    }

}
