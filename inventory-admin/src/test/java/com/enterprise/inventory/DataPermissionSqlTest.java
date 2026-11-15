package com.enterprise.inventory;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 数据权限 SQL 片段构造所使用的解析器行为验证。 */
class DataPermissionSqlTest {

    @Test
    @DisplayName("合法条件可解析为 SQL 表达式")
    void validConditions() throws Exception {
        assertThat(CCJSqlParserUtil.parseCondExpression("create_by = 1001").toString())
                .contains("create_by");
        assertThat(CCJSqlParserUtil.parseCondExpression("create_dept IN (100,101,103)").toString())
                .contains("IN");
        assertThat(CCJSqlParserUtil.parseCondExpression("1 = 0").toString()).contains("1 = 0");
    }

    @Test
    @DisplayName("拒绝条件(1 = 0)与部门 IN 条件均可被 SQL 解析器构造")
    void denyAndInConditions() throws Exception {
        assertThat(CCJSqlParserUtil.parseCondExpression("1 = 0").toString()).contains("1 = 0");
        assertThat(CCJSqlParserUtil.parseCondExpression("create_dept IN (100,101)").toString())
                .contains("100").contains("101");
    }
}
