package com.enterprise.module.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.module.inventory.entity.BizProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BizProductMapper extends BaseMapper<BizProduct> {

    @Select("SELECT * FROM biz_product WHERE product_code = #{code} AND deleted = 0 LIMIT 1")
    BizProduct selectByCode(@Param("code") String code);

}
