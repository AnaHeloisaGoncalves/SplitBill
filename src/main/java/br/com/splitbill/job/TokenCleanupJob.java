package br.com.splitbill.job;

import java.time.Instant;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.com.splitbill.user.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupJob {

    private final RefreshTokenService refreshTokenService;

    // Run every hour at minute 0
    @Scheduled(cron = "0 0 * * * *")
    public void cleanupTokens() {
        log.info("Starting automatic cleanup job for expired refresh tokens");
        try {
            refreshTokenService.deleteExpiredTokens(Instant.now());
            log.info("Successfully finished automatic cleanup job");
        } catch (Exception e) {
            log.error("Error executing token cleanup job", e);
        }
    }
}
