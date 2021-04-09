package com.dd.dda.service.tracing;

import org.aspectj.lang.JoinPoint;
import org.springframework.http.HttpRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public interface TracingService {

    void onIncomingRequest(HttpServletRequest request, JoinPoint joinPoint);

    void onOutgoingRequest(HttpRequest request, byte[] body) throws IOException;

    void onResponse(Object response, Double duration);

    void onErrorResponse(Exception exception, Object response);
}
