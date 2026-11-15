package com.enterprise.common.mybatis.datascope;

import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
import com.enterprise.common.core.CommonConstant;
import com.enterprise.common.core.context.CurrentUser;
import com.enterprise.common.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 数据权限 SQL 拦截器核心：在 SQL 解析阶段自动追加过滤条件，业务代码零侵入。
 *
 * <p>范围映射：
 * <ul>
 *   <li>1 全部：不追加条件</li>
 *   <li>2 本部门及以下：create_dept IN (本部门 + 子孙部门)</li>
 *   <li>3 本部门：create_dept = 当前部门</li>
 *   <li>4 本人：create_by = 当前用户</li>
 *   <li>5 自定义：create_dept IN (角色绑定的部门)</li>
 * </ul>
 *
 * <p>安全约束：只对白名单内的业务表生效；无登录上下文（定时任务/内部调用）不追加条件，
 * 由调用方显式传参控制；可用 {@code @IgnoreDataScope} 主动跳过。
 */
@Slf4j
@RequiredArgsConstructor
public class DataPermissionHandlerImpl implements MultiDataPermissionHandler {

    /** 需要施加数据权限的表（均含 create_by / create_dept 字段） */
    private static final Set<String> SCOPE_TABLES = Set.of(
            "biz_purchase_order", "biz_sale_order", "biz_inventory_check", "biz_transfer",
            "biz_stock_flow", "biz_product", "biz_supplier", "biz_customer",
            "biz_category", "biz_warehouse", "sys_notice", "sys_user");

    /**
     * 延迟获取部门树 SPI：MyBatis-Plus 插件在 sqlSessionFactory 创建期装配，
     * 而该 SPI 的实现依赖 Mapper（进而依赖 sqlSessionFactory）。若在装配期直接持有实现 Bean，
     * 会形成 sqlSessionFactory ↔ interceptor ↔ Mapper 的循环依赖，应用将无法启动。
     * 用 Supplier 把解析推迟到真正执行 SQL 时，此时 Spring 容器已就绪。
     */
    private final Supplier<DataScopeProvider> dataScopeProvider;

    @Override
    public Expression getSqlSegment(Table table, Expression where, String mappedStatementId) {
        if (table == null || DataScopeContext.isIgnored()) return null;
        String tableName = table.getName().replace("`", "").toLowerCase(Locale.ROOT);
        if (!SCOPE_TABLES.contains(tableName)) return null;

        CurrentUser user = SecurityUtils.currentUser();
        if (user == null || user.isSuperAdmin()) return null;
        int scope = user.getDataScope() == null ? CommonConstant.SCOPE_SELF : user.getDataScope();
        if (scope == CommonConstant.SCOPE_ALL) return null;

        String condition = buildCondition(table, user, scope);
        if (condition == null) return null;
        try {
            // 通过解析器构造 AST，避免直接拼接 SQL 字符串带来的注入与语法风险
            return CCJSqlParserUtil.parseCondExpression(condition);
        } catch (Exception e) {
            // 失败必须「拒绝」而不是「放行」，避免数据越权
            log.error("[data-scope] 条件解析失败 condition={}", condition, e);
            try {
                return CCJSqlParserUtil.parseCondExpression("1 = 0");
            } catch (Exception fatal) {
                throw new IllegalStateException("数据权限条件构造失败", fatal);
            }
        }
    }

    private String buildCondition(Table table, CurrentUser user, int scope) {
        String prefix = table.getAlias() == null ? "" : table.getAlias().getName() + ".";
        String deptColumn = prefix + "create_dept";
        String byColumn = prefix + "create_by";

        return switch (scope) {
            case CommonConstant.SCOPE_SELF -> user.getUserId() == null
                    ? "1 = 0" : byColumn + " = " + user.getUserId();
            case CommonConstant.SCOPE_DEPT -> user.getDeptId() == null
                    ? "1 = 0" : deptColumn + " = " + user.getDeptId();
            case CommonConstant.SCOPE_DEPT_AND_CHILD -> {
                DataScopeProvider provider = dataScopeProvider == null ? null : dataScopeProvider.get();
                List<Long> ids = provider == null
                        ? (user.getDeptId() == null ? List.of() : List.of(user.getDeptId()))
                        : provider.findDeptAndChildren(user.getDeptId());
                yield inCondition(deptColumn, ids);
            }
            case CommonConstant.SCOPE_CUSTOM -> inCondition(deptColumn,
                    user.getCustomDeptIds() == null ? List.of() : List.copyOf(user.getCustomDeptIds()));
            default -> null;
        };
    }

    private String inCondition(String column, List<Long> ids) {
        if (ids == null || ids.isEmpty()) return "1 = 0";
        return column + " IN (" + ids.stream().map(String::valueOf).collect(Collectors.joining(",")) + ")";
    }

    /** 兼容旧版 DataPermissionHandler 的 2 参数调用（一般会走 3 参数重载） */
    @Override
    public Expression getSqlSegment(Expression where, String mappedStatementId) {
        return null;
    }
}
