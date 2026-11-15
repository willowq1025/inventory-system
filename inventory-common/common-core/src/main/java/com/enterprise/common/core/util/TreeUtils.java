package com.enterprise.common.core.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

/** 通用树构建：菜单树、部门树、分类树共用。 */
public final class TreeUtils {

    private TreeUtils() {}

    public static <T, K> List<T> build(List<T> all,
                                       Function<T, K> idGetter,
                                       Function<T, K> parentGetter,
                                       BiConsumer<T, List<T>> childrenSetter,
                                       K rootId) {
        Map<K, List<T>> childrenMap = new LinkedHashMap<>();
        for (T node : all) {
            childrenMap.computeIfAbsent(parentGetter.apply(node), k -> new ArrayList<>()).add(node);
        }
        for (T node : all) {
            List<T> children = childrenMap.get(idGetter.apply(node));
            childrenSetter.accept(node, children == null ? new ArrayList<>() : children);
        }
        List<T> roots = childrenMap.get(rootId);
        return roots == null ? new ArrayList<>() : roots;
    }

    /** 收集某节点及全部子孙 ID，用于删除校验、部门数据权限 */
    public static <T, K> List<K> collectDescendants(List<T> all, K targetId,
                                                    Function<T, K> idGetter,
                                                    Function<T, K> parentGetter) {
        List<K> result = new ArrayList<>();
        result.add(targetId);
        boolean changed;
        do {
            changed = false;
            for (T node : all) {
                K parent = parentGetter.apply(node);
                K id = idGetter.apply(node);
                if (result.stream().anyMatch(r -> Objects.equals(r, parent)) && result.stream().noneMatch(r -> Objects.equals(r, id))) {
                    result.add(id);
                    changed = true;
                }
            }
        } while (changed);
        return result;
    }
}
