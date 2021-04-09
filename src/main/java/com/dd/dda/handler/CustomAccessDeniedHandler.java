package com.dd.dda.handler;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(final HttpServletRequest request, final HttpServletResponse response, final AccessDeniedException ex) throws IOException {
        response.getOutputStream().print("You are not authorized to see / process this request...!!!");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

}
