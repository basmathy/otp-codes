package ru.basmathy.otpcodes.http.dto;

import ru.basmathy.otpcodes.model.User;

import java.time.Instant;

public class UserResponse {
    private final long id;
    private final String login;
    private final String role;
    private final Instant createdAt;

    public UserResponse(User user) {
        this.id = user.getId();
        this.login = user.getLogin();
        this.role = user.getRole().name();
        this.createdAt = user.getCreatedAt();
    }

    public long getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
