package ru.basmathy.otpcodes.config;

import java.util.Properties;

public class TelegramConfig {
    private final boolean enabled;
    private final String botToken;
    private final String chatId;
    private final String apiUrlTemplate;

    private TelegramConfig(boolean enabled, String botToken, String chatId, String apiUrlTemplate) {
        this.enabled = enabled;
        this.botToken = botToken;
        this.chatId = chatId;
        this.apiUrlTemplate = apiUrlTemplate;
    }

    public static TelegramConfig load() {
        Properties properties = AppConfig.loadProperties("telegram.properties");
        return new TelegramConfig(
                AppConfig.booleanProperty(properties, "telegram.enabled"),
                AppConfig.stringProperty(properties, "telegram.bot.token"),
                AppConfig.stringProperty(properties, "telegram.chat.id"),
                AppConfig.stringProperty(properties, "telegram.api.url")
        );
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getBotToken() {
        return botToken;
    }

    public String getChatId() {
        return chatId;
    }

    public String getApiUrlTemplate() {
        return apiUrlTemplate;
    }
}
