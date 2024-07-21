package com.github.omerfarukicen.cassandraui.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "client.storage")
public class StorageProperties {
    private String location = System.getProperty("java.io.tmpdir");
    private String propertiesFile = "connections.cfg";
}
