package br.com.splitbill.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.splitbill.user.dto.UpdateUserRequest;
import br.com.splitbill.user.dto.UserResponse;
import br.com.splitbill.user.exception.DuplicateEmailException;
import br.com.splitbill.user.model.User;
import br.com.splitbill.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .publicId(UUID.randomUUID())
                .name("John Doe")
                .email("john@example.com")
                .password("encoded_pass")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testGetProfile() {
        UserResponse result = userService.getProfile(testUser);

        assertNotNull(result);
        assertEquals("John Doe", result.name());
        assertEquals("john@example.com", result.email());
        assertEquals(testUser.getPublicId().toString(), result.publicId());
    }

    @Test
    void testSearchUsers() {
        Page<User> page = new PageImpl<>(List.of(testUser));
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<UserResponse> result = userService.searchUsers("John", Pageable.unpaged());

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("John Doe", result.getContent().get(0).name());
    }

    @Test
    void testUpdateProfile_NameOnly() {
        UpdateUserRequest req = new UpdateUserRequest("John Updated", null, null);

        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse result = userService.updateProfile(testUser, req);

        assertNotNull(result);
        assertEquals("John Updated", testUser.getName());
        assertEquals("john@example.com", testUser.getEmail()); // remain unchanged
        verify(userRepository).save(testUser);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void testUpdateProfile_PasswordOnly() {
        UpdateUserRequest req = new UpdateUserRequest(null, null, "new_password");

        when(passwordEncoder.encode("new_password")).thenReturn("new_hash");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse result = userService.updateProfile(testUser, req);

        assertEquals("new_hash", testUser.getPassword());
        verify(passwordEncoder).encode("new_password");
        verify(userRepository).save(testUser);
    }

    @Test
    void testUpdateProfile_EmailChange_Success() {
        UpdateUserRequest req = new UpdateUserRequest(null, "new@example.com", null);

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse result = userService.updateProfile(testUser, req);

        assertEquals("new@example.com", testUser.getEmail());
        verify(userRepository).existsByEmail("new@example.com");
        verify(userRepository).save(testUser);
    }

    @Test
    void testUpdateProfile_EmailChange_DuplicateThrowsException() {
        UpdateUserRequest req = new UpdateUserRequest(null, "existing@example.com", null);

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> userService.updateProfile(testUser, req));

        // It shouldn't save
        verify(userRepository, never()).save(any());
    }
}
