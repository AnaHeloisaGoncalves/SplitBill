package br.com.splitbill.user.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.splitbill.user.dto.LoginRequest;
import br.com.splitbill.user.dto.RegisterUserRequest;
import br.com.splitbill.user.model.User;
import br.com.splitbill.user.repository.RefreshTokenRepository;
import br.com.splitbill.user.repository.UserRepository;
import br.com.splitbill.user.service.AuthService;
import jakarta.servlet.http.Cookie;

@SpringBootTest
@ActiveProfiles("test")
public class AuthControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private org.springframework.web.context.WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    
    @Autowired
    private AuthService authService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        RegisterUserRequest req = new RegisterUserRequest("Test User", "test@example.com", "password123");
        authService.register(req);
    }

    @Test
    void testLoginSuccess() throws Exception {
        LoginRequest req = new LoginRequest("test@example.com", "password123");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(cookie().exists("refreshToken"));
    }

    @Test
    void testLoginFailure() throws Exception {
        LoginRequest req = new LoginRequest("test@example.com", "wrongpassword");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void testRefreshSuccess() throws Exception {
        LoginRequest loginReq = new LoginRequest("test@example.com", "password123");

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andReturn();

        Cookie refreshCookie = loginResult.getResponse().getCookie("refreshToken");

        mockMvc.perform(post("/auth/refresh").cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(cookie().exists("refreshToken"));
    }

    @Test
    void testRefreshInvalidTokenFormat() throws Exception {
        Cookie badCookie = new Cookie("refreshToken", "invalid-format");

        mockMvc.perform(post("/auth/refresh").cookie(badCookie))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testRefreshReuseDetection() throws Exception {
        LoginRequest loginReq = new LoginRequest("test@example.com", "password123");

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andReturn();

        Cookie refreshCookie = loginResult.getResponse().getCookie("refreshToken");

        // 1st Refresh - rotates the token 
        mockMvc.perform(post("/auth/refresh").cookie(refreshCookie))
                .andExpect(status().isOk());

        // At this point, the original 'refreshCookie' was rotated, so it's marked as revoked in the DB.
        // Let's try to REUSE the revoked token.
        mockMvc.perform(post("/auth/refresh").cookie(refreshCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(cookie().maxAge("refreshToken", 0)); // we clear the cookie

        // Now, verify that ALL tokens for this user are revoked (Reuse Detection activated!)
        User user = userRepository.findByEmail("test@example.com");
        long validTokens = refreshTokenRepository.findByUserAndRevokedFalse(user).stream().count();
        assertEquals(0, validTokens, "All tokens must be revoked after reuse detection");
    }

    @Test
    void testLogout() throws Exception {
        LoginRequest loginReq = new LoginRequest("test@example.com", "password123");

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andReturn();

        Cookie refreshCookie = loginResult.getResponse().getCookie("refreshToken");

        mockMvc.perform(post("/auth/logout").cookie(refreshCookie))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("refreshToken", 0));
        
        // Ensure token was revoked in DB
        User user = userRepository.findByEmail("test@example.com");
        long validTokens = refreshTokenRepository.findByUserAndRevokedFalse(user).stream().count();
        assertEquals(0, validTokens);
    }
}
