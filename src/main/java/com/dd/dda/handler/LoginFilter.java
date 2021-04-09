package com.dd.dda.handler;

import com.dd.dda.model.sqldata.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.dd.dda.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@Component
public class LoginFilter extends BasicAuthenticationEntryPoint {

    private String email;
    private UserService userService;
    private ObjectMapper objectMapper;
    private static final String MESSAGE = "message";

    @Autowired
    public LoginFilter(@Lazy UserService userService, @Lazy ObjectMapper objectMapper) {
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.addHeader("WWW-Authenticate", "Basic =" + getRealmName());

        log.error("Error by loginFiler -> exception = ", authException);

        Map<String, Object> body = new HashMap<>();

        if (authException instanceof DisabledException) {
            body.put(MESSAGE, "Dieser Account ist wegen mehrfacher Registrierung deaktiviert. Bitte prüfe dein Postfach. Wir bitten die Unannehmlichkeit zu entschuldigen.");
        } else if (authException instanceof InsufficientAuthenticationException) {
            body.put(MESSAGE, "Session is certainly not valid anymore");
        } else {
            body.put(MESSAGE, "Falscher Nutzername oder Kennwort!");
        }

        body.put("error", new String(authException.getMessage().getBytes(), UTF_8));
        body.put("email", getEmailByRequestUsername(request));
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);

        response.getWriter().write(objectMapper.writeValueAsString(body));
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    }

    @Override
    public void afterPropertiesSet() {
        setRealmName("0");
        super.afterPropertiesSet();
    }

    private String getEmailByRequestUsername(HttpServletRequest request) {
        final String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
            String base64Credentials = authorization.substring("Basic".length()).trim();
            byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(credDecoded, UTF_8);
            final String[] values = credentials.split(":", 2);
            this.email = values[0];
            getUserByEmail(values[0]);
        }
        return this.email;
    }

    private void getUserByEmail(String email) {
        User user = userService.getUserByMail(email);

        if (user == null) {
            log.error("No valid user found!");
        }
    }

}