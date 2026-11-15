package com.enterprise.common.redis.util;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** 注解 key 的 SpEL 解析（带编译缓存）。 */
public final class SpelKeyResolver {

    private static final SpelExpressionParser PARSER = new SpelExpressionParser();
    private static final ParameterNameDiscoverer DISCOVERER = new DefaultParameterNameDiscoverer();
    private static final Map<String, Expression> CACHE = new ConcurrentHashMap<>();

    private SpelKeyResolver() {}

    public static String resolve(ProceedingJoinPoint joinPoint, String expression) {
        if (!StringUtils.hasText(expression)) return null;
        Method method = org.aspectj.lang.reflect.MethodSignature.class.cast(joinPoint.getSignature()).getMethod();
        String[] names = DISCOVERER.getParameterNames(method);
        Object[] args = joinPoint.getArgs();
        StandardEvaluationContext context = new StandardEvaluationContext();
        if (names != null) {
            for (int i = 0; i < names.length; i++) context.setVariable(names[i], args[i]);
        }
        for (int i = 0; i < args.length; i++) context.setVariable("p" + i, args[i]);
        context.setVariable("args", args);
        context.setVariable("methodName", method.getName());
        Expression exp = CACHE.computeIfAbsent(expression, PARSER::parseExpression);
        Object value = exp.getValue(context);
        return value == null ? null : value.toString();
    }
}
