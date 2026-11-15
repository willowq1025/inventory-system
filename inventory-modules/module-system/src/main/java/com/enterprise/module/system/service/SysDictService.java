package com.enterprise.module.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.common.core.CacheKey;
import com.enterprise.common.redis.service.CacheConsistency;
import com.enterprise.common.redis.service.CacheService;
import com.enterprise.module.system.entity.SysDictData;
import com.enterprise.module.system.entity.SysDictType;
import com.enterprise.module.system.mapper.SysDictDataMapper;
import com.enterprise.module.system.mapper.SysDictTypeMapper;
import com.enterprise.module.system.vo.DictVO;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 字典缓存示例：读走 Cache-Aside，写走「事务提交后删缓存」，
 * 保证字典修改后不会出现「DB 已改但缓存仍旧」或「事务回滚缓存被删」。
 */
@Service
@RequiredArgsConstructor
public class SysDictService {

    private final SysDictTypeMapper typeMapper;
    private final SysDictDataMapper dataMapper;
    private final CacheService cacheService;
    private final CacheConsistency cacheConsistency;

    public List<SysDictType> types() {
        return typeMapper.selectList(Wrappers.lambdaQuery(SysDictType.class)
                .orderByAsc(SysDictType::getId));
    }

    public List<DictVO> byType(String dictType) {
        String key = CacheKey.DICT_TYPE + dictType;
        List<DictVO> cached = cacheService.get(key, new TypeReference<>() {});
        if (cached != null) return cached;
        List<DictVO> list = dataMapper.selectByType(dictType).stream()
                .map(d -> new DictVO(d.getId(), d.getDictLabel(), d.getDictValue(), d.getListClass(), d.getCssClass()))
                .toList();
        cacheService.set(key, list);
        return list;
    }

    public Map<String, List<DictVO>> all() {
        Map<String, List<DictVO>> result = new LinkedHashMap<>();
        for (SysDictType type : types()) result.put(type.getDictType(), byType(type.getDictType()));
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveData(SysDictData data) {
        if (data.getId() == null) dataMapper.insert(data);
        else dataMapper.updateById(data);
        cacheConsistency.evictAfterCommit(CacheKey.DICT_TYPE + data.getDictType());
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeData(Long id) {
        SysDictData data = dataMapper.selectById(id);
        if (data == null) return;
        dataMapper.deleteById(id);
        cacheConsistency.evictAfterCommit(CacheKey.DICT_TYPE + data.getDictType());
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveType(SysDictType type) {
        if (type.getId() == null) typeMapper.insert(type);
        else typeMapper.updateById(type);
    }
}
