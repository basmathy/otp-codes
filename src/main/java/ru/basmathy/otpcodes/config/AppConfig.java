package ru.basmathy.otpcodes.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private final int serverPort;
    private final String jwtSecret;
    private final long jwtExpiresSeconds;
    private final long otpExpirationCheckSeconds;
    private final String otpFilePath;

    public AppConfig(int serverPort, String jwtSecret, long jwtExpiresSeconds,
                     long otpExpirationCheckSeconds, String otpFilePath) {
        this.serverPort = serverPort;
        this.jwtSecret = jwtSecret;
        this.jwtExpiresSeconds = jwtExpiresSeconds;
        this.otpExpirationCheckSeconds = otpExpirationCheckSeconds;
        this.otpFilePath = otpFilePath;
    }

    public static AppConfig load() {
        Properties properties = loadProperties("application.properties");
        return new AppConfig(
                intProperty(properties, "server.port"),
                stringProperty(properties, "jwt.secret"),
                longProperty(properties, "jwt.expires.seconds"),
                longProperty(properties, "otp.expiration.check.seconds"),
                stringProperty(properties, "otp.file.path")
        );
    }

    static Properties loadProperties(String resourceName) {
        Properties properties = new Properties();
        try (InputStream inputStream = AppConfig.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new IllegalStateException(resourceName + " not found");
            }
            properties.load(inputStream);
            return properties;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + resourceName, e);
        }
    }

    static String stringProperty(Properties properties, String name) {
        String value = properties.getProperty(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing property: " + name);
        }
        return value;
    }

    static int intProperty(Properties properties, String name) {
        return Integer.parseInt(stringProperty(properties, name));
    }

    static long longProperty(Properties properties, String name) {
        return Long.parseLong(stringProperty(properties, name));
    }

    static boolean booleanProperty(Properties properties, String name) {
        return Boolean.parseBoolean(stringProperty(properties, name));
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getJwtSecret() {
        return jwtSecret;
    }

    public long getJwtExpiresSeconds() {
        return jwtExpiresSeconds;
    }

    public long getOtpExpirationCheckSeconds() {
        return otpExpirationCheckSeconds;
    }

    public String getOtpFilePath() {
        return otpFilePath;
    }
}
