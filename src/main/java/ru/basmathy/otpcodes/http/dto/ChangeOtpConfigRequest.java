package ru.basmathy.otpcodes.http.dto;

public class ChangeOtpConfigRequest {
    private int codeLength;
    private int lifetimeSeconds;

    public int getCodeLength() {
        return codeLength;
    }

    public void setCodeLength(int codeLength) {
        this.codeLength = codeLength;
    }

    public int getLifetimeSeconds() {
        return lifetimeSeconds;
    }

    public void setLifetimeSeconds(int lifetimeSeconds) {
        this.lifetimeSeconds = lifetimeSeconds;
    }
}
