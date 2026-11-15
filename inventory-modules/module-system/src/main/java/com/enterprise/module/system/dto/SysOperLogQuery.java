package com.enterprise.module.system.dto;

import com.enterprise.common.core.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysOperLogQuery extends PageQuery {

    /** 业务类型 */
    private String businessType;

    /** 状态 */
    private Integer status;

    /** 操作人 */
    private Long operId;

    @Override
    protected Set<String> allowedOrderColumns() {
        return Set.of("business_type", "id", "oper_id", "oper_time", "status");
    }
}
