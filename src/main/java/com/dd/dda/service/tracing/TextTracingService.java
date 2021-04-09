package com.dd.dda.service.tracing;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@Service
@Profile("!kube")
public class TextTracingService extends AbstractTracingService {

    private static final String END_SEPARATOR = "--------------------------------------------------------------------------------------";
    
    @Override
    public synchronized void onIncomingRequest(HttpServletRequest request, JoinPoint joinPoint) {
        log.info("");
        log.info("--------------------------- INCOMING REQUEST -----------------------------------------");
        log.info("URL           : {}", this.getUrl(request));
        log.info("Method        : {}", request.getMethod());

        log.info("Headers       : {}", getHeaders(request));
        this.logJoinPoint(joinPoint);
        log.info(END_SEPARATOR);
        log.info("");
    }

    @Override
    public synchronized void onOutgoingRequest(HttpRequest request, byte[] body) throws IOException {
        log.info("");
        log.info("--------------------------- OUTGOING REQUEST -----------------------------------------");
        log.info("URL           : {}", request.getURI());
        log.info("Method        : {}", request.getMethod());

        String bodyAsString = "No Body Data";
        if (body != null && body.length > 0) {
            bodyAsString = StringUtils.abbreviate(new String(body, UTF_8), 100);
        }

        log.info("Headers       : {}", getHeaders(request));

        if (request.getMethod() != HttpMethod.GET) {
            log.info("Request body  : {}", bodyAsString);
        }

        log.info(END_SEPARATOR);
        log.info("");
    }

    @Override
    public synchronized void onResponse(Object response, Double duration) {
        log.info("");
        log.info("--------------------------- RESPONSE -------------------------------------------------");
        if (response instanceof ResponseEntity) {
            ResponseEntity entity = (ResponseEntity) response;
            log.info("Status code   : {}", entity.getStatusCode());
            if (entity.getBody() != null) {
                log.info("Body          : {}", StringUtils.abbreviate(entity.getBody().toString(), 100));
            }
        }

        log.info("Duration      : {} ms", duration);
        log.info(END_SEPARATOR);
        log.info("");
    }

    @Override
    public synchronized void onErrorResponse(Exception exception, Object response) {
        log.error("--------------------------- ERROR RESPONSE -------------------------------------------");
        log.error("Exception     : {}", exception.getClass());
        log.error("Message       : {}", exception.getLocalizedMessage());
        log.error("Returning     : {}", response);
        log.error(END_SEPARATOR);
        log.error("");
    }

    private synchronized void logJoinPoint(JoinPoint joinPoint) {
        if (joinPoint != null) {
            String argsInfo = "No Args";
            if (joinPoint.getArgs().length > 0) {
                argsInfo = Arrays.toString(joinPoint.getArgs());
            }
            log.info("");
            log.info("Signature     : {}", joinPoint.getSignature());
            log.info("Args          : {}", argsInfo);
            log.info("");
        }
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
