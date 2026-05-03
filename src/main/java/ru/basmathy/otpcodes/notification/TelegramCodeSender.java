package ru.basmathy.otpcodes.notification;

import ru.basmathy.otpcodes.config.TelegramConfig;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class TelegramCodeSender implements CodeSender {
    private static final Logger logger = Logger.getLogger(TelegramCodeSender.class.getName());

    private final TelegramConfig telegramConfig;
    private final HttpClient httpClient;

    public TelegramCodeSender(TelegramConfig telegramConfig) {
        this.telegramConfig = telegramConfig;
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public void sendCode(String destination, String code) {
        if (!telegramConfig.isEnabled()) {
            logger.info("Telegram sending is disabled. Code for " + destination + ": " + code);
            return;
        }

        String message = destination + ", your confirmation code is: " + code;
        String baseUrl = String.format(telegramConfig.getApiUrlTemplate(), telegramConfig.getBotToken());
        String url = baseUrl + "?chat_id=" + urlEncode(telegramConfig.getChatId())
                + "&text=" + urlEncode(message);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("Telegram API returned status " + response.statusCode());
            }
            logger.info("Telegram OTP code sent");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Telegram request was interrupted", e);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to send Telegram OTP code", e);
        }
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
