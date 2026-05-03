package ru.basmathy.otpcodes.config;

import java.util.Properties;

public class MailConfig {
    private final Properties properties;
    private final boolean enabled;
    private final String username;
    private final String password;
    private final String fromEmail;

    private MailConfig(Properties properties, boolean enabled, String username, String password, String fromEmail) {
        this.properties = properties;
        this.enabled = enabled;
        this.username = username;
        this.password = password;
        this.fromEmail = fromEmail;
    }

    public static MailConfig load() {
        Properties properties = AppConfig.loadProperties("email.properties");
        return new MailConfig(
                properties,
                AppConfig.booleanProperty(properties, "email.enabled"),
                AppConfig.stringProperty(properties, "email.username"),
                AppConfig.stringProperty(properties, "email.password"),
                AppConfig.stringProperty(properties, "email.from")
        );
    }

    public Properties getProperties() {
        return properties;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFromEmail() {
        return fromEmail;
    }
}
