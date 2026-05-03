package ru.basmathy.otpcodes.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class MigrationRunner {
    private static final Logger logger = Logger.getLogger(MigrationRunner.class.getName());

    private final ConnectionFactory connectionFactory;

    public MigrationRunner(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void runMigrations() {
        String sql = readSchemaSql();

        try (Connection connection = connectionFactory.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
            logger.info("Database schema migration completed");
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to execute database schema migration", e);
        }
    }

    private String readSchemaSql() {
        try (InputStream inputStream = MigrationRunner.class.getClassLoader()
                .getResourceAsStream("db/schema.sql")) {
            if (inputStream == null) {
                throw new IllegalStateException("db/schema.sql not found");
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read db/schema.sql", e);
        }
    }
}
