package com.dd.dda.service;

import com.dd.dda.model.LoginUser;
import com.dd.dda.model.sqldata.User;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class DDAUserDetailsService implements UserDetailsService {

    private final UserService userService;
    private final HttpServletRequest request;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public DDAUserDetailsService(@Lazy UserService userService, HttpServletRequest request, @Lazy BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userService = userService;
        this.request = request;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = userService.getUserByMail(email);
        if (checkMainPassword(user)) {
            return new LoginUser(user.getId(), user.getEmail(), user.getPasswordHash(), user.isActive(), true, true, true, this.buildAuthorities()); //buildAuthorities(user)
        }
        throw new BadCredentialsException("User or Password are wrong...!!!");
    }

    private List<GrantedAuthority> buildAuthorities() {    //buildAuthorities(User user) <-- when you will later make db roles
        List<GrantedAuthority> auths = new ArrayList<>();
        auths.add(new SimpleGrantedAuthority("User")); //When User get an Role attribute, change it here
        return new ArrayList<>(auths);
    }

    private boolean checkMainPassword(User user) {
        if (user == null) {
            return false;
        }
        final String authorization = request.getHeader("Authorization");

        if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
            // Authorization: Basic base64credentials
            String base64Credentials = authorization.substring("Basic".length()).trim();

            String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
            final String[] values = credentials.split(":", 2);
            String password = values[1].trim();
            return bCryptPasswordEncoder.matches(password, user.getPasswordHash());
        }
        return false;
    }
}

