package ru.basmathy.otpcodes.http.handler;

import com.sun.net.httpserver.HttpExchange;
import ru.basmathy.otpcodes.http.RequestReader;
import ru.basmathy.otpcodes.http.ResponseWriter;
import ru.basmathy.otpcodes.http.dto.ValidateOtpRequest;
import ru.basmathy.otpcodes.model.UserRole;
import ru.basmathy.otpcodes.security.AuthFilter;
import ru.basmathy.otpcodes.security.CurrentUser;
import ru.basmathy.otpcodes.service.OtpService;

import java.io.IOException;

public class ValidateOtpHandler extends BaseHandler {
    private final OtpService otpService;
    private final AuthFilter authFilter;

    public ValidateOtpHandler(OtpService otpService, AuthFilter authFilter) {
        this.otpService = otpService;
        this.authFilter = authFilter;
    }

    @Override
    protected int handleRequest(HttpExchange exchange) throws IOException {
        requireMethod(exchange, "POST");
        CurrentUser currentUser = authFilter.requireRole(exchange, UserRole.USER);
        saveCurrentUser(exchange, currentUser);

        ValidateOtpRequest request = RequestReader.readJson(exchange, ValidateOtpRequest.class);
        otpService.checkOtpCode(currentUser.getId(), request.getOperationId(), request.getCode());
        ResponseWriter.writeJson(exchange, 200, ResponseWriter.message("OTP code validated"));
        return 200;
    }
}
