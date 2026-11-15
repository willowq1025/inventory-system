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
import com.enterprise.module.inventory.dto.BizStockFlowQuery;
import com.enterprise.module.inventory.entity.BizStockFlow;
import com.enterprise.module.inventory.excel.BizStockFlowExcel;
import com.enterprise.module.inventory.mapper.BizStockFlowMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 库存流水服务：分页搜索 / 审核留痕 / 缓存一致性 / Excel 导入导出。
 * 统一继承 ServiceImpl 复用 MyBatis-Plus 通用能力，业务差异只写在此处。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BizStockFlowService extends ServiceImpl<BizStockFlowMapper, BizStockFlow> {

    private final CacheConsistency cacheConsistency;

    public PageResult<BizStockFlow> page(BizStockFlowQuery q) {
        return toResult(baseMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), buildWrapper(q)));
    }

    public List<BizStockFlow> listAll(BizStockFlowQuery q) {
        return list(buildWrapper(q));
    }

    public BizStockFlow detail(Long id) {
        BizStockFlow entity = getById(id);
        if (entity == null) throw new BizException(ResultCode.DATA_NOT_FOUND, "库存流水不存在");
        return entity;
    }

    private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BizStockFlow> buildWrapper(BizStockFlowQuery q) {
        var w = Wrappers.lambdaQuery(BizStockFlow.class);
        if (StrUtil.isNotBlank(q.getKeyword())) {
            w.and(inner -> inner.like(BizStockFlow::getFlowNo, q.getKeyword()).or().like(BizStockFlow::getBizNo, q.getKeyword()));
        }
        w.eq(q.getProductId() != null, BizStockFlow::getProductId, q.getProductId());
        w.eq(q.getWarehouseId() != null, BizStockFlow::getWarehouseId, q.getWarehouseId());
        w.eq(q.getBizType() != null, BizStockFlow::getBizType, q.getBizType());
        w.ge(q.getStartTime() != null, BizStockFlow::getOperTime, q.getStartTime());
        w.le(q.getEndTime() != null, BizStockFlow::getOperTime, q.getEndTime());
        String orderBy = q.safeOrderBy();
        if (orderBy == null) {
            w.orderByDesc(BizStockFlow::getOperTime);
        } else {
            // safeOrderBy 已做正则 + 白名单校验，杜绝 order by 注入
            w.last("ORDER BY " + orderBy + (q.isAsc() ? " ASC" : " DESC"));
        }
        return w;
    }

    private PageResult<BizStockFlow> toResult(IPage<BizStockFlow> page) {
        return PageResult.of(page.getTotal(), page.getRecords(), page.getCurrent(), page.getSize());
    }


    public void export(BizStockFlowQuery q, HttpServletResponse response) {
        List<BizStockFlowExcel> rows = listAll(q).stream().map(BizStockFlowExcel::from).toList();
        ExcelUtils.export(response, rows, BizStockFlowExcel.class, "库存流水列表", "库存流水");
    }

}
