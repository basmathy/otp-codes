package ru.basmathy.otpcodes.service;

import ru.basmathy.otpcodes.dao.UserDao;
import ru.basmathy.otpcodes.exception.UnauthorizedException;
import ru.basmathy.otpcodes.model.User;

public class AuthService {
    private final UserDao userDao;
    private final PasswordService passwordService;
    private final TokenService tokenService;

    public AuthService(UserDao userDao, PasswordService passwordService, TokenService tokenService) {
        this.userDao = userDao;
        this.passwordService = passwordService;
        this.tokenService = tokenService;
    }

    public AuthToken loginUser(String login, String password) {
        User user = userDao.findByLogin(login)
                .orElseThrow(() -> new UnauthorizedException("Invalid login or password"));

        if (!passwordService.checkPassword(password, user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid login or password");
        }

        return tokenService.createToken(user);
    }
}
