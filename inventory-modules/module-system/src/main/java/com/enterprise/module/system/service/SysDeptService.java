package com.enterprise.module.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.common.core.BizException;
import com.enterprise.common.core.CacheKey;
import com.enterprise.common.core.ResultCode;
import com.enterprise.common.redis.service.CacheConsistency;
import com.enterprise.module.system.dto.DeptCmd;
import com.enterprise.module.system.entity.SysDept;
import com.enterprise.module.system.mapper.SysDeptMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysDeptService extends ServiceImpl<SysDeptMapper, SysDept> {

    private final CacheConsistency cacheConsistency;

    public List<SysDept> tree() {
        return list(Wrappers.lambdaQuery(SysDept.class).orderByAsc(SysDept::getParentId, SysDept::getSort));
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(DeptCmd cmd) {
        SysDept dept = toEntity(cmd);
        dept.setAncestors(buildAncestors(cmd.getParentId()));
        save(dept);
        evictDeptCache();
        return dept.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(DeptCmd cmd) {
        if (cmd.getId() == null) throw new BizException(ResultCode.VALIDATION_ERROR, "部门ID不能为空");
        if (cmd.getId().equals(cmd.getParentId())) throw new BizException("上级部门不能是自己");
        SysDept dept = toEntity(cmd);
        dept.setAncestors(buildAncestors(cmd.getParentId()));
        updateById(dept);
        evictDeptCache();
    }

    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        if (baseMapper.countUsers(id) > 0) throw new BizException(ResultCode.CONFLICT, "该部门下存在用户，无法删除");
        removeById(id);
        evictDeptCache();
    }

    private SysDept toEntity(DeptCmd cmd) {
        SysDept dept = new SysDept();
        dept.setId(cmd.getId());
        dept.setParentId(cmd.getParentId());
        dept.setDeptName(cmd.getDeptName());
        dept.setDeptCode(cmd.getDeptCode());
        dept.setLeader(cmd.getLeader());
        dept.setPhone(cmd.getPhone());
        dept.setEmail(cmd.getEmail());
        dept.setSort(cmd.getSort());
        dept.setStatus(cmd.getStatus());
        dept.setRemark(cmd.getRemark());
        return dept;
    }

    private String buildAncestors(Long parentId) {
        if (parentId == null || parentId == 0L) return "0";
        SysDept parent = getById(parentId);
        return parent == null ? "0" : parent.getAncestors() + "," + parent.getId();
    }

    private void evictDeptCache() {
        cacheConsistency.evictPrefixAfterCommit(CacheKey.DEPT_CHILDREN);
    }
}
