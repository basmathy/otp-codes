package ru.basmathy.otpcodes.http.handler;

import com.sun.net.httpserver.HttpExchange;
import ru.basmathy.otpcodes.http.RequestReader;
import ru.basmathy.otpcodes.http.ResponseWriter;
import ru.basmathy.otpcodes.http.dto.GenerateOtpRequest;
import ru.basmathy.otpcodes.model.UserRole;
import ru.basmathy.otpcodes.security.AuthFilter;
import ru.basmathy.otpcodes.security.CurrentUser;
import ru.basmathy.otpcodes.service.OtpService;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class GenerateOtpHandler extends BaseHandler {
    private final OtpService otpService;
    private final AuthFilter authFilter;

    public GenerateOtpHandler(OtpService otpService, AuthFilter authFilter) {
        this.otpService = otpService;
        this.authFilter = authFilter;
    }

    @Override
    protected int handleRequest(HttpExchange exchange) throws IOException {
        requireMethod(exchange, "POST");
        CurrentUser currentUser = authFilter.requireRole(exchange, UserRole.USER);
        saveCurrentUser(exchange, currentUser);

        GenerateOtpRequest request = RequestReader.readJson(exchange, GenerateOtpRequest.class);
        long otpId = otpService.createOtpCode(
                currentUser.getId(),
                request.getOperationId(),
                request.getChannel(),
                request.getDestination()
        );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", otpId);
        response.put("message", "OTP code generated");
        ResponseWriter.writeJson(exchange, 201, response);
        return 201;
    }
}
