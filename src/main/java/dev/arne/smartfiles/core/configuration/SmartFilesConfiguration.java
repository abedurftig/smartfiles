package dev.arne.smartfiles.core.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.FileSystems;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "smartfiles")
public class SmartFilesConfiguration {

    private String rootDirectory = System.getProperty("user.home") + FileSystems.getDefault().getSeparator() +".smartfiles";
    private String tenantId = "root";

    public String getCurrentDirectory() {
        return rootDirectory + FileSystems.getDefault().getSeparator() + tenantId;
    }
}
