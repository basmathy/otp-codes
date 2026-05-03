package ru.basmathy.otpcodes.scheduler;

import ru.basmathy.otpcodes.service.ExpiredOtpService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OtpExpirationScheduler {
    private final ScheduledExecutorService executorService;
    private final ExpiredOtpService expiredOtpService;
    private final long intervalSeconds;

    public OtpExpirationScheduler(ExpiredOtpService expiredOtpService, long intervalSeconds) {
        this.expiredOtpService = expiredOtpService;
        this.intervalSeconds = intervalSeconds;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        executorService.scheduleAtFixedRate(
                expiredOtpService::markExpiredCodes,
                intervalSeconds,
                intervalSeconds,
                TimeUnit.SECONDS
        );
    }

    public void stop() {
        executorService.shutdown();
    }
}
