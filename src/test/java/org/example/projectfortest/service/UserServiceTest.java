package org.example.projectfortest.service;

import org.example.projectfortest.config.JwtTokenProvider;
import org.example.projectfortest.dto.RegisterRequest;
import org.example.projectfortest.entity.User;
import org.example.projectfortest.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_shouldSaveUserAndReturnTokens() {
        RegisterRequest request = new RegisterRequest("new@mail.com", "pass123");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPass");

        User savedUser = User.builder().email(request.getEmail()).password("encodedPass").build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtTokenProvider.generateAccessToken(savedUser)).thenReturn("access123");
        when(jwtTokenProvider.generateRefreshToken(savedUser)).thenReturn("refresh123");

        Map<String, Object> result = userService.register(request);

        assertThat(result).containsEntry("user", savedUser);
        assertThat(result.get("accessToken")).isEqualTo("access123");
        assertThat(result.get("refreshToken")).isEqualTo("refresh123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldThrowIfEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("exist@mail.com", "pass123");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email is already in use");
    }

    @Test
    void login_shouldReturnTokensIfPasswordMatches() {
        User user = User.builder().email("user@mail.com").password("encodedPass").build();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass123", "encodedPass")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(user)).thenReturn("access123");
        when(jwtTokenProvider.generateRefreshToken(user)).thenReturn("refresh123");

        Map<String, String> tokens = userService.login(user.getEmail(), "pass123");

        assertThat(tokens).containsEntry("accessToken", "access123")
                .containsEntry("refreshToken", "refresh123");
    }

    @Test
    void login_shouldThrowIfPasswordDoesNotMatch() {
        User user = User.builder().email("user@mail.com").password("encodedPass").build();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPass", "encodedPass")).thenReturn(false);

        assertThatThrownBy(() -> userService.login(user.getEmail(), "wrongPass"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void login_shouldThrowIfUserNotFound() {
        when(userRepository.findByEmail("no@mail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login("no@mail.com", "password"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void recoveryPassword_shouldUpdatePasswordWhenUserExists() {
        User user = User.builder().email("user@mail.com").password("oldPass").build();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");
        when(userRepository.save(any(User.class))).thenReturn(user);

        Map<String, String> result = userService.recoveryPassword(user.getEmail(), "newPass");

        assertThat(result.get("message")).isEqualTo("Password updated successfully");
        assertThat(result.get("email")).isEqualTo(user.getEmail());
        verify(userRepository).save(user);
    }

    @Test
    void recoveryPassword_shouldThrowIfUserNotFound() {
        when(userRepository.findByEmail("no@mail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.recoveryPassword("no@mail.com", "newPass"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email is empty");
    }
}