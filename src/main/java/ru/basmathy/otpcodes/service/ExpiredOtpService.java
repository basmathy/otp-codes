package ru.basmathy.otpcodes.service;

import ru.basmathy.otpcodes.dao.OtpCodeDao;

import java.time.Instant;
import java.util.logging.Logger;

public class ExpiredOtpService {
    private static final Logger logger = Logger.getLogger(ExpiredOtpService.class.getName());

    private final OtpCodeDao otpCodeDao;

    public ExpiredOtpService(OtpCodeDao otpCodeDao) {
        this.otpCodeDao = otpCodeDao;
    }

    public void markExpiredCodes() {
        int count = otpCodeDao.markExpiredCodes(Instant.now());
        logger.info("Expired OTP codes updated: count=" + count);
    }
}
