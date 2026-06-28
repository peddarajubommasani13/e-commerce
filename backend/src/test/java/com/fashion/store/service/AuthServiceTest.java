package com.fashion.store.service;

import com.fashion.store.dto.AuthDTO.*;
import com.fashion.store.entity.User;
import com.fashion.store.exception.BadRequestException;
import com.fashion.store.repository.UserRepository;
import com.fashion.store.security.JwtUtil;
import com.fashion.store.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserDetailsService userDetailsService;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private JwtUtil jwtUtil;
    private AuthServiceImpl authService;
    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Stubbed JwtUtil to bypass Mockito bytecode generation issue on JDK 26
        jwtUtil = new JwtUtil() {
            @Override
            public String generateToken(String username, Map<String, Object> extraClaims) {
                return "mock-token";
            }
        };

        authService = new AuthServiceImpl(userRepository, passwordEncoder, jwtUtil, authenticationManager, userDetailsService);

        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .passwordHash(passwordEncoder.encode("Password1!"))
                .role(User.Role.USER)
                .build();
    }

    @Test
    @DisplayName("Register: success creates user and returns token")
    void register_success() {
        RegisterRequest req = new RegisterRequest();
        req.setName("Test User");
        req.setEmail("test@example.com");
        req.setPassword("Password1!");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userDetailsService.loadUserByUsername("test@example.com"))
                .thenReturn(new org.springframework.security.core.userdetails.User(
                        "test@example.com", testUser.getPasswordHash(), java.util.List.of()));

        AuthResponse response = authService.register(req);

        assertThat(response.getToken()).isEqualTo("mock-token");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getRole()).isEqualTo("USER");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Register: duplicate email throws BadRequestException")
    void register_duplicateEmail_throws() {
        RegisterRequest req = new RegisterRequest();
        req.setName("Test User");
        req.setEmail("test@example.com");
        req.setPassword("Password1!");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    @DisplayName("Login: valid credentials returns token")
    void login_success() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@example.com");
        req.setPassword("Password1!");

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userDetailsService.loadUserByUsername("test@example.com"))
                .thenReturn(new org.springframework.security.core.userdetails.User(
                        "test@example.com", testUser.getPasswordHash(), java.util.List.of()));

        AuthResponse response = authService.login(req);

        assertThat(response.getToken()).isEqualTo("mock-token");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Login: bad credentials throws exception")
    void login_badCredentials_throws() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@example.com");
        req.setPassword("wrong");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("GetCurrentUser: valid email returns profile")
    void getCurrentUser_success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        UserProfile profile = authService.getCurrentUser("test@example.com");

        assertThat(profile.getEmail()).isEqualTo("test@example.com");
        assertThat(profile.getName()).isEqualTo("Test User");
        assertThat(profile.getRole()).isEqualTo("USER");
    }
}
