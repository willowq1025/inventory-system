package com.enterprise.common.mybatis.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.enterprise.common.mybatis.datascope.DataPermissionHandlerImpl;
import com.enterprise.common.mybatis.datascope.DataScopeProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 插件顺序（MP 官方推荐）：数据权限 → 分页 → 乐观锁 → 防全表更新/删除。
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 必须用 ObjectProvider 延迟解析 DataScopeProvider：该 SPI 实现依赖 Mapper，
     * 而 Mapper 依赖 sqlSessionFactory，sqlSessionFactory 又依赖本拦截器。
     * 装配期直接注入实现会形成循环依赖（allow-circular-references=false 时启动失败）；
     * ObjectProvider 只保存引用，执行 SQL 时才解析目标 Bean，此时容器已就绪。
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(ObjectProvider<DataScopeProvider> dataScopeProvider) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 1. 数据权限：SQL 解析阶段按登录人范围自动追加条件
        interceptor.addInnerInterceptor(new DataPermissionInterceptor(
                new DataPermissionHandlerImpl(dataScopeProvider::getIfAvailable)));

        // 2. 分页（物理分页，限制单页上限）
        PaginationInnerInterceptor pagination = new PaginationInnerInterceptor(DbType.MYSQL);
        pagination.setMaxLimit(500L);
        pagination.setOverflow(false);
        pagination.setOptimizeJoin(true);
        interceptor.addInnerInterceptor(pagination);

        // 3. 乐观锁（库存 version 字段）
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 4. 阻断不带 where 的 update / delete
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        return interceptor;
    }

    /**
     * 编程式事务：库存扣减、单据流转等需要「先加分布式锁 → 再开事务 → 提交后释放锁」，
     * 声明式 @Transactional 的切面顺序无法满足该要求，故统一使用 TransactionTemplate。
     */
    @Bean
    @ConditionalOnMissingBean(TransactionTemplate.class)
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }
}
