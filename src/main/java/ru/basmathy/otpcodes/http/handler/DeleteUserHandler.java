package ru.basmathy.otpcodes.http.handler;

import com.sun.net.httpserver.HttpExchange;
import ru.basmathy.otpcodes.exception.BadRequestException;
import ru.basmathy.otpcodes.http.ResponseWriter;
import ru.basmathy.otpcodes.model.UserRole;
import ru.basmathy.otpcodes.security.AuthFilter;
import ru.basmathy.otpcodes.security.CurrentUser;
import ru.basmathy.otpcodes.service.AdminService;

import java.io.IOException;

public class DeleteUserHandler extends BaseHandler {
    private static final String PREFIX = "/api/admin/users/";

    private final AdminService adminService;
    private final AuthFilter authFilter;

    public DeleteUserHandler(AdminService adminService, AuthFilter authFilter) {
        this.adminService = adminService;
        this.authFilter = authFilter;
    }

    @Override
    protected int handleRequest(HttpExchange exchange) throws IOException {
        requireMethod(exchange, "DELETE");
        CurrentUser currentUser = authFilter.requireRole(exchange, UserRole.ADMIN);
        saveCurrentUser(exchange, currentUser);

        long userId = parseUserId(exchange.getRequestURI().getPath());
        adminService.removeUserById(userId);
        ResponseWriter.writeJson(exchange, 200, ResponseWriter.message("User removed"));
        return 200;
    }

    private long parseUserId(String path) {
        String idText = path.substring(PREFIX.length());
        if (idText.isBlank() || idText.contains("/")) {
            throw new BadRequestException("Invalid user id");
        }
        try {
            return Long.parseLong(idText);
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid user id");
        }
    }
}
