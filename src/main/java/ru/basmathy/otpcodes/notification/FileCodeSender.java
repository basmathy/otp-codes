package ru.basmathy.otpcodes.notification;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.logging.Logger;

public class FileCodeSender implements CodeSender {
    private static final Logger logger = Logger.getLogger(FileCodeSender.class.getName());

    private final Path filePath;

    public FileCodeSender(String filePath) {
        this.filePath = Path.of(filePath);
    }

    @Override
    public void sendCode(String destination, String code) {
        String line = Instant.now() + " destination=" + destination + " code=" + code + System.lineSeparator();
        try {
            Files.writeString(
                    filePath,
                    line,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
            logger.info("OTP code saved to file");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save OTP code to file", e);
        }
    }
}
