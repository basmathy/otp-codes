package ru.basmathy.otpcodes.service;

import ru.basmathy.otpcodes.dao.UserDao;
import ru.basmathy.otpcodes.exception.BadRequestException;
import ru.basmathy.otpcodes.model.User;
import ru.basmathy.otpcodes.model.UserRole;

import java.util.Locale;
import java.util.logging.Logger;

public class UserService {
    private static final Logger logger = Logger.getLogger(UserService.class.getName());

    private final UserDao userDao;
    private final PasswordService passwordService;

    public UserService(UserDao userDao, PasswordService passwordService) {
        this.userDao = userDao;
        this.passwordService = passwordService;
    }

    public long registerUser(String login, String password, String roleText) {
        validateLogin(login);
        UserRole role = parseRole(roleText);

        if (userDao.findByLogin(login).isPresent()) {
            throw new BadRequestException("Login is already used");
        }
        if (role == UserRole.ADMIN && userDao.adminExists()) {
            throw new BadRequestException("Administrator already exists");
        }

        User user = new User();
        user.setLogin(login);
        user.setPasswordHash(passwordService.hashPassword(password));
        user.setRole(role);
        long userId = userDao.save(user);
        logger.info("User registered: " + login);
        return userId;
    }

    private void validateLogin(String login) {
        if (login == null || login.isBlank() || login.length() > 100) {
            throw new BadRequestException("Login must not be empty and must be shorter than 100 characters");
        }
    }

    private UserRole parseRole(String roleText) {
        if (roleText == null || roleText.isBlank()) {
            return UserRole.USER;
        }
        try {
            return UserRole.valueOf(roleText.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unknown user role");
        }
    }
}
