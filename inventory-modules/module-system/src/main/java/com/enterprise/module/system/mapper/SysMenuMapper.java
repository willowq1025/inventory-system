package com.enterprise.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.module.system.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    @Select("SELECT DISTINCT m.permission FROM sys_menu m " +
            "JOIN sys_role_menu rm ON rm.menu_id = m.id " +
            "JOIN sys_user_role ur ON ur.role_id = rm.role_id " +
            "WHERE ur.user_id = #{userId} AND m.permission IS NOT NULL AND m.permission <> '' " +
            "AND m.deleted = 0 AND m.status = 1")
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);

    @Select("SELECT DISTINCT m.* FROM sys_menu m " +
            "JOIN sys_role_menu rm ON rm.menu_id = m.id " +
            "JOIN sys_user_role ur ON ur.role_id = rm.role_id " +
            "WHERE ur.user_id = #{userId} AND m.menu_type IN ('M','C') AND m.deleted = 0 " +
            "AND m.status = 1 ORDER BY m.parent_id, m.sort")
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM sys_menu WHERE api_path IS NOT NULL AND api_path <> '' " +
            "AND permission IS NOT NULL AND permission <> '' AND menu_type = 'F' AND deleted = 0 AND status = 1")
    List<SysMenu> selectApiRules();

    @Select("SELECT COUNT(*) FROM sys_menu WHERE parent_id = #{menuId} AND deleted = 0")
    int countChildren(@Param("menuId") Long menuId);

}
