package com.university.registration_service.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PerformanceMonitoringAspect {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitoringAspect.class);

    private static final long THRESHOLD_MS = 1000;

    @Pointcut("within(com.university.registration_service.service..*)")
    public void serviceLayerPointcut() {
    }

    @Around("serviceLayerPointcut()")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        Object result = joinPoint.proceed();

        long executionTime = System.currentTimeMillis() - start;

        if (executionTime > THRESHOLD_MS) {
            logger.warn("SLOW METHOD: {}.{}() took {} ms (threshold: {} ms)",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    executionTime,
                    THRESHOLD_MS);
        }

        return result;
    }
}
