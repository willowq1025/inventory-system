package com.enterprise.common.mybatis.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.enterprise.common.security.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** 创建人 / 创建部门 / 时间自动填充；create_dept 是数据权限的关键来源。 */
@Slf4j
@Component
public class AuditMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        Long userId = SecurityUtils.userId();
        Long deptId = SecurityUtils.deptId();
        fill(metaObject, "createTime", now);
        fill(metaObject, "createBy", userId);
        fill(metaObject, "createDept", deptId);
        fill(metaObject, "updateTime", now);
        fill(metaObject, "updateBy", userId);
        fill(metaObject, "deleted", 0);
        fill(metaObject, "version", 0);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        fill(metaObject, "updateTime", LocalDateTime.now(), true);
        fill(metaObject, "updateBy", SecurityUtils.userId(), true);
    }

    private void fill(MetaObject metaObject, String field, Object value) {
        fill(metaObject, field, value, false);
    }

    private void fill(MetaObject metaObject, String field, Object value, boolean force) {
        if (value == null || !metaObject.hasSetter(field)) return;
        if (force || metaObject.getValue(field) == null) {
            metaObject.setValue(field, value);
        }
    }
}
