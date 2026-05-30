package com.university.event_service.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("execution(* com.university.event_service.service..*.*(..))")
    public void serviceLayer() {
    }

    @Pointcut("execution(* com.university.event_service.repository..*.*(..))")
    public void repositoryLayer() {
    }

    @Around("serviceLayer() || repositoryLayer()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();
        log.info("Entering: {} with arguments: {}", methodName, joinPoint.getArgs());
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            log.info("Exiting: {} completed in {} ms", methodName, duration);
            return result;
        } catch (Throwable t) {
            long duration = System.currentTimeMillis() - start;
            log.error("Exception in: {} after {} ms: {}", methodName, duration, t.getMessage());
            throw t;
        }
    }

    @AfterThrowing(pointcut = "serviceLayer() || repositoryLayer()", throwing = "ex")
    public void logAfterThrowing(Throwable ex) {
        log.error("Exception thrown: {}", ex.getMessage(), ex);
    }
}
