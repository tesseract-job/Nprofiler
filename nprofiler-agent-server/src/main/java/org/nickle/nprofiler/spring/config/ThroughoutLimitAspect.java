package org.nickle.nprofiler.spring.config;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class ThroughoutLimitAspect {
    @Pointcut("execution(* org.nickle.nprofiler.spring.web..*.*(..))")
    public void pointCut() {
    }

    @Before("pointCut()")
    public void beforeInvoke(JoinPoint joinPoint) {
       
    }
}
