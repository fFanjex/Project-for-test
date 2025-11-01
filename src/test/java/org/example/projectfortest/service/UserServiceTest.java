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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_shouldSaveUserAndReturnTokens() {
        RegisterRequest request = new RegisterRequest("test@mail.ru", "password123");
        User user = User.builder()
                .id("1")
                .email("test@mail.ru")
                .password("password123")
                .build();
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtTokenProvider.generateAccessToken(user)).thenReturn("access123");
        when(jwtTokenProvider.generateRefreshToken(user)).thenReturn("refresh123");
        Map<String, Object> result = userService.register(request);
        assertThat(result).containsKeys("user", "accessToken", "refreshToken");
        assertThat(result.get("accessToken")).isEqualTo("access123");
        assertThat(result.get("refreshToken")).isEqualTo("refresh123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_shouldThrowIfEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("test@mail.ru", "password123");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));
        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email is already in use");
    }

    @Test
    void login_shouldReturnTokensIfPasswordMatches() {
        User user = User.builder()
                .id("1")
                .email("test@mail.ru")
                .password("encodedPassword")
                .build();
        when(userRepository.findByEmail("test@mail.ru")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(user)).thenReturn("access123");
        when(jwtTokenProvider.generateRefreshToken(user)).thenReturn("refresh123");
        Map<String, String> tokens = userService.login("test@mail.ru", "password123");
        assertThat(tokens).containsEntry("accessToken", "access123")
                .containsEntry("refreshToken", "refresh123");
    }

    @Test
    void login_shouldThrowIfUserNotFound() {
        when(userRepository.findByEmail("no@mail.ru")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.login("no@mail.com", "password"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void login_shouldThrowIfPasswordDoesNotMatch() {
        User user = User.builder()
                .email("test@mail.com")
                .password("encodedPass")
                .build();

        when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPass", "encodedPass")).thenReturn(false);

        assertThatThrownBy(() -> userService.login("test@mail.com", "wrongPass"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void recoveryPassword_ShouldUpdatePassword_WhenUserExists() {
        User user = User.builder().email("test@mail.ru").password("oldPassword").build();
        when(userRepository.findByEmail("test@mail.ru")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(user);
        Map<String, String> result = userService.recoveryPassword("test@mail.ru", "newPassword");
        assertThat(result.get("message")).isEqualTo("Password updated successfully");
        assertThat(result.get("email")).isEqualTo("test@mail.ru");
        verify(userRepository).save(user);
    }

    @Test
    void recoveryPassword_ShouldThrow_WhenUserNotFound() {
        when(userRepository.findByEmail("no@mail.ru")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> userService.recoveryPassword("notfound@example.com", "newPass"));
    }
}
