package com.university.user_service.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("execution(* com.university.user_service.service..*.*(..))")
    public void serviceLayer() {
    }

    @Pointcut("execution(* com.university.user_service.repository..*.*(..))")
    public void repositoryLayer() {
    }

    @Around("serviceLayer() || repositoryLayer()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        String className = joinPoint.getSignature().getDeclaringTypeName();
        Object[] args = joinPoint.getArgs();

        logger.info("Entering: {}.{}() with arguments: {}", className, methodName, Arrays.toString(args));

        long start = System.currentTimeMillis();
        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            logger.error("Exception in {}.{}() with cause: {}", className, methodName, throwable.getMessage());
            throw throwable;
        }
        long elapsed = System.currentTimeMillis() - start;

        logger.info("Exiting: {}.{}() with result: {}, execution time: {} ms",
                     className, methodName, result, elapsed);
        return result;
    }
}
