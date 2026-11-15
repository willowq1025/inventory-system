package com.enterprise.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.module.system.entity.SysRoleDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;
import java.util.List;

@Mapper
public interface SysRoleDeptMapper extends BaseMapper<SysRoleDept> {

    @Select("SELECT dept_id FROM sys_role_dept WHERE role_id = #{roleId}")
    List<Long> selectDeptIdsByRoleId(@Param("roleId") Long roleId);

    @Delete("DELETE FROM sys_role_dept WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);

}
