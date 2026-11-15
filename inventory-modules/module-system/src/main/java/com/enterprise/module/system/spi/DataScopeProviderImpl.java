package com.enterprise.module.system.spi;

import com.enterprise.common.core.CacheKey;
import com.enterprise.common.mybatis.datascope.DataScopeProvider;
import com.enterprise.common.redis.service.CacheService;
import com.enterprise.module.system.mapper.SysDeptMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

/** 本部门及以下：解析 sys_dept.ancestors，结果缓存并随部门变更失效。 */
@Service
@RequiredArgsConstructor
public class DataScopeProviderImpl implements DataScopeProvider {

    private final SysDeptMapper deptMapper;
    private final CacheService cacheService;

    @Override
    public List<Long> findDeptAndChildren(Long deptId) {
        if (deptId == null) return List.of();
        String key = CacheKey.DEPT_CHILDREN + deptId;
        List<Long> cached = cacheService.get(key, new com.fasterxml.jackson.core.type.TypeReference<>() {});
        if (cached != null) return cached;
        List<Long> ids = deptMapper.selectDeptAndChildren(deptId);
        List<Long> result = ids == null || ids.isEmpty() ? List.of(deptId) : ids;
        cacheService.set(key, result, Duration.ofHours(2));
        return result;
    }

    public void evict(Long deptId) {
        cacheService.evict(CacheKey.DEPT_CHILDREN + deptId);
    }
}
