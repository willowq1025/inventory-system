package com.enterprise.module.inventory.service;

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
import com.enterprise.module.inventory.dto.BizWarehouseQuery;
import com.enterprise.module.inventory.entity.BizWarehouse;
import com.enterprise.module.inventory.excel.BizWarehouseExcel;
import com.enterprise.module.inventory.mapper.BizWarehouseMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 仓库服务：分页搜索 / 审核留痕 / 缓存一致性 / Excel 导入导出。
 * 统一继承 ServiceImpl 复用 MyBatis-Plus 通用能力，业务差异只写在此处。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BizWarehouseService extends ServiceImpl<BizWarehouseMapper, BizWarehouse> {

    private final CacheConsistency cacheConsistency;

    public PageResult<BizWarehouse> page(BizWarehouseQuery q) {
        return toResult(baseMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), buildWrapper(q)));
    }

    public List<BizWarehouse> listAll(BizWarehouseQuery q) {
        return list(buildWrapper(q));
    }

    public BizWarehouse detail(Long id) {
        BizWarehouse entity = getById(id);
        if (entity == null) throw new BizException(ResultCode.DATA_NOT_FOUND, "仓库不存在");
        return entity;
    }

    private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BizWarehouse> buildWrapper(BizWarehouseQuery q) {
        var w = Wrappers.lambdaQuery(BizWarehouse.class);
        if (StrUtil.isNotBlank(q.getKeyword())) {
            w.and(inner -> inner.like(BizWarehouse::getWarehouseName, q.getKeyword()).or().like(BizWarehouse::getWarehouseCode, q.getKeyword()));
        }
        w.eq(q.getStatus() != null, BizWarehouse::getStatus, q.getStatus());
        w.ge(q.getStartTime() != null, BizWarehouse::getCreateTime, q.getStartTime());
        w.le(q.getEndTime() != null, BizWarehouse::getCreateTime, q.getEndTime());
        String orderBy = q.safeOrderBy();
        if (orderBy == null) {
            w.orderByDesc(BizWarehouse::getCreateTime);
        } else {
            // safeOrderBy 已做正则 + 白名单校验，杜绝 order by 注入
            w.last("ORDER BY " + orderBy + (q.isAsc() ? " ASC" : " DESC"));
        }
        return w;
    }

    private PageResult<BizWarehouse> toResult(IPage<BizWarehouse> page) {
        return PageResult.of(page.getTotal(), page.getRecords(), page.getCurrent(), page.getSize());
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(BizWarehouse entity) {
        BizWarehouse exists = lambdaQuery().eq(BizWarehouse::getWarehouseCode, entity.getWarehouseCode()).one();
        if (exists != null && !exists.getId().equals(entity.getId())) {
            throw new BizException(ResultCode.DATA_ALREADY_EXISTS, "WarehouseCode已存在: " + entity.getWarehouseCode());
        }
        entity.setId(null);
        save(entity);
        return entity.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(BizWarehouse entity) {
        if (entity.getId() == null) throw new BizException(ResultCode.VALIDATION_ERROR, "ID不能为空");
        BizWarehouse exists = lambdaQuery().eq(BizWarehouse::getWarehouseCode, entity.getWarehouseCode()).one();
        if (exists != null && !exists.getId().equals(entity.getId())) {
            throw new BizException(ResultCode.DATA_ALREADY_EXISTS, "WarehouseCode已存在: " + entity.getWarehouseCode());
        }
        updateById(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        detail(id);
        removeById(id);
    }

    public void export(BizWarehouseQuery q, HttpServletResponse response) {
        List<BizWarehouseExcel> rows = listAll(q).stream().map(BizWarehouseExcel::from).toList();
        ExcelUtils.export(response, rows, BizWarehouseExcel.class, "仓库列表", "仓库");
    }

    @Transactional(rollbackFor = Exception.class)
    public ExcelImportResult importData(MultipartFile file) {
        return ExcelUtils.importExcel(file, BizWarehouseExcel.class, this::validateRow, batch -> {
            for (BizWarehouseExcel row : batch) {
                BizWarehouse entity = row.toEntity();
                BizWarehouse old = lambdaQuery().eq(BizWarehouse::getWarehouseCode, row.getWarehouseCode()).one();
                if (old != null) {
                    entity.setId(old.getId());
                    updateById(entity);
                } else {
                    entity.setId(null);
                    save(entity);
                }
            }
            cacheConsistency.evictPrefixAfterCommit(CacheKey.PRODUCT);
        });
    }

    private String validateRow(BizWarehouseExcel row) {
        if (StrUtil.isBlank(row.getWarehouseCode())) return "WarehouseCode不能为空";
        return null;
    }

}
