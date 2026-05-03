package ru.basmathy.otpcodes.security;

import com.sun.net.httpserver.HttpExchange;
import ru.basmathy.otpcodes.exception.ForbiddenException;
import ru.basmathy.otpcodes.exception.UnauthorizedException;
import ru.basmathy.otpcodes.model.UserRole;
import ru.basmathy.otpcodes.service.TokenService;

import java.util.List;

public class AuthFilter {
    private final TokenService tokenService;

    public AuthFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    public CurrentUser requireUser(HttpExchange exchange) {
        String authorization = firstHeader(exchange, "Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing token");
        }
        return tokenService.parseToken(authorization.substring("Bearer ".length()));
    }

    public CurrentUser requireRole(HttpExchange exchange, UserRole requiredRole) {
        CurrentUser currentUser = requireUser(exchange);
        if (currentUser.getRole() != requiredRole) {
            throw new ForbiddenException("Access denied");
        }
        return currentUser;
    }

    private String firstHeader(HttpExchange exchange, String name) {
        List<String> values = exchange.getRequestHeaders().get(name);
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.get(0);
    }
}
