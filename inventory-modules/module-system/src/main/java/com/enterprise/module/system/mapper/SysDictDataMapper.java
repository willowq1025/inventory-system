package com.enterprise.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.module.system.entity.SysDictData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface SysDictDataMapper extends BaseMapper<SysDictData> {

    @Select("SELECT * FROM sys_dict_data WHERE dict_type = #{dictType} AND deleted = 0 AND status = 1 ORDER BY sort")
    List<SysDictData> selectByType(@Param("dictType") String dictType);

}
