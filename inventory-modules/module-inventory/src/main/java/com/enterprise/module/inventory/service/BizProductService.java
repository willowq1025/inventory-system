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
import com.enterprise.common.redis.service.CacheService;
import com.enterprise.module.inventory.dto.BizProductQuery;
import com.enterprise.module.inventory.entity.BizProduct;
import com.enterprise.module.inventory.excel.BizProductExcel;
import com.enterprise.module.inventory.mapper.BizProductMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 商品服务：分页搜索 / 审核留痕 / 缓存一致性 / Excel 导入导出。
 * 统一继承 ServiceImpl 复用 MyBatis-Plus 通用能力，业务差异只写在此处。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BizProductService extends ServiceImpl<BizProductMapper, BizProduct> {

    private final CacheConsistency cacheConsistency;
    private final CacheService cacheService;

    public PageResult<BizProduct> page(BizProductQuery q) {
        return toResult(baseMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), buildWrapper(q)));
    }

    public List<BizProduct> listAll(BizProductQuery q) {
        return list(buildWrapper(q));
    }

    public BizProduct detail(Long id) {
        BizProduct entity = cacheService.getOrLoad(CacheKey.PRODUCT + id, BizProduct.class, () -> getById(id));
        if (entity == null) throw new BizException(ResultCode.DATA_NOT_FOUND, "商品不存在");
        return entity;
    }

    private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BizProduct> buildWrapper(BizProductQuery q) {
        var w = Wrappers.lambdaQuery(BizProduct.class);
        if (StrUtil.isNotBlank(q.getKeyword())) {
            w.and(inner -> inner.like(BizProduct::getProductName, q.getKeyword()).or().like(BizProduct::getProductCode, q.getKeyword()).or().like(BizProduct::getBrand, q.getKeyword()));
        }
        w.eq(q.getCategoryId() != null, BizProduct::getCategoryId, q.getCategoryId());
        w.eq(q.getStatus() != null, BizProduct::getStatus, q.getStatus());
        w.ge(q.getStartTime() != null, BizProduct::getCreateTime, q.getStartTime());
        w.le(q.getEndTime() != null, BizProduct::getCreateTime, q.getEndTime());
        String orderBy = q.safeOrderBy();
        if (orderBy == null) {
            w.orderByDesc(BizProduct::getCreateTime);
        } else {
            // safeOrderBy 已做正则 + 白名单校验，杜绝 order by 注入
            w.last("ORDER BY " + orderBy + (q.isAsc() ? " ASC" : " DESC"));
        }
        return w;
    }

    private PageResult<BizProduct> toResult(IPage<BizProduct> page) {
        return PageResult.of(page.getTotal(), page.getRecords(), page.getCurrent(), page.getSize());
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(BizProduct entity) {
        BizProduct exists = lambdaQuery().eq(BizProduct::getProductCode, entity.getProductCode()).one();
        if (exists != null && !exists.getId().equals(entity.getId())) {
            throw new BizException(ResultCode.DATA_ALREADY_EXISTS, "ProductCode已存在: " + entity.getProductCode());
        }
        entity.setId(null);
        save(entity);
        cacheConsistency.evictAfterCommit(CacheKey.PRODUCT + entity.getId());
        return entity.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(BizProduct entity) {
        if (entity.getId() == null) throw new BizException(ResultCode.VALIDATION_ERROR, "ID不能为空");
        BizProduct exists = lambdaQuery().eq(BizProduct::getProductCode, entity.getProductCode()).one();
        if (exists != null && !exists.getId().equals(entity.getId())) {
            throw new BizException(ResultCode.DATA_ALREADY_EXISTS, "ProductCode已存在: " + entity.getProductCode());
        }
        updateById(entity);
        cacheConsistency.evictAfterCommit(CacheKey.PRODUCT + entity.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        detail(id);
        removeById(id);
        cacheConsistency.evictAfterCommit(CacheKey.PRODUCT + id);
    }

    public void export(BizProductQuery q, HttpServletResponse response) {
        List<BizProductExcel> rows = listAll(q).stream().map(BizProductExcel::from).toList();
        ExcelUtils.export(response, rows, BizProductExcel.class, "商品列表", "商品");
    }

    @Transactional(rollbackFor = Exception.class)
    public ExcelImportResult importData(MultipartFile file) {
        return ExcelUtils.importExcel(file, BizProductExcel.class, this::validateRow, batch -> {
            for (BizProductExcel row : batch) {
                BizProduct entity = row.toEntity();
                BizProduct old = lambdaQuery().eq(BizProduct::getProductCode, row.getProductCode()).one();
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

    private String validateRow(BizProductExcel row) {
        if (StrUtil.isBlank(row.getProductCode())) return "ProductCode不能为空";
        return null;
    }

}
