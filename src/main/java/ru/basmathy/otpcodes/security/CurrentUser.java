package ru.basmathy.otpcodes.security;

import ru.basmathy.otpcodes.model.UserRole;

public class CurrentUser {
    private final long id;
    private final String login;
    private final UserRole role;

    public CurrentUser(long id, String login, UserRole role) {
        this.id = id;
        this.login = login;
        this.role = role;
    }

    public long getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public UserRole getRole() {
        return role;
    }
}
