package ru.basmathy.otpcodes.dao;

import ru.basmathy.otpcodes.db.ConnectionFactory;
import ru.basmathy.otpcodes.model.OtpConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OtpConfigDao {
    private final ConnectionFactory connectionFactory;

    public OtpConfigDao(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public OtpConfig getConfig() {
        String sql = "select id, code_length, lifetime_seconds from otp_settings where id = 1";

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return mapConfig(resultSet);
            }
            throw new IllegalStateException("OTP settings row not found");
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load OTP settings", e);
        }
    }

    public void updateConfig(int codeLength, int lifetimeSeconds) {
        String sql = """
                update otp_settings
                set code_length = ?, lifetime_seconds = ?
                where id = 1
                """;

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, codeLength);
            statement.setInt(2, lifetimeSeconds);

            int updatedRows = statement.executeUpdate();
            if (updatedRows == 0) {
                throw new IllegalStateException("OTP settings row not found");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update OTP settings", e);
        }
    }

    private OtpConfig mapConfig(ResultSet resultSet) throws SQLException {
        return new OtpConfig(
                resultSet.getInt("id"),
                resultSet.getInt("code_length"),
                resultSet.getInt("lifetime_seconds")
        );
    }
}
