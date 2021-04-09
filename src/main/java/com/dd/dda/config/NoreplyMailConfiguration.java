package com.dd.dda.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "noreplymail")
@Data
public class NoreplyMailConfiguration {

    private String smtpServer;
    private String username;
    private String password;
    private String emailFrom;
    private String duplicateInfoDestinations;

}
