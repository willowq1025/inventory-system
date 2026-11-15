package com.enterprise.module.system.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.common.core.BizException;
import com.enterprise.common.core.PageResult;
import com.enterprise.common.core.ResultCode;
import com.enterprise.common.security.service.PermissionCacheService;
import com.enterprise.module.system.dto.RoleCmd;
import com.enterprise.module.system.dto.RoleQuery;
import com.enterprise.module.system.entity.*;
import com.enterprise.module.system.mapper.*;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/** 角色 + 菜单/接口权限授权 + 自定义数据权限部门。 */
@Service
@RequiredArgsConstructor
public class SysRoleService extends ServiceImpl<SysRoleMapper, SysRole> {

    private final SysRoleMenuMapper roleMenuMapper;
    private final SysRoleDeptMapper roleDeptMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PermissionCacheService permissionCache;

    public PageResult<SysRole> page(RoleQuery q) {
        var w = Wrappers.lambdaQuery(SysRole.class)
                .like(StrUtil.isNotBlank(q.getRoleName()), SysRole::getRoleName, q.getRoleName())
                .like(StrUtil.isNotBlank(q.getRoleKey()), SysRole::getRoleKey, q.getRoleKey())
                .eq(q.getStatus() != null, SysRole::getStatus, q.getStatus());
        String orderBy = q.safeOrderBy();
        if (orderBy == null) w.orderByAsc(SysRole::getSort);
        else w.last("ORDER BY " + orderBy + (q.isAsc() ? " ASC" : " DESC"));
        var page = baseMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), w);
        return PageResult.of(page.getTotal(), page.getRecords(), page.getCurrent(), page.getSize());
    }

    public List<SysRole> listAll() {
        return list(Wrappers.lambdaQuery(SysRole.class).orderByAsc(SysRole::getSort));
    }

    /** 详情回显：角色信息 + 已授权菜单 + 自定义部门 */
    public RoleCmd detail(Long id) {
        SysRole role = getById(id);
        if (role == null) throw new BizException(ResultCode.DATA_NOT_FOUND, "角色不存在");
        RoleCmd cmd = new RoleCmd();
        cmd.setId(role.getId());
        cmd.setRoleName(role.getRoleName());
        cmd.setRoleKey(role.getRoleKey());
        cmd.setDataScope(role.getDataScope());
        cmd.setSort(role.getSort());
        cmd.setStatus(role.getStatus());
        cmd.setRemark(role.getRemark());
        cmd.setMenuIds(roleMenuMapper.selectMenuIdsByRoleId(id));
        cmd.setDeptIds(roleDeptMapper.selectDeptIdsByRoleId(id));
        return cmd;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(RoleCmd cmd) {
        SysRole exists = lambdaQuery().eq(SysRole::getRoleKey, cmd.getRoleKey()).one();
        if (exists != null) throw new BizException(ResultCode.DATA_ALREADY_EXISTS, "角色标识已存在");
        SysRole role = toEntity(cmd);
        save(role);
        saveGrant(role.getId(), cmd);
        return role.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(RoleCmd cmd) {
        SysRole exists = lambdaQuery().eq(SysRole::getRoleKey, cmd.getRoleKey()).one();
        if (exists != null && !exists.getId().equals(cmd.getId())) {
            throw new BizException(ResultCode.DATA_ALREADY_EXISTS, "角色标识已存在");
        }
        updateById(toEntity(cmd));
        saveGrant(cmd.getId(), cmd);
        evictRoleUsers(cmd.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        SysRole role = getById(id);
        if (role != null && "admin".equals(role.getRoleKey())) {
            throw new BizException("超级管理员角色不可删除");
        }
        removeById(id);
        roleMenuMapper.deleteByRoleId(id);
        roleDeptMapper.deleteByRoleId(id);
        evictRoleUsers(id);
    }

    /** 单独分配权限（菜单/接口 + 自定义数据部门） */
    @Transactional(rollbackFor = Exception.class)
    public void grant(Long roleId, List<Long> menuIds, List<Long> deptIds) {
        RoleCmd cmd = new RoleCmd();
        cmd.setMenuIds(menuIds);
        cmd.setDeptIds(deptIds);
        saveGrant(roleId, cmd);
        permissionCache.evictApiRules();
        evictRoleUsers(roleId);
    }

    private void saveGrant(Long roleId, RoleCmd cmd) {
        if (cmd.getMenuIds() != null) {
            roleMenuMapper.deleteByRoleId(roleId);
            for (Long menuId : new HashSet<>(cmd.getMenuIds())) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setId(IdWorker.getId());
                rm.setRoleId(roleId);
                rm.setMenuId(menuId);
                roleMenuMapper.insert(rm);
            }
            permissionCache.evictApiRules();
        }
        if (cmd.getDeptIds() != null) {
            roleDeptMapper.deleteByRoleId(roleId);
            for (Long deptId : new HashSet<>(cmd.getDeptIds())) {
                SysRoleDept rd = new SysRoleDept();
                rd.setId(IdWorker.getId());
                rd.setRoleId(roleId);
                rd.setDeptId(deptId);
                roleDeptMapper.insert(rd);
            }
        }
    }

    /** 权限变更 → 该角色下所有在线用户的权限缓存立即失效 */
    private void evictRoleUsers(Long roleId) {
        List<Long> userIds = userRoleMapper.selectList(
                        Wrappers.lambdaQuery(SysUserRole.class).eq(SysUserRole::getRoleId, roleId))
                .stream().map(SysUserRole::getUserId).toList();
        permissionCache.evictUsers(userIds);
    }

    private SysRole toEntity(RoleCmd cmd) {
        SysRole role = new SysRole();
        role.setId(cmd.getId());
        role.setRoleName(cmd.getRoleName());
        role.setRoleKey(cmd.getRoleKey());
        role.setDataScope(cmd.getDataScope());
        role.setSort(cmd.getSort());
        role.setStatus(cmd.getStatus());
        role.setRemark(cmd.getRemark());
        return role;
    }
}
