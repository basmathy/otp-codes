package ru.basmathy.otpcodes.http.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.basmathy.otpcodes.exception.ApiException;
import ru.basmathy.otpcodes.http.RequestLogger;
import ru.basmathy.otpcodes.http.ResponseWriter;
import ru.basmathy.otpcodes.security.CurrentUser;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BaseHandler implements HttpHandler {
    private static final Logger logger = Logger.getLogger(BaseHandler.class.getName());
    private static final String CURRENT_USER_ATTRIBUTE = "currentUser";

    private final RequestLogger requestLogger = new RequestLogger();

    @Override
    public final void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        long startedAt = System.currentTimeMillis();
        int statusCode = 500;

        requestLogger.logIncoming(method, path);
        try {
            statusCode = handleRequest(exchange);
        } catch (ApiException e) {
            statusCode = e.getStatusCode();
            ResponseWriter.writeJson(exchange, statusCode, ResponseWriter.error(e.getMessage()));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected request error", e);
            ResponseWriter.writeJson(exchange, statusCode, ResponseWriter.error("Internal server error"));
        } finally {
            long durationMs = System.currentTimeMillis() - startedAt;
            requestLogger.logCompleted(method, path, statusCode, durationMs, currentLogin(exchange));
        }
    }

    protected abstract int handleRequest(HttpExchange exchange) throws IOException;

    protected void requireMethod(HttpExchange exchange, String expectedMethod) {
        if (!expectedMethod.equalsIgnoreCase(exchange.getRequestMethod())) {
            throw new ApiException(405, "Method not allowed");
        }
    }

    protected void saveCurrentUser(HttpExchange exchange, CurrentUser currentUser) {
        exchange.setAttribute(CURRENT_USER_ATTRIBUTE, currentUser);
    }

    private String currentLogin(HttpExchange exchange) {
        Object value = exchange.getAttribute(CURRENT_USER_ATTRIBUTE);
        if (value instanceof CurrentUser currentUser) {
            return currentUser.getLogin();
        }
        return null;
    }
}
