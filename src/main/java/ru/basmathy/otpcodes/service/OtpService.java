package ru.basmathy.otpcodes.service;

import ru.basmathy.otpcodes.dao.OtpCodeDao;
import ru.basmathy.otpcodes.dao.OtpConfigDao;
import ru.basmathy.otpcodes.exception.BadRequestException;
import ru.basmathy.otpcodes.model.DeliveryChannel;
import ru.basmathy.otpcodes.model.OtpCode;
import ru.basmathy.otpcodes.model.OtpConfig;
import ru.basmathy.otpcodes.model.OtpStatus;
import ru.basmathy.otpcodes.notification.CodeSender;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Locale;
import java.util.logging.Logger;

public class OtpService {
    private static final Logger logger = Logger.getLogger(OtpService.class.getName());

    private final OtpCodeDao otpCodeDao;
    private final OtpConfigDao otpConfigDao;
    private final CodeSender emailCodeSender;
    private final CodeSender smsCodeSender;
    private final CodeSender telegramCodeSender;
    private final CodeSender fileCodeSender;
    private final SecureRandom secureRandom = new SecureRandom();

    public OtpService(OtpCodeDao otpCodeDao, OtpConfigDao otpConfigDao,
                      CodeSender emailCodeSender, CodeSender smsCodeSender,
                      CodeSender telegramCodeSender, CodeSender fileCodeSender) {
        this.otpCodeDao = otpCodeDao;
        this.otpConfigDao = otpConfigDao;
        this.emailCodeSender = emailCodeSender;
        this.smsCodeSender = smsCodeSender;
        this.telegramCodeSender = telegramCodeSender;
        this.fileCodeSender = fileCodeSender;
    }

    public long createOtpCode(long userId, String operationId, String channelText, String destination) {
        validateOperationId(operationId);
        validateDestination(destination);
        DeliveryChannel channel = parseChannel(channelText);

        OtpConfig config = otpConfigDao.getConfig();
        String code = generateNumericCode(config.getCodeLength());

        OtpCode otpCode = new OtpCode();
        otpCode.setUserId(userId);
        otpCode.setOperationId(operationId);
        otpCode.setCode(code);
        otpCode.setStatus(OtpStatus.ACTIVE);
        otpCode.setExpiresAt(Instant.now().plusSeconds(config.getLifetimeSeconds()));

        long id = otpCodeDao.save(otpCode);
        sendCode(channel, destination, code);
        logger.info("OTP generated for operation " + operationId);
        return id;
    }

    public void checkOtpCode(long userId, String operationId, String code) {
        validateOperationId(operationId);
        if (code == null || code.isBlank()) {
            throw new BadRequestException("Code must not be empty");
        }

        OtpCode otpCode = otpCodeDao.findActiveByUserAndOperation(userId, operationId)
                .orElseThrow(() -> new BadRequestException("Active OTP code not found"));

        if (otpCode.getExpiresAt().isBefore(Instant.now()) || otpCode.getExpiresAt().equals(Instant.now())) {
            otpCodeDao.markExpired(otpCode.getId());
            logger.info("OTP validation failed: expired code");
            throw new BadRequestException("OTP code expired");
        }

        if (!otpCode.getCode().equals(code)) {
            logger.info("OTP validation failed: wrong code");
            throw new BadRequestException("Wrong OTP code");
        }

        otpCodeDao.markUsed(otpCode.getId());
        logger.info("OTP code used for operation " + operationId);
    }

    private void sendCode(DeliveryChannel channel, String destination, String code) {
        switch (channel) {
            case EMAIL -> emailCodeSender.sendCode(destination, code);
            case SMS -> smsCodeSender.sendCode(destination, code);
            case TELEGRAM -> telegramCodeSender.sendCode(destination, code);
            case FILE -> fileCodeSender.sendCode(destination, code);
        }
    }

    private DeliveryChannel parseChannel(String channelText) {
        if (channelText == null || channelText.isBlank()) {
            throw new BadRequestException("Delivery channel must not be empty");
        }
        try {
            return DeliveryChannel.valueOf(channelText.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unknown delivery channel");
        }
    }

    private void validateOperationId(String operationId) {
        if (operationId == null || operationId.isBlank() || operationId.length() > 120) {
            throw new BadRequestException("Operation id must not be empty and must be shorter than 120 characters");
        }
    }

    private void validateDestination(String destination) {
        if (destination == null || destination.isBlank()) {
            throw new BadRequestException("Destination must not be empty");
        }
    }

    private String generateNumericCode(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(secureRandom.nextInt(10));
        }
        return builder.toString();
    }
}
