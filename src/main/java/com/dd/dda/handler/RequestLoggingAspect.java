package com.dd.dda.handler;


import com.dd.dda.service.tracing.TracingService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@Aspect
@Component
public class RequestLoggingAspect {

    static final String REQUEST_ID = "request.id";

    private final TracingService tracing;

    public RequestLoggingAspect(TracingService tracing) {
        this.tracing = tracing;
    }

    @Around("execution(public * com.dd.dda.controller.*Controller.*(..))")
    public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        MDC.put(REQUEST_ID, UUID.randomUUID().toString());

        this.tracing.onIncomingRequest(request, joinPoint);

        long startTime = System.nanoTime();
        Object response = joinPoint.proceed();
        double duration = (System.nanoTime() - startTime) / 1000000.0;

        this.tracing.onResponse(response, duration);

        return response;
    }

    @AfterReturning(value = "execution(* com.dd.dda.handler.*HandlerAdvice.*(..)) && args(exception, ..)", returning = "response", argNames = "joinPoint,exception,response")
    public void afterHandleException(JoinPoint joinPoint, Exception exception, Object response) {
        this.tracing.onErrorResponse(exception, response);
    }

}
