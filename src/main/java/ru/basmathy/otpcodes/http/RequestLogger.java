package ru.basmathy.otpcodes.http;

import java.util.logging.Logger;

public class RequestLogger {
    private static final Logger logger = Logger.getLogger(RequestLogger.class.getName());

    public void logIncoming(String method, String path) {
        logger.info("Incoming request: " + method + " " + path);
    }

    public void logCompleted(String method, String path, int statusCode, long durationMs, String login) {
        String userPart = login == null ? "" : " user=" + login;
        logger.info("Request completed: " + method + " " + path
                + " status=" + statusCode + " durationMs=" + durationMs + userPart);
    }
}
