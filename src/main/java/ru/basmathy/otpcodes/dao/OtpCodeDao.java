package ru.basmathy.otpcodes.dao;

import ru.basmathy.otpcodes.db.ConnectionFactory;
import ru.basmathy.otpcodes.model.OtpCode;
import ru.basmathy.otpcodes.model.OtpStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

public class OtpCodeDao {
    private final ConnectionFactory connectionFactory;

    public OtpCodeDao(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public long save(OtpCode otpCode) {
        String sql = """
                insert into otp_codes (user_id, operation_id, code, status, expires_at)
                values (?, ?, ?, ?, ?)
                """;

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, otpCode.getUserId());
            statement.setString(2, otpCode.getOperationId());
            statement.setString(3, otpCode.getCode());
            statement.setString(4, otpCode.getStatus().name());
            statement.setTimestamp(5, Timestamp.from(otpCode.getExpiresAt()));
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
                throw new IllegalStateException("OTP code id was not generated");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save OTP code", e);
        }
    }

    public Optional<OtpCode> findActiveByUserAndOperation(long userId, String operationId) {
        String sql = """
                select id, user_id, operation_id, code, status, created_at, expires_at, used_at
                from otp_codes
                where user_id = ? and operation_id = ? and status = ?
                order by created_at desc
                limit 1
                """;

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setString(2, operationId);
            statement.setString(3, OtpStatus.ACTIVE.name());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapOtpCode(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find active OTP code", e);
        }
    }

    public void markUsed(long id) {
        String sql = "update otp_codes set status = ?, used_at = ? where id = ?";

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, OtpStatus.USED.name());
            statement.setTimestamp(2, Timestamp.from(Instant.now()));
            statement.setLong(3, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to mark OTP code as used", e);
        }
    }

    public void markExpired(long id) {
        String sql = "update otp_codes set status = ? where id = ?";

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, OtpStatus.EXPIRED.name());
            statement.setLong(2, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to mark OTP code as expired", e);
        }
    }

    public int markExpiredCodes(Instant now) {
        String sql = "update otp_codes set status = ? where status = ? and expires_at <= ?";

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, OtpStatus.EXPIRED.name());
            statement.setString(2, OtpStatus.ACTIVE.name());
            statement.setTimestamp(3, Timestamp.from(now));
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to mark expired OTP codes", e);
        }
    }

    public void deleteByUserId(long userId) {
        String sql = "delete from otp_codes where user_id = ?";

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete OTP codes by user id", e);
        }
    }

    private OtpCode mapOtpCode(ResultSet resultSet) throws SQLException {
        Timestamp usedAt = resultSet.getTimestamp("used_at");
        return new OtpCode(
                resultSet.getLong("id"),
                resultSet.getLong("user_id"),
                resultSet.getString("operation_id"),
                resultSet.getString("code"),
                OtpStatus.valueOf(resultSet.getString("status")),
                resultSet.getTimestamp("created_at").toInstant(),
                resultSet.getTimestamp("expires_at").toInstant(),
                usedAt == null ? null : usedAt.toInstant()
        );
    }
}
