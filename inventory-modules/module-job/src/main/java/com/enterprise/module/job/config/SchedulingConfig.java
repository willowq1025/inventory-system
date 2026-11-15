package com.enterprise.module.job.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** 开启定时任务；集群互斥由 JobLeader 保证，同一时刻只有一个实例真正执行。 */
@Configuration
@EnableScheduling
public class SchedulingConfig {}
