package com.enterprise.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.module.system.entity.SysDictType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysDictTypeMapper extends BaseMapper<SysDictType> {

    @Select("SELECT * FROM sys_dict_type WHERE dict_type = #{dictType} AND deleted = 0 LIMIT 1")
    SysDictType selectByType(@Param("dictType") String dictType);

}
