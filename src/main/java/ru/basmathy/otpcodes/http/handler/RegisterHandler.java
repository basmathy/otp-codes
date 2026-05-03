package ru.basmathy.otpcodes.http.handler;

import com.sun.net.httpserver.HttpExchange;
import ru.basmathy.otpcodes.http.RequestReader;
import ru.basmathy.otpcodes.http.ResponseWriter;
import ru.basmathy.otpcodes.http.dto.RegisterRequest;
import ru.basmathy.otpcodes.service.UserService;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class RegisterHandler extends BaseHandler {
    private final UserService userService;

    public RegisterHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected int handleRequest(HttpExchange exchange) throws IOException {
        requireMethod(exchange, "POST");
        RegisterRequest request = RequestReader.readJson(exchange, RegisterRequest.class);
        long userId = userService.registerUser(request.getLogin(), request.getPassword(), request.getRole());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", userId);
        response.put("message", "User registered");
        ResponseWriter.writeJson(exchange, 201, response);
        return 201;
    }
}
