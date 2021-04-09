package com.dd.dda.model;

import org.springframework.util.StringUtils;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

public class Login {

    @NotEmpty(message = "Diese Zugangsdaten sind leider nicht korrekt (E-Mail Leer)")
    @Pattern(regexp = ".+@.+\\..+", message = "Diese Zugangsdaten sind leider nicht korrekt (E-Mail Format)")
    private String email;

    @NotEmpty(message = "Diese Zugangsdaten sind leider nicht korrekt (Password Leer)")
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        if (!StringUtils.isEmpty(email)) {
            this.email = email.trim().toLowerCase();
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "Login{" +
                "email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
