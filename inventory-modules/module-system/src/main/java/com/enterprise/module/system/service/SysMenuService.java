package com.enterprise.module.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.common.core.BizException;
import com.enterprise.common.core.ResultCode;
import com.enterprise.common.security.service.PermissionCacheService;
import com.enterprise.module.system.dto.MenuCmd;
import com.enterprise.module.system.entity.SysMenu;
import com.enterprise.module.system.mapper.SysMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 菜单 + 接口权限点（menu_type=F 的行即受控接口）。 */
@Service
@RequiredArgsConstructor
public class SysMenuService extends ServiceImpl<SysMenuMapper, SysMenu> {

    private final PermissionCacheService permissionCache;

    /** 平铺返回，前端按 parentId 组装树 */
    public List<SysMenu> tree() {
        return list(Wrappers.lambdaQuery(SysMenu.class)
                .orderByAsc(SysMenu::getParentId, SysMenu::getSort));
    }

    public List<SysMenu> permissionTree() {
        return tree();
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(MenuCmd cmd) {
        SysMenu entity = toEntity(cmd);
        save(entity);
        invalidate();
        return entity.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(MenuCmd cmd) {
        if (cmd.getId() == null) throw new BizException(ResultCode.VALIDATION_ERROR, "菜单ID不能为空");
        if (cmd.getId().equals(cmd.getParentId())) throw new BizException("上级菜单不能是自己");
        updateById(toEntity(cmd));
        invalidate();
    }

    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        if (baseMapper.countChildren(id) > 0) {
            throw new BizException(ResultCode.CONFLICT, "存在子菜单，请先删除子菜单");
        }
        removeById(id);
        invalidate();
    }

    /** 菜单/接口规则变更 → 清空全部用户权限缓存，下一次请求重新加载 */
    private void invalidate() {
        permissionCache.evictApiRules();
        permissionCache.evictAllUsers();
    }

    private SysMenu toEntity(MenuCmd cmd) {
        SysMenu m = new SysMenu();
        m.setId(cmd.getId());
        m.setParentId(cmd.getParentId());
        m.setMenuName(cmd.getMenuName());
        m.setMenuType(cmd.getMenuType());
        m.setPath(cmd.getPath());
        m.setComponent(cmd.getComponent());
        m.setRouteName(cmd.getRouteName());
        m.setIcon(cmd.getIcon());
        m.setPermission(cmd.getPermission());
        m.setApiPath(cmd.getApiPath());
        m.setApiMethod(cmd.getApiMethod());
        m.setSort(cmd.getSort());
        m.setVisible(cmd.getVisible());
        m.setStatus(cmd.getStatus());
        m.setRemark(cmd.getRemark());
        return m;
    }
}
