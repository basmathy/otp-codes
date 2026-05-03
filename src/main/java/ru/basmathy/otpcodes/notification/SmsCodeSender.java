package ru.basmathy.otpcodes.notification;

import org.jsmpp.bean.Alphabet;
import org.jsmpp.bean.BindType;
import org.jsmpp.bean.ESMClass;
import org.jsmpp.bean.GeneralDataCoding;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.RegisteredDelivery;
import org.jsmpp.bean.SMSCDeliveryReceipt;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;
import ru.basmathy.otpcodes.config.SmsConfig;

import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class SmsCodeSender implements CodeSender {
    private static final Logger logger = Logger.getLogger(SmsCodeSender.class.getName());

    private final SmsConfig smsConfig;

    public SmsCodeSender(SmsConfig smsConfig) {
        this.smsConfig = smsConfig;
    }

    @Override
    public void sendCode(String destination, String code) {
        if (!smsConfig.isEnabled()) {
            logger.info("SMS sending is disabled. Code for " + destination + ": " + code);
            return;
        }

        SMPPSession session = new SMPPSession();
        try {
            BindParameter bindParameter = new BindParameter(
                    BindType.BIND_TX,
                    smsConfig.getSystemId(),
                    smsConfig.getPassword(),
                    smsConfig.getSystemType(),
                    TypeOfNumber.UNKNOWN,
                    NumberingPlanIndicator.UNKNOWN,
                    smsConfig.getSourceAddress()
            );
            session.connectAndBind(smsConfig.getHost(), smsConfig.getPort(), bindParameter);
            session.submitShortMessage(
                    smsConfig.getSystemType(),
                    TypeOfNumber.UNKNOWN,
                    NumberingPlanIndicator.UNKNOWN,
                    smsConfig.getSourceAddress(),
                    TypeOfNumber.UNKNOWN,
                    NumberingPlanIndicator.UNKNOWN,
                    destination,
                    new ESMClass(),
                    (byte) 0,
                    (byte) 1,
                    null,
                    null,
                    new RegisteredDelivery(SMSCDeliveryReceipt.DEFAULT),
                    (byte) 0,
                    new GeneralDataCoding(Alphabet.ALPHA_DEFAULT),
                    (byte) 0,
                    ("Your code: " + code).getBytes(StandardCharsets.UTF_8)
            );
            logger.info("SMS OTP code sent");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to send SMS OTP code", e);
        } finally {
            session.unbindAndClose();
        }
    }
}
