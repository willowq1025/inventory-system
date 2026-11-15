package com.enterprise.module.system.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** 前端动态路由节点（由 sys_menu 生成，Vue Router 按 component 字符串动态加载）。 */
@Data
public class RouteVO {

    private Long id;
    private String path;
    private String name;
    private String component;
    private String redirect;
    private Map<String, Object> meta;
    private List<RouteVO> children = new ArrayList<>();
}
