package ru.basmathy.otpcodes;

import com.sun.net.httpserver.HttpServer;
import ru.basmathy.otpcodes.config.AppConfig;
import ru.basmathy.otpcodes.config.DatabaseConfig;
import ru.basmathy.otpcodes.config.MailConfig;
import ru.basmathy.otpcodes.config.SmsConfig;
import ru.basmathy.otpcodes.config.TelegramConfig;
import ru.basmathy.otpcodes.dao.OtpCodeDao;
import ru.basmathy.otpcodes.dao.OtpConfigDao;
import ru.basmathy.otpcodes.dao.UserDao;
import ru.basmathy.otpcodes.db.ConnectionFactory;
import ru.basmathy.otpcodes.db.MigrationRunner;
import ru.basmathy.otpcodes.http.Router;
import ru.basmathy.otpcodes.http.handler.ChangeOtpConfigHandler;
import ru.basmathy.otpcodes.http.handler.DeleteUserHandler;
import ru.basmathy.otpcodes.http.handler.GenerateOtpHandler;
import ru.basmathy.otpcodes.http.handler.ListUsersHandler;
import ru.basmathy.otpcodes.http.handler.LoginHandler;
import ru.basmathy.otpcodes.http.handler.RegisterHandler;
import ru.basmathy.otpcodes.http.handler.ValidateOtpHandler;
import ru.basmathy.otpcodes.notification.EmailCodeSender;
import ru.basmathy.otpcodes.notification.FileCodeSender;
import ru.basmathy.otpcodes.notification.SmsCodeSender;
import ru.basmathy.otpcodes.notification.TelegramCodeSender;
import ru.basmathy.otpcodes.scheduler.OtpExpirationScheduler;
import ru.basmathy.otpcodes.security.AuthFilter;
import ru.basmathy.otpcodes.service.AdminService;
import ru.basmathy.otpcodes.service.AuthService;
import ru.basmathy.otpcodes.service.ExpiredOtpService;
import ru.basmathy.otpcodes.service.OtpService;
import ru.basmathy.otpcodes.service.PasswordService;
import ru.basmathy.otpcodes.service.TokenService;
import ru.basmathy.otpcodes.service.UserService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws IOException {
        AppConfig appConfig = AppConfig.load();
        DatabaseConfig databaseConfig = DatabaseConfig.load();
        ConnectionFactory connectionFactory = new ConnectionFactory(databaseConfig);
        MigrationRunner migrationRunner = new MigrationRunner(connectionFactory);

        migrationRunner.runMigrations();

        UserDao userDao = new UserDao(connectionFactory);
        OtpConfigDao otpConfigDao = new OtpConfigDao(connectionFactory);
        OtpCodeDao otpCodeDao = new OtpCodeDao(connectionFactory);

        PasswordService passwordService = new PasswordService();
        TokenService tokenService = new TokenService(appConfig.getJwtSecret(), appConfig.getJwtExpiresSeconds());
        UserService userService = new UserService(userDao, passwordService);
        AuthService authService = new AuthService(userDao, passwordService, tokenService);
        AdminService adminService = new AdminService(userDao, otpConfigDao);
        OtpService otpService = new OtpService(
                otpCodeDao,
                otpConfigDao,
                new EmailCodeSender(MailConfig.load()),
                new SmsCodeSender(SmsConfig.load()),
                new TelegramCodeSender(TelegramConfig.load()),
                new FileCodeSender(appConfig.getOtpFilePath())
        );
        ExpiredOtpService expiredOtpService = new ExpiredOtpService(otpCodeDao);
        AuthFilter authFilter = new AuthFilter(tokenService);

        HttpServer server = HttpServer.create(new InetSocketAddress(appConfig.getServerPort()), 0);
        server.setExecutor(Executors.newFixedThreadPool(8));

        Router router = new Router(
                server,
                new RegisterHandler(userService),
                new LoginHandler(authService),
                new ChangeOtpConfigHandler(adminService, authFilter),
                new ListUsersHandler(adminService, authFilter),
                new DeleteUserHandler(adminService, authFilter),
                new GenerateOtpHandler(otpService, authFilter),
                new ValidateOtpHandler(otpService, authFilter)
        );
        router.registerRoutes();

        OtpExpirationScheduler scheduler = new OtpExpirationScheduler(
                expiredOtpService,
                appConfig.getOtpExpirationCheckSeconds()
        );
        scheduler.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            scheduler.stop();
            server.stop(0);
        }));

        server.start();
        logger.info("OTP codes server started on port " + appConfig.getServerPort());
    }
}
