package com.enterprise.module.system.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 字典项视图。id 供管理端编辑/删除定位；运行时缓存消费方只关心 label/value。
 */
@Data
public class DictVO implements Serializable {

    private Long id;
    private String label;
    private String value;
    private String listClass;
    private String cssClass;

    public DictVO() {
    }

    public DictVO(String label, String value, String listClass, String cssClass) {
        this.label = label;
        this.value = value;
        this.listClass = listClass;
        this.cssClass = cssClass;
    }

    public DictVO(Long id, String label, String value, String listClass, String cssClass) {
        this.id = id;
        this.label = label;
        this.value = value;
        this.listClass = listClass;
        this.cssClass = cssClass;
    }
}
