package com.enterprise.common.mybatis.datascope;

import java.util.List;

/** 部门树 SPI：由 module-system 基于 sys_dept.ancestors 实现并缓存。 */
public interface DataScopeProvider {

    /** 返回部门自身 + 全部子孙部门 ID */
    List<Long> findDeptAndChildren(Long deptId);
}
