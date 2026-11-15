package com.enterprise.inventory;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.enterprise.common.core.BizException;
import com.enterprise.common.core.ResultCode;
import com.enterprise.common.redis.service.DistributedLockTemplate;
import com.enterprise.module.inventory.dto.StockChangeCmd;
import com.enterprise.module.inventory.entity.BizStock;
import com.enterprise.module.inventory.entity.BizStockFlow;
import com.enterprise.module.inventory.mapper.BizStockFlowMapper;
import com.enterprise.module.inventory.mapper.BizStockMapper;
import com.enterprise.module.inventory.service.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

/**
 * 库存扣减核心单元测试：验证「DB 原子条件更新」这道最终防线。
 * 真实并发由 MySQL 行锁 + available_quantity >= 扣减量 保证，这里验证分支语义。
 */
@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock private BizStockMapper stockMapper;
    @Mock private BizStockFlowMapper flowMapper;
    @Mock private DistributedLockTemplate lockTemplate;
    @Mock private TransactionTemplate transactionTemplate;
    @InjectMocks private StockService stockService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(stockService, "baseMapper", stockMapper);
    }

    @Test
    @DisplayName("库存不足：原子 UPDATE 返回 0，抛业务异常且不写流水")
    void shouldRejectWhenStockNotEnough() {
        when(stockMapper.selectOne(any(), anyBoolean())).thenReturn(null);
        when(stockMapper.changeQuantity(1L, 2L, -10L)).thenReturn(0);

        StockChangeCmd cmd = StockChangeCmd.of(1L, 2L, -10L, "SALE_OUT", "XS-1", null);

        assertThatThrownBy(() -> stockService.changeInTransaction(cmd))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("库存不足");
        verify(flowMapper, never()).insert(any(BizStockFlow.class));
    }

    @Test
    @DisplayName("扣减成功：写入可追溯库存流水")
    void shouldWriteFlowOnSuccess() {
        when(stockMapper.selectOne(any(), anyBoolean())).thenReturn(null);
        when(stockMapper.changeQuantity(1L, 2L, -5L)).thenReturn(1);
        when(flowMapper.insert(any(BizStockFlow.class))).thenReturn(1);

        stockService.changeInTransaction(StockChangeCmd.of(1L, 2L, -5L, "SALE_OUT", "XS-2", null));

        var captor = org.mockito.ArgumentCaptor.forClass(BizStockFlow.class);
        verify(flowMapper).insert(captor.capture());
        BizStockFlow flow = captor.getValue();
        assertThat(flow.getQuantityChange()).isEqualTo(-5L);
        assertThat(flow.getBizType()).isEqualTo("SALE_OUT");
        assertThat(flow.getBizNo()).isEqualTo("XS-2");
    }

    @Test
    @DisplayName("连续扣减：第二次 DB 判定库存不足时整体失败")
    void shouldRejectSecondDeductWhenExhausted() {
        when(stockMapper.selectOne(any(), anyBoolean())).thenReturn(null);
        when(stockMapper.changeQuantity(1L, 2L, -5L)).thenReturn(1, 0);
        when(flowMapper.insert(any(BizStockFlow.class))).thenReturn(1);

        StockChangeCmd cmd = StockChangeCmd.of(1L, 2L, -5L, "SALE_OUT", "XS-3", null);
        stockService.changeInTransaction(cmd);

        assertThatThrownBy(() -> stockService.changeInTransaction(cmd))
                .isInstanceOf(BizException.class);
        verify(flowMapper, times(1)).insert(any(BizStockFlow.class));
    }

    @Test
    @DisplayName("数量为 0 的变动被参数校验拦截")
    void shouldRejectZeroQuantity() {
        assertThatThrownBy(() -> stockService.changeInTransaction(
                StockChangeCmd.of(1L, 2L, 0L, "ADJUST", null, null)))
                .isInstanceOf(BizException.class);
        verifyNoInteractions(flowMapper);
    }
}
