package com.dd.dda.service.tracing;

import org.aspectj.lang.JoinPoint;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

public class AbstractTracingService implements TracingService {

    @Override
    public void onIncomingRequest(HttpServletRequest request, JoinPoint joinPoint) {
        throw new RuntimeException("To be implemented");
    }

    @Override
    public void onOutgoingRequest(HttpRequest request, byte[] body) throws IOException {
        throw new RuntimeException("To be implemented");
    }

    @Override
    public void onResponse(Object response, Double duration) {
        throw new RuntimeException("To be implemented");
    }

    @Override
    public void onErrorResponse(Exception exception, Object response) {
        throw new RuntimeException("To be implemented");
    }

    protected String getHeaders(HttpServletRequest request) {
        StringBuilder builder = new StringBuilder();
        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = request.getHeader(key);
            builder.append(String.format("[%s=%s] ", key, value));
        }
        return builder.toString();
    }

    protected StringBuilder getHeaders(HttpRequest request) {
        StringBuilder builder = new StringBuilder();
        HttpHeaders headers = request.getHeaders();
        Set<String> strings = headers.keySet();
        for (String key : strings) {
            List<String> value = headers.get(key);
            builder.append(String.format("[%s=%s] ", key, value));
        }
        return builder;
    }
}
