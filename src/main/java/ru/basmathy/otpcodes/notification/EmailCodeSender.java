package ru.basmathy.otpcodes.notification;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import ru.basmathy.otpcodes.config.MailConfig;

import java.util.logging.Logger;

public class EmailCodeSender implements CodeSender {
    private static final Logger logger = Logger.getLogger(EmailCodeSender.class.getName());

    private final MailConfig mailConfig;
    private final Session session;

    public EmailCodeSender(MailConfig mailConfig) {
        this.mailConfig = mailConfig;
        this.session = Session.getInstance(mailConfig.getProperties(), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailConfig.getUsername(), mailConfig.getPassword());
            }
        });
    }

    @Override
    public void sendCode(String destination, String code) {
        if (!mailConfig.isEnabled()) {
            logger.info("Email sending is disabled. Code for " + destination + ": " + code);
            return;
        }

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(mailConfig.getFromEmail()));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(destination));
            message.setSubject("Your OTP Code");
            message.setText("Your verification code is: " + code);
            Transport.send(message);
            logger.info("Email OTP code sent");
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to send email OTP code", e);
        }
    }
}
