package org.gl.eir.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "sftp.config")
public class SftpConfigurations {
    private String sftpServer;
    private int sftpPort;
    private String sftpUsername;
    private String sftpPassword;
    private String remoteFolderPath;
    private String localFolderPath;
}
