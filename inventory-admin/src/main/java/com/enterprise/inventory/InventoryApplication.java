package com.enterprise.inventory;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 应用启动入口。
 * scanBasePackages 覆盖全部模块；MapperScan 统一注册所有模块的 Mapper 接口。
 */
@SpringBootApplication(scanBasePackages = "com.enterprise")
@MapperScan("com.enterprise.**.mapper")
@EnableAsync
@EnableScheduling
public class InventoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryApplication.class, args);
    }
}
