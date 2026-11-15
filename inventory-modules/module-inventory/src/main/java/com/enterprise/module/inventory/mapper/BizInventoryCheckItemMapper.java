package com.enterprise.module.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.module.inventory.entity.BizInventoryCheckItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;

@Mapper
public interface BizInventoryCheckItemMapper extends BaseMapper<BizInventoryCheckItem> {

    @Delete("DELETE FROM biz_inventory_check_item WHERE check_id = #{checkId}")
    int deleteByCheckId(@Param("checkId") Long checkId);

}
