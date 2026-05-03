package ru.basmathy.otpcodes.http.handler;

import com.sun.net.httpserver.HttpExchange;
import ru.basmathy.otpcodes.http.RequestReader;
import ru.basmathy.otpcodes.http.ResponseWriter;
import ru.basmathy.otpcodes.http.dto.ChangeOtpConfigRequest;
import ru.basmathy.otpcodes.model.OtpConfig;
import ru.basmathy.otpcodes.model.UserRole;
import ru.basmathy.otpcodes.security.AuthFilter;
import ru.basmathy.otpcodes.security.CurrentUser;
import ru.basmathy.otpcodes.service.AdminService;

import java.io.IOException;

public class ChangeOtpConfigHandler extends BaseHandler {
    private final AdminService adminService;
    private final AuthFilter authFilter;

    public ChangeOtpConfigHandler(AdminService adminService, AuthFilter authFilter) {
        this.adminService = adminService;
        this.authFilter = authFilter;
    }

    @Override
    protected int handleRequest(HttpExchange exchange) throws IOException {
        requireMethod(exchange, "PUT");
        CurrentUser currentUser = authFilter.requireRole(exchange, UserRole.ADMIN);
        saveCurrentUser(exchange, currentUser);

        ChangeOtpConfigRequest request = RequestReader.readJson(exchange, ChangeOtpConfigRequest.class);
        OtpConfig config = adminService.changeOtpSettings(request.getCodeLength(), request.getLifetimeSeconds());
        ResponseWriter.writeJson(exchange, 200, config);
        return 200;
    }
}
