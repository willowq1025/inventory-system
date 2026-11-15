package com.enterprise.module.system.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.common.core.BizException;
import com.enterprise.common.core.PageResult;
import com.enterprise.common.core.ResultCode;
import com.enterprise.common.excel.util.ExcelUtils;
import com.enterprise.common.security.service.PermissionCacheService;
import com.enterprise.common.security.util.SecurityUtils;
import com.enterprise.module.system.dto.UserCmd;
import com.enterprise.module.system.dto.UserQuery;
import com.enterprise.module.system.entity.*;
import com.enterprise.module.system.mapper.*;
import com.enterprise.module.system.vo.UserVO;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysUserService extends ServiceImpl<SysUserMapper, SysUser> {

    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final SysDeptMapper deptMapper;
    private final PasswordEncoder passwordEncoder;
    private final PermissionCacheService permissionCache;

    public PageResult<SysUser> page(UserQuery q) {
        var w = Wrappers.lambdaQuery(SysUser.class)
                .like(StrUtil.isNotBlank(q.getUsername()), SysUser::getUsername, q.getUsername())
                .like(StrUtil.isNotBlank(q.getNickname()), SysUser::getNickname, q.getNickname())
                .like(StrUtil.isNotBlank(q.getPhone()), SysUser::getPhone, q.getPhone())
                .eq(q.getDeptId() != null, SysUser::getDeptId, q.getDeptId())
                .eq(q.getStatus() != null, SysUser::getStatus, q.getStatus())
                .ge(q.getStartTime() != null, SysUser::getCreateTime, q.getStartTime())
                .le(q.getEndTime() != null, SysUser::getCreateTime, q.getEndTime());
        String orderBy = q.safeOrderBy();
        if (orderBy == null) w.orderByDesc(SysUser::getCreateTime);
        else w.last("ORDER BY " + orderBy + (q.isAsc() ? " ASC" : " DESC"));
        var page = baseMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), w);
        // 不在列表接口暴露密码哈希
        page.getRecords().forEach(u -> u.setPassword(null));
        return PageResult.of(page.getTotal(), page.getRecords(), page.getCurrent(), page.getSize());
    }

    public UserVO detail(Long id) {
        SysUser user = getById(id);
        if (user == null) throw new BizException(ResultCode.DATA_NOT_FOUND, "用户不存在");
        UserVO vo = UserVO.from(user);
        List<Long> roleIds = userRoleMapper.selectList(
                        Wrappers.lambdaQuery(SysUserRole.class).eq(SysUserRole::getUserId, id))
                .stream().map(SysUserRole::getRoleId).toList();
        vo.setRoleIds(roleIds);
        if (!roleIds.isEmpty()) {
            vo.setRoleNames(roleMapper.selectBatchIds(roleIds).stream().map(SysRole::getRoleName).toList());
        }
        if (user.getDeptId() != null) {
            SysDept dept = deptMapper.selectById(user.getDeptId());
            if (dept != null) vo.setDeptName(dept.getDeptName());
        }
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(UserCmd cmd) {
        if (baseMapper.selectByUsername(cmd.getUsername()) != null) {
            throw new BizException(ResultCode.DATA_ALREADY_EXISTS, "账号已存在: " + cmd.getUsername());
        }
        SysUser user = new SysUser();
        user.setUsername(cmd.getUsername());
        user.setNickname(cmd.getNickname());
        user.setPassword(passwordEncoder.encode(cmd.getPassword()));
        user.setEmail(cmd.getEmail());
        user.setPhone(cmd.getPhone());
        user.setDeptId(cmd.getDeptId());
        user.setStatus(cmd.getStatus());
        user.setSex(cmd.getSex());
        user.setRemark(cmd.getRemark());
        save(user);
        bindRoles(user.getId(), cmd.getRoleIds());
        return user.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(UserCmd cmd) {
        SysUser user = getById(cmd.getId());
        if (user == null) throw new BizException(ResultCode.DATA_NOT_FOUND, "用户不存在");
        SysUser same = baseMapper.selectByUsername(cmd.getUsername());
        if (same != null && !same.getId().equals(cmd.getId())) {
            throw new BizException(ResultCode.DATA_ALREADY_EXISTS, "账号已存在");
        }
        SysUser update = new SysUser();
        update.setId(cmd.getId());
        update.setUsername(cmd.getUsername());
        update.setNickname(cmd.getNickname());
        update.setEmail(cmd.getEmail());
        update.setPhone(cmd.getPhone());
        update.setDeptId(cmd.getDeptId());
        update.setStatus(cmd.getStatus());
        update.setSex(cmd.getSex());
        update.setRemark(cmd.getRemark());
        updateById(update);
        if (cmd.getRoleIds() != null) {
            userRoleMapper.deleteByUserId(cmd.getId());
            bindRoles(cmd.getId(), cmd.getRoleIds());
        }
        permissionCache.evictUser(cmd.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        if (Objects.equals(id, SecurityUtils.userId())) {
            throw new BizException("不能删除当前登录账号");
        }
        SysUser user = getById(id);
        if (user != null && "admin".equals(user.getUsername())) {
            throw new BizException("超级管理员不可删除");
        }
        removeById(id);
        userRoleMapper.deleteByUserId(id);
        permissionCache.evictUser(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long id, String rawPassword) {
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "密码长度不能小于 6");
        }
        SysUser update = new SysUser();
        update.setId(id);
        update.setPassword(passwordEncoder.encode(rawPassword));
        updateById(update);
        permissionCache.evictUser(id);
    }

    public void export(UserQuery q, HttpServletResponse response) {
        q.setPageSize(500);
        List<UserVO> rows = page(q).getRecords().stream().map(UserVO::from).toList();
        ExcelUtils.export(response, rows, UserVO.class, "用户列表", "用户");
    }

    private void bindRoles(Long userId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) return;
        for (Long roleId : new HashSet<>(roleIds)) {
            SysUserRole ur = new SysUserRole();
            ur.setId(IdWorker.getId());
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            userRoleMapper.insert(ur);
        }
    }
}
