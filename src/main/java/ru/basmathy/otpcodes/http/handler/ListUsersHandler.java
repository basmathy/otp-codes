package ru.basmathy.otpcodes.http.handler;

import com.sun.net.httpserver.HttpExchange;
import ru.basmathy.otpcodes.http.ResponseWriter;
import ru.basmathy.otpcodes.http.dto.UserResponse;
import ru.basmathy.otpcodes.model.User;
import ru.basmathy.otpcodes.model.UserRole;
import ru.basmathy.otpcodes.security.AuthFilter;
import ru.basmathy.otpcodes.security.CurrentUser;
import ru.basmathy.otpcodes.service.AdminService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListUsersHandler extends BaseHandler {
    private final AdminService adminService;
    private final AuthFilter authFilter;

    public ListUsersHandler(AdminService adminService, AuthFilter authFilter) {
        this.adminService = adminService;
        this.authFilter = authFilter;
    }

    @Override
    protected int handleRequest(HttpExchange exchange) throws IOException {
        requireMethod(exchange, "GET");
        CurrentUser currentUser = authFilter.requireRole(exchange, UserRole.ADMIN);
        saveCurrentUser(exchange, currentUser);

        List<User> users = adminService.findRegularUsers();
        List<UserResponse> response = new ArrayList<>();
        for (User user : users) {
            response.add(new UserResponse(user));
        }
        ResponseWriter.writeJson(exchange, 200, response);
        return 200;
    }
}
