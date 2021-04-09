package com.dd.dda.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
@ConfigurationProperties(prefix = "file.storage")
@Data
public class FileStorageConfiguration {

    private String rootLocation;
    private String windowsDisk;

    private String verificationProofLocation;
    private String profilcePicLocation;
    private String parliamentPicLocation;
    private String billAbstractLocation;
    private String billFilesLocation;

    public String getRootLocation() {
        return rootLocation;
    }

    public Path getRootLocationPath() {
        return getPathOf(rootLocation);
    }

    public String getWindowsDisk() {
        return windowsDisk;
    }


    private Path getPathOf(String location) {
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            return Path.of(getWindowsDisk() + "/" + location);
        }
        return Path.of(location);
    }
}
