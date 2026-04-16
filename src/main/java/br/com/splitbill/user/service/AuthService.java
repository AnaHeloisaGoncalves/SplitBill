package br.com.splitbill.user.service;

import java.util.Optional;

import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import br.com.splitbill.security.JwtService;
import br.com.splitbill.user.dto.LoginRequest;
import br.com.splitbill.user.dto.RegisterUserRequest;
import br.com.splitbill.user.exception.DuplicateEmailException;
import br.com.splitbill.user.model.User;
import br.com.splitbill.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public User register(RegisterUserRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new DuplicateEmailException("Email already registered: " + req.email());
        }

        User user = User.builder()
                .name(req.name())
                .email(req.email())
                .password(passwordEncoder.encode(req.password()))
                .build();

        return userRepository.save(user);
    }

    public AuthResult login(LoginRequest req, String ipAddress, String userAgent) {
        User user = userRepository.findByEmail(req.email());
        if (user == null) {
            throw new org.springframework.security.authentication.BadCredentialsException("Invalid credentials");
        }
        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new org.springframework.security.authentication.BadCredentialsException("Invalid credentials");
        }

        String accessToken = jwtService.generateAccessToken(user);
        String cookieValue = refreshTokenService.createRefreshTokenCookieValue(user, ipAddress, userAgent);
        ResponseCookie cookie = refreshTokenService.buildRefreshCookie(cookieValue);

        return new AuthResult(accessToken, cookie);
    }

    public record AuthResult(String accessToken, ResponseCookie refreshCookie) {
    }

}
