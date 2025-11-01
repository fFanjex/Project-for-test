package org.example.projectfortest.controller;

import org.example.projectfortest.dto.LoginRequest;
import org.example.projectfortest.dto.RegisterRequest;
import org.example.projectfortest.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Map;

public class AuthControllerTest {
    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_shouldReturnOkResponseWithUserData() {
        RegisterRequest request = new RegisterRequest("test@mail.ru", "password123");
        Map<String, Object> mockResponse = Map.of(
                "accessToken", "access123",
                "refreshToken", "refresh123"
        );
        when(userService.register(request)).thenReturn(mockResponse);
        ResponseEntity<?> response = authController.register(request);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(mockResponse);
        verify(userService, times(1)).register(request);
    }

    @Test
    void login_shouldReturnTokens() {
        LoginRequest request = new LoginRequest("test@mail.ru", "password123");
        Map<String, String> mockTokens = Map.of(
                "accessToken", "access123",
                "refreshToken", "refresh123"
        );
        when(userService.login(request.getEmail(), request.getPassword())).thenReturn(mockTokens);
        ResponseEntity<?> response = authController.login(request);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(mockTokens);
        verify(userService, times(1)).login(request.getEmail(),
                request.getPassword());
    }

    @Test
    void recovery_ShouldReturnOkStatus() {
        when(userService.recoveryPassword(anyString(), anyString()))
                .thenReturn(Map.of("message", "Password updated successfully"));
        ResponseEntity<?> response = authController.recoveryPassword("user@example.com", "newPass123");
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(userService).recoveryPassword("user@example.com", "newPass123");
    }
}
