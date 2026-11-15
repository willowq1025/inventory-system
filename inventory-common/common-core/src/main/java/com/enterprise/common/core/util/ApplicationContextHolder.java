package com.enterprise.common.core.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/** 供 SPI、工具类在非 Bean 环境获取 Spring Bean（谨慎使用）。 */
@Component
public class ApplicationContextHolder implements ApplicationContextAware {

    private static ApplicationContext CONTEXT;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        CONTEXT = applicationContext;
    }

    public static ApplicationContext context() { return CONTEXT; }

    public static <T> T getBean(Class<T> type) {
        return CONTEXT == null ? null : CONTEXT.getBean(type);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        return CONTEXT == null ? null : (T) CONTEXT.getBean(name);
    }
}
