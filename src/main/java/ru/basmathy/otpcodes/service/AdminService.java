package ru.basmathy.otpcodes.service;

import ru.basmathy.otpcodes.dao.OtpConfigDao;
import ru.basmathy.otpcodes.dao.UserDao;
import ru.basmathy.otpcodes.exception.BadRequestException;
import ru.basmathy.otpcodes.exception.NotFoundException;
import ru.basmathy.otpcodes.model.OtpConfig;
import ru.basmathy.otpcodes.model.User;
import ru.basmathy.otpcodes.model.UserRole;

import java.util.List;
import java.util.logging.Logger;

public class AdminService {
    private static final Logger logger = Logger.getLogger(AdminService.class.getName());

    private final UserDao userDao;
    private final OtpConfigDao otpConfigDao;

    public AdminService(UserDao userDao, OtpConfigDao otpConfigDao) {
        this.userDao = userDao;
        this.otpConfigDao = otpConfigDao;
    }

    public OtpConfig changeOtpSettings(int codeLength, int lifetimeSeconds) {
        if (codeLength < 4 || codeLength > 12) {
            throw new BadRequestException("Code length must be between 4 and 12");
        }
        if (lifetimeSeconds < 30 || lifetimeSeconds > 86400) {
            throw new BadRequestException("Lifetime must be between 30 and 86400 seconds");
        }
        otpConfigDao.updateConfig(codeLength, lifetimeSeconds);
        logger.info("OTP settings changed");
        return otpConfigDao.getConfig();
    }

    public List<User> findRegularUsers() {
        return userDao.findAllUsersWithoutAdmins();
    }

    public void removeUserById(long userId) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (user.getRole() == UserRole.ADMIN) {
            throw new BadRequestException("Administrator cannot be removed through this endpoint");
        }
        userDao.deleteById(userId);
        logger.info("User removed: id=" + userId);
    }
}
