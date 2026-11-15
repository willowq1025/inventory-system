package com.enterprise.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.module.system.entity.SysDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface SysDeptMapper extends BaseMapper<SysDept> {

    @Select("SELECT id FROM sys_dept WHERE deleted = 0 " +
            "AND (id = #{deptId} OR ancestors LIKE CONCAT('%,', #{deptId}, ',%') " +
            "OR ancestors LIKE CONCAT('%,', #{deptId}))")
    List<Long> selectDeptAndChildren(@Param("deptId") Long deptId);

    @Select("SELECT COUNT(*) FROM sys_user WHERE dept_id = #{deptId} AND deleted = 0")
    int countUsers(@Param("deptId") Long deptId);

}
