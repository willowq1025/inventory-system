package com.enterprise.module.sale.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.module.sale.entity.BizSaleItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;

@Mapper
public interface BizSaleItemMapper extends BaseMapper<BizSaleItem> {

    @Delete("DELETE FROM biz_sale_item WHERE order_id = #{orderId}")
    int deleteByOrderId(@Param("orderId") Long orderId);

}
