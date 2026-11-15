package com.enterprise.module.sale.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.module.sale.entity.BizSaleOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Mapper
public interface BizSaleOrderMapper extends BaseMapper<BizSaleOrder> {

    @Select("SELECT COALESCE(SUM(total_amount),0) FROM biz_sale_order " +
            "WHERE deleted = 0 AND status = 3 AND create_time >= #{start} AND create_time < #{end}")
    BigDecimal sumAmount(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Select("SELECT COUNT(*) FROM biz_sale_order " +
            "WHERE deleted = 0 AND status = 3 AND create_time >= #{start} AND create_time < #{end}")
    int countFinished(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

}
