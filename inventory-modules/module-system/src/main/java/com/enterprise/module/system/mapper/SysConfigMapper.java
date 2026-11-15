package com.enterprise.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.module.system.entity.SysConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysConfigMapper extends BaseMapper<SysConfig> {

    @Select("SELECT * FROM sys_config WHERE config_key = #{configKey} AND deleted = 0 LIMIT 1")
    SysConfig selectByKey(@Param("configKey") String configKey);

}
