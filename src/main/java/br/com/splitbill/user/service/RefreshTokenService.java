package br.com.splitbill.user.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.splitbill.user.model.RefreshToken;
import br.com.splitbill.user.model.User;
import br.com.splitbill.user.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.refresh-token-expiration-days}")
    private long refreshTokenExpirationDays;

    @Value("${app.cookie.secure:true}")
    private boolean cookieSecure;

    @Value("${app.cookie.same-site:Strict}")
    private String cookieSameSite;

    @Value("${app.cookie.domain:}")
    private String cookieDomain;

    @Transactional
    public String createRefreshTokenCookieValue(User user, String ipAddress, String userAgent) {
        UUID tokenId = UUID.randomUUID();
        String rawToken = UUID.randomUUID().toString();
        String hashed = passwordEncoder.encode(rawToken);

        RefreshToken rt = RefreshToken.builder()
                .tokenId(tokenId)
                .token(hashed)
                .user(user)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .expiryDate(Instant.now().plus(refreshTokenExpirationDays, ChronoUnit.DAYS))
                .revoked(false)
                .build();

        refreshTokenRepository.save(rt);

        // cookie value will be tokenId:rawToken
        return tokenId.toString() + ":" + rawToken;
    }

    public Optional<RefreshToken> findByTokenId(UUID tokenId) {
        return refreshTokenRepository.findByTokenId(tokenId);
    }

    public boolean validateRefreshToken(RefreshToken stored, String rawToken) {
        if (stored == null) return false;
        if (Boolean.TRUE.equals(stored.getRevoked())) return false;
        if (stored.getExpiryDate().isBefore(Instant.now())) return false;
        return passwordEncoder.matches(rawToken, stored.getToken());
    }

    @Transactional
    public String rotateRefreshToken(RefreshToken old, User user, String ipAddress, String userAgent) {
        // revoke old
        old.setRevoked(true);
        refreshTokenRepository.save(old);
        // create new
        return createRefreshTokenCookieValue(user, ipAddress, userAgent);
    }

    @Transactional
    public void revokeByTokenId(UUID tokenId) {
        refreshTokenRepository.findByTokenId(tokenId).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    @Transactional
    public void revokeAllUserTokens(User user) {
        log.warn("Revoking all tokens for user id: {}", user.getId());
        refreshTokenRepository.revokeAllByUser(user);
    }

    @Transactional
    public void deleteExpiredTokens(Instant now) {
        refreshTokenRepository.deleteAllByExpiryDateBefore(now);
    }

    public ResponseCookie buildRefreshCookie(String cookieValue) {
        // cookie age in seconds
        long maxAge = refreshTokenExpirationDays * 24 * 60 * 60;
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from("refreshToken", cookieValue)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(maxAge)
                .sameSite(cookieSameSite);
        
        if (cookieDomain != null && !cookieDomain.isEmpty() && !cookieDomain.equals("\"\"")) {
            builder.domain(cookieDomain);
        }

        return builder.build();
    }

    public ResponseCookie deleteRefreshCookie() {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite(cookieSameSite);
                
        if (cookieDomain != null && !cookieDomain.isEmpty() && !cookieDomain.equals("\"\"")) {
            builder.domain(cookieDomain);
        }

        return builder.build();
    }

}