package ru.basmathy.otpcodes.config;

import java.util.Properties;

public class SmsConfig {
    private final boolean enabled;
    private final String host;
    private final int port;
    private final String systemId;
    private final String password;
    private final String systemType;
    private final String sourceAddress;

    private SmsConfig(boolean enabled, String host, int port, String systemId,
                      String password, String systemType, String sourceAddress) {
        this.enabled = enabled;
        this.host = host;
        this.port = port;
        this.systemId = systemId;
        this.password = password;
        this.systemType = systemType;
        this.sourceAddress = sourceAddress;
    }

    public static SmsConfig load() {
        Properties properties = AppConfig.loadProperties("sms.properties");
        return new SmsConfig(
                AppConfig.booleanProperty(properties, "sms.enabled"),
                AppConfig.stringProperty(properties, "smpp.host"),
                AppConfig.intProperty(properties, "smpp.port"),
                AppConfig.stringProperty(properties, "smpp.system_id"),
                AppConfig.stringProperty(properties, "smpp.password"),
                AppConfig.stringProperty(properties, "smpp.system_type"),
                AppConfig.stringProperty(properties, "smpp.source_addr")
        );
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getSystemId() {
        return systemId;
    }

    public String getPassword() {
        return password;
    }

    public String getSystemType() {
        return systemType;
    }

    public String getSourceAddress() {
        return sourceAddress;
    }
}
