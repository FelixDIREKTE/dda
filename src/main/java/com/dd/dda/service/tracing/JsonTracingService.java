package com.dd.dda.service.tracing;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.slf4j.Marker;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static net.logstash.logback.marker.Markers.append;

@Slf4j
@Service
@Profile("kube")
public class JsonTracingService extends AbstractTracingService {

    @Override
    public void onIncomingRequest(HttpServletRequest request, JoinPoint joinPoint) {
        Map<String, Object> map = new HashMap<>();
        map.put("url", this.getUrl(request));
        map.put("method", request.getMethod());
        map.put("headers", this.getHeaders(request));
        map.put("tag", "incoming request");

        Marker requestMarker = append("request", map);
        log.info(requestMarker, "INCOMING REQUEST");
    }

    @Override
    public void onOutgoingRequest(HttpRequest request, byte[] body) throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("url", request.getURI());
        map.put("method", request.getMethod());
        map.put("headers", this.getHeaders(request));
        map.put("tag", "outgoing request");
        if (request.getMethod() != HttpMethod.GET && body != null) {
            map.put("body", new String(body, "UTF-8"));
        }

        Marker requestMarker = append("request", map);
        log.info(requestMarker, "OUTGOING REQUEST");
    }

    @Override
    public void onResponse(Object response, Double duration) {
        Map<String, Object> map = new HashMap<>();
        if (response instanceof ResponseEntity) {
            ResponseEntity entity = (ResponseEntity) response;
            map.put("status", entity.getStatusCode());
            map.put("body", entity.getBody());
        }
        map.put("duration", duration);
        map.put("tag", "response");

        Marker requestMarker = append("response", map);
        log.info(requestMarker, "RESPONSE");
    }

    @Override
    public void onErrorResponse(Exception exception, Object response) {
        Map<String, Object> map = new HashMap<>();
        map.put("exception", exception.getClass());
        map.put("message", exception.getLocalizedMessage());
        map.put("returning", response);

        Marker requestMarker = append("response", map);
        log.info(requestMarker, "ERROR RESPONSE");
        log.error("Error in cloud", exception);
    }

    private String getUrl(HttpServletRequest request) {
        StringBuffer requestURL = request.getRequestURL();
        String queryString = request.getQueryString();

        if (queryString == null) {
            return requestURL.toString();
        } else {
            return requestURL.append('?').append(queryString).toString();
        }
    }
}
