package br.com.splitbill.user.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
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
import com.jayway.jsonpath.JsonPath;

import br.com.splitbill.user.dto.LoginRequest;
import br.com.splitbill.user.dto.RegisterUserRequest;
import br.com.splitbill.user.dto.UpdateUserRequest;
import br.com.splitbill.user.repository.RefreshTokenRepository;
import br.com.splitbill.user.repository.UserRepository;
import br.com.splitbill.user.service.AuthService;

@SpringBootTest
@ActiveProfiles("test")
public class UserControllerTest {

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

    private String validAccessToken;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        RegisterUserRequest req = new RegisterUserRequest("Diego", "diego@split.com", "password123");
        authService.register(req);

        // register secondary user for test
        RegisterUserRequest req2 = new RegisterUserRequest("Ana", "ana@split.com", "password123");
        authService.register(req2);

        // authenticate context user
        LoginRequest login = new LoginRequest("diego@split.com", "password123");
        MvcResult res = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andReturn();
        
        validAccessToken = JsonPath.read(res.getResponse().getContentAsString(), "$.accessToken");
    }

    @AfterEach
    void tearDown() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder post(String url) {
        return org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(url);
    }

    @Test
    void testGetMyProfile_Success() throws Exception {
        mockMvc.perform(get("/users/me")
                .header("Authorization", "Bearer " + validAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Diego"))
                .andExpect(jsonPath("$.email").value("diego@split.com"));
    }

    @Test
    void testGetMyProfile_Unauthenticated_Returns403() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateMyProfile_SuccessNameOnly() throws Exception {
        UpdateUserRequest updateReq = new UpdateUserRequest("Diego Updated", null, null);

        mockMvc.perform(put("/users/me")
                .header("Authorization", "Bearer " + validAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Diego Updated"))
                .andExpect(jsonPath("$.email").value("diego@split.com"));
    }

    @Test
    void testUpdateMyProfile_DuplicateEmail_Returns409() throws Exception {
        // try to steal Ana's email
        UpdateUserRequest updateReq = new UpdateUserRequest("Diego", "ana@split.com", null);

        mockMvc.perform(put("/users/me")
                .header("Authorization", "Bearer " + validAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Email já cadastrado: ana@split.com"));
    }

    @Test
    void testUpdateMyProfile_ValidationFails() throws Exception {
        // Name is < 3 characters
        UpdateUserRequest updateReq = new UpdateUserRequest("Di", null, null);

        mockMvc.perform(put("/users/me")
                .header("Authorization", "Bearer " + validAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void testSearchUsers_PaginationAndFilter() throws Exception {
        // search without query should return both users
        mockMvc.perform(get("/users")
                .header("Authorization", "Bearer " + validAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));

        // search query "Ana"
        mockMvc.perform(get("/users?q=Ana")
                .header("Authorization", "Bearer " + validAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Ana"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}
