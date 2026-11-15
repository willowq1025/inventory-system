package com.enterprise.module.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.common.core.CacheKey;
import com.enterprise.common.redis.service.CacheConsistency;
import com.enterprise.common.redis.service.CacheService;
import com.enterprise.module.system.entity.SysConfig;
import com.enterprise.module.system.mapper.SysConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 系统参数（预警阈值、Webhook 地址等）读取与缓存。 */
@Service
@RequiredArgsConstructor
public class SysConfigService extends ServiceImpl<SysConfigMapper, SysConfig> {

    private final CacheService cacheService;
    private final CacheConsistency cacheConsistency;

    public List<SysConfig> listAll() {
        return list(Wrappers.lambdaQuery(SysConfig.class).orderByAsc(SysConfig::getId));
    }

    public String get(String key, String defaultValue) {
        String cacheKey = CacheKey.CONFIG + key;
        String cached = cacheService.get(cacheKey, String.class);
        if (cached != null) return cached;
        SysConfig config = baseMapper.selectByKey(key);
        String value = config == null ? defaultValue : config.getConfigValue();
        cacheService.set(cacheKey, value);
        return value;
    }

    public int getInt(String key, int defaultValue) {
        try { return Integer.parseInt(get(key, String.valueOf(defaultValue))); }
        catch (NumberFormatException e) { return defaultValue; }
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, String value) {
        SysConfig config = new SysConfig();
        config.setId(id);
        config.setConfigValue(value);
        updateById(config);
        SysConfig full = getById(id);
        if (full != null) cacheConsistency.evictAfterCommit(CacheKey.CONFIG + full.getConfigKey());
    }
}
