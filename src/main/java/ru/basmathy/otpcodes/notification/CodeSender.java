package ru.basmathy.otpcodes.notification;

public interface CodeSender {
    void sendCode(String destination, String code);
}
