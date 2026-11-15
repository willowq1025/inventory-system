package com.enterprise.common.notify.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** 注册通知模块的 @ConfigurationProperties。 */
@Configuration
@EnableConfigurationProperties(NotifyProperties.class)
public class NotifyConfig {}
