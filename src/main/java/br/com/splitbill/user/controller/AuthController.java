package br.com.splitbill.user.controller;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.splitbill.user.dto.AuthResponse;
import br.com.splitbill.user.dto.LoginRequest;
import br.com.splitbill.user.dto.RegisterUserRequest;
import br.com.splitbill.user.dto.RefreshRequest;
import br.com.splitbill.user.model.RefreshToken;
import br.com.splitbill.user.model.User;
import br.com.splitbill.user.repository.RefreshTokenRepository;
import br.com.splitbill.user.repository.UserRepository;
import br.com.splitbill.user.service.AuthService;
import br.com.splitbill.user.service.RefreshTokenService;
import br.com.splitbill.security.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Validated @RequestBody RegisterUserRequest req) {
        User user = authService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Validated @RequestBody LoginRequest req, HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");

        AuthService.AuthResult result = authService.login(req, ipAddress, userAgent);
        ResponseCookie cookie = result.refreshCookie();
        AuthResponse body = new AuthResponse(result.accessToken());
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(body);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request) {
        Optional<Cookie> cookieOpt = getRefreshCookie(request);
        if (cookieOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String cookieValue = cookieOpt.get().getValue();
        String[] parts = cookieValue.split(":", 2);
        if (parts.length != 2) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            UUID tokenId = UUID.fromString(parts[0]);
            String raw = parts[1];
            Optional<RefreshToken> storedOpt = refreshTokenRepository.findByTokenId(tokenId);
            if (storedOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            RefreshToken stored = storedOpt.get();

            String currentIp = getClientIpAddress(request);
            String currentUserAgent = request.getHeader("User-Agent");

            // Reuse Detection
            if (Boolean.TRUE.equals(stored.getRevoked())) {
                log.warn("🚨 REUSE DETECTION: Revoked token used for user {}. IP: {}, UA: {}", stored.getUser().getId(), currentIp, currentUserAgent);
                refreshTokenService.revokeAllUserTokens(stored.getUser());
                ResponseCookie delete = refreshTokenService.deleteRefreshCookie();
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).header(HttpHeaders.SET_COOKIE, delete.toString()).build();
            }

            if (!refreshTokenService.validateRefreshToken(stored, raw)) {
                log.warn("Invalid refresh token attempt for token ID: {}", tokenId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Context validation (Warn on IP change, might reject on UA change but warning is safer for now)
            if (stored.getUserAgent() != null && !stored.getUserAgent().equals(currentUserAgent)) {
                log.warn("User-Agent mismatch during refresh for user {}. Old: {}, New: {}", stored.getUser().getId(), stored.getUserAgent(), currentUserAgent);
            }
            if (stored.getIpAddress() != null && !stored.getIpAddress().equals(currentIp)) {
                log.warn("IP mismatch during refresh for user {}. Old: {}, New: {}", stored.getUser().getId(), stored.getIpAddress(), currentIp);
            }

            User user = stored.getUser();
            String accessToken = jwtService.generateAccessToken(user);
            // rotate
            String newCookieValue = refreshTokenService.rotateRefreshToken(stored, user, currentIp, currentUserAgent);
            ResponseCookie cookie = refreshTokenService.buildRefreshCookie(newCookieValue);

            AuthResponse body = new AuthResponse(accessToken);
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(body);

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        Optional<Cookie> cookieOpt = getRefreshCookie(request);
        if (cookieOpt.isPresent()) {
            String cookieValue = cookieOpt.get().getValue();
            String[] parts = cookieValue.split(":", 2);
            if (parts.length == 2) {
                try {
                    UUID tokenId = UUID.fromString(parts[0]);
                    refreshTokenService.revokeByTokenId(tokenId);
                } catch (IllegalArgumentException ex) {
                    // ignore invalid uuid
                }
            }
        }

        ResponseCookie delete = refreshTokenService.deleteRefreshCookie();
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, delete.toString()).build();
    }

    private Optional<Cookie> getRefreshCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return Optional.empty();
        return Arrays.stream(cookies).filter(c -> "refreshToken".equals(c.getName())).findFirst();
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        }
        return xForwardedForHeader.split(",")[0].trim();
    }

}
