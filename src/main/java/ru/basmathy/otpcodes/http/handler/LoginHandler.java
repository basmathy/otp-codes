package ru.basmathy.otpcodes.http.handler;

import com.sun.net.httpserver.HttpExchange;
import ru.basmathy.otpcodes.http.RequestReader;
import ru.basmathy.otpcodes.http.ResponseWriter;
import ru.basmathy.otpcodes.http.dto.LoginRequest;
import ru.basmathy.otpcodes.http.dto.LoginResponse;
import ru.basmathy.otpcodes.service.AuthService;
import ru.basmathy.otpcodes.service.AuthToken;

import java.io.IOException;

public class LoginHandler extends BaseHandler {
    private final AuthService authService;

    public LoginHandler(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected int handleRequest(HttpExchange exchange) throws IOException {
        requireMethod(exchange, "POST");
        LoginRequest request = RequestReader.readJson(exchange, LoginRequest.class);
        AuthToken authToken = authService.loginUser(request.getLogin(), request.getPassword());
        ResponseWriter.writeJson(
                exchange,
                200,
                new LoginResponse(authToken.getToken(), authToken.getExpiresInSeconds())
        );
        return 200;
    }
}
