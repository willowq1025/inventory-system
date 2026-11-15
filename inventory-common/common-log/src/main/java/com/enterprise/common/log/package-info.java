/**
 * common-log：traceId 全链路追踪、全局请求日志、审计日志（谁在什么时间做了什么）。
 * 落库通过 SPI 交给 module-system，保证日志模块不依赖业务表。
 */
package com.enterprise.common.log;
