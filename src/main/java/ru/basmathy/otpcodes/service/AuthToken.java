package ru.basmathy.otpcodes.service;

public class AuthToken {
    private final String token;
    private final long expiresInSeconds;

    public AuthToken(String token, long expiresInSeconds) {
        this.token = token;
        this.expiresInSeconds = expiresInSeconds;
    }

    public String getToken() {
        return token;
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }
}
