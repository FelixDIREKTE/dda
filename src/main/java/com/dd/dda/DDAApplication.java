package com.dd.dda;

import com.dd.dda.config.FileStorageConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@Slf4j
@SpringBootApplication
@EnableJpaRepositories
@EnableJpaAuditing
@EnableTransactionManagement
@EnableIntegration
@EnableScheduling
public class DDAApplication {
    public static final String NOT_TEST = "!test";
    public static final String KUBE = "kube";
    public static final String NOT_KUBE = "!kube";

    private final FileStorageConfiguration fileStorageConfiguration;



    public DDAApplication(FileStorageConfiguration fileStorageConfiguration) {
        this.fileStorageConfiguration = fileStorageConfiguration;

    }

    public static void main(String[] args) {
        SpringApplication.run(DDAApplication.class, args);
    }

    @PostConstruct
    void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));


    }

    @EventListener(ApplicationReadyEvent.class)
    public void afterStartup() {
    }

}
