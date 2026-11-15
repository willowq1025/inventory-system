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
import com.enterprise.module.inventory.dto.BizCustomerQuery;
import com.enterprise.module.inventory.entity.BizCustomer;
import com.enterprise.module.inventory.excel.BizCustomerExcel;
import com.enterprise.module.inventory.mapper.BizCustomerMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 客户服务：分页搜索 / 审核留痕 / 缓存一致性 / Excel 导入导出。
 * 统一继承 ServiceImpl 复用 MyBatis-Plus 通用能力，业务差异只写在此处。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BizCustomerService extends ServiceImpl<BizCustomerMapper, BizCustomer> {

    private final CacheConsistency cacheConsistency;

    public PageResult<BizCustomer> page(BizCustomerQuery q) {
        return toResult(baseMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), buildWrapper(q)));
    }

    public List<BizCustomer> listAll(BizCustomerQuery q) {
        return list(buildWrapper(q));
    }

    public BizCustomer detail(Long id) {
        BizCustomer entity = getById(id);
        if (entity == null) throw new BizException(ResultCode.DATA_NOT_FOUND, "客户不存在");
        return entity;
    }

    private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BizCustomer> buildWrapper(BizCustomerQuery q) {
        var w = Wrappers.lambdaQuery(BizCustomer.class);
        if (StrUtil.isNotBlank(q.getKeyword())) {
            w.and(inner -> inner.like(BizCustomer::getCustomerName, q.getKeyword()).or().like(BizCustomer::getCustomerCode, q.getKeyword()).or().like(BizCustomer::getContact, q.getKeyword()));
        }
        w.eq(q.getStatus() != null, BizCustomer::getStatus, q.getStatus());
        w.ge(q.getStartTime() != null, BizCustomer::getCreateTime, q.getStartTime());
        w.le(q.getEndTime() != null, BizCustomer::getCreateTime, q.getEndTime());
        String orderBy = q.safeOrderBy();
        if (orderBy == null) {
            w.orderByDesc(BizCustomer::getCreateTime);
        } else {
            // safeOrderBy 已做正则 + 白名单校验，杜绝 order by 注入
            w.last("ORDER BY " + orderBy + (q.isAsc() ? " ASC" : " DESC"));
        }
        return w;
    }

    private PageResult<BizCustomer> toResult(IPage<BizCustomer> page) {
        return PageResult.of(page.getTotal(), page.getRecords(), page.getCurrent(), page.getSize());
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(BizCustomer entity) {
        BizCustomer exists = lambdaQuery().eq(BizCustomer::getCustomerCode, entity.getCustomerCode()).one();
        if (exists != null && !exists.getId().equals(entity.getId())) {
            throw new BizException(ResultCode.DATA_ALREADY_EXISTS, "CustomerCode已存在: " + entity.getCustomerCode());
        }
        entity.setId(null);
        save(entity);
        return entity.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(BizCustomer entity) {
        if (entity.getId() == null) throw new BizException(ResultCode.VALIDATION_ERROR, "ID不能为空");
        BizCustomer exists = lambdaQuery().eq(BizCustomer::getCustomerCode, entity.getCustomerCode()).one();
        if (exists != null && !exists.getId().equals(entity.getId())) {
            throw new BizException(ResultCode.DATA_ALREADY_EXISTS, "CustomerCode已存在: " + entity.getCustomerCode());
        }
        updateById(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        detail(id);
        removeById(id);
    }

    public void export(BizCustomerQuery q, HttpServletResponse response) {
        List<BizCustomerExcel> rows = listAll(q).stream().map(BizCustomerExcel::from).toList();
        ExcelUtils.export(response, rows, BizCustomerExcel.class, "客户列表", "客户");
    }

    @Transactional(rollbackFor = Exception.class)
    public ExcelImportResult importData(MultipartFile file) {
        return ExcelUtils.importExcel(file, BizCustomerExcel.class, this::validateRow, batch -> {
            for (BizCustomerExcel row : batch) {
                BizCustomer entity = row.toEntity();
                BizCustomer old = lambdaQuery().eq(BizCustomer::getCustomerCode, row.getCustomerCode()).one();
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

    private String validateRow(BizCustomerExcel row) {
        if (StrUtil.isBlank(row.getCustomerCode())) return "CustomerCode不能为空";
        return null;
    }

}
