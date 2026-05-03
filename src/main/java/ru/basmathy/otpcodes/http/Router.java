package ru.basmathy.otpcodes.http;

import com.sun.net.httpserver.HttpServer;
import ru.basmathy.otpcodes.http.handler.ChangeOtpConfigHandler;
import ru.basmathy.otpcodes.http.handler.DeleteUserHandler;
import ru.basmathy.otpcodes.http.handler.GenerateOtpHandler;
import ru.basmathy.otpcodes.http.handler.ListUsersHandler;
import ru.basmathy.otpcodes.http.handler.LoginHandler;
import ru.basmathy.otpcodes.http.handler.RegisterHandler;
import ru.basmathy.otpcodes.http.handler.ValidateOtpHandler;

public class Router {
    private final HttpServer server;
    private final RegisterHandler registerHandler;
    private final LoginHandler loginHandler;
    private final ChangeOtpConfigHandler changeOtpConfigHandler;
    private final ListUsersHandler listUsersHandler;
    private final DeleteUserHandler deleteUserHandler;
    private final GenerateOtpHandler generateOtpHandler;
    private final ValidateOtpHandler validateOtpHandler;

    public Router(HttpServer server, RegisterHandler registerHandler, LoginHandler loginHandler,
                  ChangeOtpConfigHandler changeOtpConfigHandler, ListUsersHandler listUsersHandler,
                  DeleteUserHandler deleteUserHandler, GenerateOtpHandler generateOtpHandler,
                  ValidateOtpHandler validateOtpHandler) {
        this.server = server;
        this.registerHandler = registerHandler;
        this.loginHandler = loginHandler;
        this.changeOtpConfigHandler = changeOtpConfigHandler;
        this.listUsersHandler = listUsersHandler;
        this.deleteUserHandler = deleteUserHandler;
        this.generateOtpHandler = generateOtpHandler;
        this.validateOtpHandler = validateOtpHandler;
    }

    public void registerRoutes() {
        server.createContext("/api/auth/register", registerHandler);
        server.createContext("/api/auth/login", loginHandler);
        server.createContext("/api/admin/otp-config", changeOtpConfigHandler);
        server.createContext("/api/admin/users", listUsersHandler);
        server.createContext("/api/admin/users/", deleteUserHandler);
        server.createContext("/api/otp/generate", generateOtpHandler);
        server.createContext("/api/otp/validate", validateOtpHandler);
    }
}
