package ru.basmathy.otpcodes.dao;

import ru.basmathy.otpcodes.db.ConnectionFactory;
import ru.basmathy.otpcodes.model.User;
import ru.basmathy.otpcodes.model.UserRole;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {
    private final ConnectionFactory connectionFactory;

    public UserDao(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Optional<User> findByLogin(String login) {
        String sql = "select id, login, password_hash, role, created_at from app_users where login = ?";

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, login);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapUser(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find user by login", e);
        }
    }

    public Optional<User> findById(long id) {
        String sql = "select id, login, password_hash, role, created_at from app_users where id = ?";

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapUser(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find user by id", e);
        }
    }

    public long save(User user) {
        String sql = "insert into app_users (login, password_hash, role) values (?, ?, ?)";

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getLogin());
            statement.setString(2, user.getPasswordHash());
            statement.setString(3, user.getRole().name());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
                throw new IllegalStateException("User id was not generated");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save user", e);
        }
    }

    public boolean adminExists() {
        String sql = "select 1 from app_users where role = ? limit 1";

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, UserRole.ADMIN.name());

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to check admin existence", e);
        }
    }

    public List<User> findAllUsersWithoutAdmins() {
        String sql = """
                select id, login, password_hash, role, created_at
                from app_users
                where role <> ?
                order by id
                """;

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, UserRole.ADMIN.name());

            try (ResultSet resultSet = statement.executeQuery()) {
                List<User> users = new ArrayList<>();
                while (resultSet.next()) {
                    users.add(mapUser(resultSet));
                }
                return users;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load users without admins", e);
        }
    }

    public void deleteById(long id) {
        String sql = "delete from app_users where id = ?";

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete user", e);
        }
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        return new User(
                resultSet.getLong("id"),
                resultSet.getString("login"),
                resultSet.getString("password_hash"),
                UserRole.valueOf(resultSet.getString("role")),
                createdAt.toInstant()
        );
    }
}
