package com.enterprise.module.inventory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** 基于 Redis INCR 的日粒度单据号生成器，集群安全且可读。 */
@Service
@RequiredArgsConstructor
public class OrderNoGenerator {

    private final StringRedisTemplate redis;

    public String next(String prefix) {
        String day = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String key = "inv:seq:" + prefix + ":" + day;
        Long seq = redis.opsForValue().increment(key);
        if (seq != null && seq == 1L) redis.expire(key, Duration.ofDays(2));
        long value = seq == null ? System.nanoTime() % 1_000_000 : seq;
        return prefix + day + String.format("%06d", value % 1_000_000);
    }
}
