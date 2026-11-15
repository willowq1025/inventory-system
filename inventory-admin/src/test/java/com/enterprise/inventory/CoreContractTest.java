package com.enterprise.inventory;

import com.enterprise.common.core.BizAssert;
import com.enterprise.common.core.BizException;
import com.enterprise.common.core.PageQuery;
import com.enterprise.common.core.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 统一响应、参数校验与排序注入防护。 */
class CoreContractTest {

    @Test
    @DisplayName("统一响应携带 traceId 与时间戳")
    void resultCarriesTraceId() {
        MDC.put("traceId", "trace-001");
        try {
            Result<String> result = Result.ok("hello");
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isEqualTo("hello");
            assertThat(result.getTraceId()).isEqualTo("trace-001");
            assertThat(result.getTimestamp()).isPositive();
        } finally {
            MDC.clear();
        }
    }

    @Test
    @DisplayName("order by 注入被正则与白名单双重拦截")
    void orderByInjectionBlocked() {
        PageQuery query = new PageQuery();
        query.setOrderBy("id; DROP TABLE sys_user");
        assertThatThrownBy(query::safeOrderBy).isInstanceOf(BizException.class);

        query.setOrderBy("create_time");
        assertThat(query.safeOrderBy()).isEqualTo("create_time");
    }

    @Test
    @DisplayName("业务断言抛出 BizException")
    void bizAssertWorks() {
        assertThatThrownBy(() -> BizAssert.notBlank("", "名称不能为空"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("名称不能为空");
        assertThat(BizAssert.requireNotNull("x", "不能为空")).isEqualTo("x");
    }
}
