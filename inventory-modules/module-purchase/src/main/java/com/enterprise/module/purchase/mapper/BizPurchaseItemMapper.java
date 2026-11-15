package com.enterprise.module.purchase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.module.purchase.entity.BizPurchaseItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;

@Mapper
public interface BizPurchaseItemMapper extends BaseMapper<BizPurchaseItem> {

    @Delete("DELETE FROM biz_purchase_item WHERE order_id = #{orderId}")
    int deleteByOrderId(@Param("orderId") Long orderId);

}
