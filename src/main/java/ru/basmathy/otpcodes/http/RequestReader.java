package ru.basmathy.otpcodes.http;

import com.sun.net.httpserver.HttpExchange;
import ru.basmathy.otpcodes.exception.BadRequestException;

import java.io.IOException;

public class RequestReader {
    private RequestReader() {
    }

    public static <T> T readJson(HttpExchange exchange, Class<T> type) {
        try {
            return JsonUtils.mapper().readValue(exchange.getRequestBody(), type);
        } catch (IOException e) {
            throw new BadRequestException("Invalid JSON request body");
        }
    }
}
