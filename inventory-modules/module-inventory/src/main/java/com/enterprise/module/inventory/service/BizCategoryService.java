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
import com.enterprise.module.inventory.dto.BizCategoryQuery;
import com.enterprise.module.inventory.entity.BizCategory;
import com.enterprise.module.inventory.excel.BizCategoryExcel;
import com.enterprise.module.inventory.mapper.BizCategoryMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 商品分类服务：分页搜索 / 审核留痕 / 缓存一致性 / Excel 导入导出。
 * 统一继承 ServiceImpl 复用 MyBatis-Plus 通用能力，业务差异只写在此处。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BizCategoryService extends ServiceImpl<BizCategoryMapper, BizCategory> {

    private final CacheConsistency cacheConsistency;

    public PageResult<BizCategory> page(BizCategoryQuery q) {
        return toResult(baseMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), buildWrapper(q)));
    }

    public List<BizCategory> listAll(BizCategoryQuery q) {
        return list(buildWrapper(q));
    }

    public BizCategory detail(Long id) {
        BizCategory entity = getById(id);
        if (entity == null) throw new BizException(ResultCode.DATA_NOT_FOUND, "商品分类不存在");
        return entity;
    }

    private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BizCategory> buildWrapper(BizCategoryQuery q) {
        var w = Wrappers.lambdaQuery(BizCategory.class);
        if (StrUtil.isNotBlank(q.getKeyword())) {
            w.and(inner -> inner.like(BizCategory::getCategoryName, q.getKeyword()).or().like(BizCategory::getCategoryCode, q.getKeyword()));
        }
        w.eq(q.getParentId() != null, BizCategory::getParentId, q.getParentId());
        w.ge(q.getStartTime() != null, BizCategory::getCreateTime, q.getStartTime());
        w.le(q.getEndTime() != null, BizCategory::getCreateTime, q.getEndTime());
        String orderBy = q.safeOrderBy();
        if (orderBy == null) {
            w.orderByDesc(BizCategory::getCreateTime);
        } else {
            // safeOrderBy 已做正则 + 白名单校验，杜绝 order by 注入
            w.last("ORDER BY " + orderBy + (q.isAsc() ? " ASC" : " DESC"));
        }
        return w;
    }

    private PageResult<BizCategory> toResult(IPage<BizCategory> page) {
        return PageResult.of(page.getTotal(), page.getRecords(), page.getCurrent(), page.getSize());
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(BizCategory entity) {
        BizCategory exists = lambdaQuery().eq(BizCategory::getCategoryCode, entity.getCategoryCode()).one();
        if (exists != null && !exists.getId().equals(entity.getId())) {
            throw new BizException(ResultCode.DATA_ALREADY_EXISTS, "CategoryCode已存在: " + entity.getCategoryCode());
        }
        entity.setId(null);
        save(entity);
        return entity.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(BizCategory entity) {
        if (entity.getId() == null) throw new BizException(ResultCode.VALIDATION_ERROR, "ID不能为空");
        BizCategory exists = lambdaQuery().eq(BizCategory::getCategoryCode, entity.getCategoryCode()).one();
        if (exists != null && !exists.getId().equals(entity.getId())) {
            throw new BizException(ResultCode.DATA_ALREADY_EXISTS, "CategoryCode已存在: " + entity.getCategoryCode());
        }
        updateById(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        detail(id);
        removeById(id);
    }

    public void export(BizCategoryQuery q, HttpServletResponse response) {
        List<BizCategoryExcel> rows = listAll(q).stream().map(BizCategoryExcel::from).toList();
        ExcelUtils.export(response, rows, BizCategoryExcel.class, "商品分类列表", "商品分类");
    }

    @Transactional(rollbackFor = Exception.class)
    public ExcelImportResult importData(MultipartFile file) {
        return ExcelUtils.importExcel(file, BizCategoryExcel.class, this::validateRow, batch -> {
            for (BizCategoryExcel row : batch) {
                BizCategory entity = row.toEntity();
                BizCategory old = lambdaQuery().eq(BizCategory::getCategoryCode, row.getCategoryCode()).one();
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

    private String validateRow(BizCategoryExcel row) {
        if (StrUtil.isBlank(row.getCategoryCode())) return "CategoryCode不能为空";
        return null;
    }

}
