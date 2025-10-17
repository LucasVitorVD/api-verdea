package com.verdea.api_verdea.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.verdea.api_verdea.dtos.userDto.LoginResponse;
import com.verdea.api_verdea.dtos.userDto.UserRequestDTO;
import com.verdea.api_verdea.dtos.userDto.UserResponseDTO;
import com.verdea.api_verdea.enums.Role;
import com.verdea.api_verdea.services.authentication.AuthenticationService;
import com.verdea.api_verdea.services.authentication.CookieService;
import com.verdea.api_verdea.services.resetPassword.PasswordResetService;
import com.verdea.api_verdea.services.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private CookieService cookieService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private PasswordResetService passwordResetService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser
    @DisplayName("Register user successfully")
    void registerUser_success() throws Exception {
        UserRequestDTO request = new UserRequestDTO("email@gmail.com", "12345");
        UserResponseDTO response = new UserResponseDTO(1L, request.email(), Role.USER, LocalDateTime.now());
        String body = objectMapper.writeValueAsString(request);

        when(userService.registerUser(request)).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value(request.email()))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userService).registerUser(request);
    }

    @Test
    @WithMockUser
    @DisplayName("Authenticate and set cookies correctly")
    void authenticate_success() throws Exception {
        UserRequestDTO request = new UserRequestDTO("user@gmail.com", "123456");
        UUID refreshToken = UUID.randomUUID();
        String accessToken = "access-token";

        LoginResponse loginResponse = new LoginResponse(accessToken, refreshToken);

        when(authenticationService.authenticate(any(UserRequestDTO.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Usuário autenticado!"));

        verify(authenticationService).authenticate(any(UserRequestDTO.class));
        verify(cookieService).addAccessTokenCookie(any(HttpServletResponse.class), eq(accessToken));
        verify(cookieService).addRefreshTokenCookie(any(HttpServletResponse.class), eq(refreshToken));
    }

    @Test
    @WithMockUser
    @DisplayName("Refresh token successfully")
    void refreshToken_success() throws Exception {
        UUID refreshToken = UUID.randomUUID();
        String newAccessToken = "new-access-token";

        LoginResponse loginResponse = new LoginResponse(newAccessToken, refreshToken);

        when(cookieService.extractRefreshTokenFromCookies(any(HttpServletRequest.class))).thenReturn(refreshToken);
        when(authenticationService.refreshToken(refreshToken)).thenReturn(loginResponse);

        mockMvc.perform(post("/api/auth/refresh-token").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(newAccessToken))
                .andExpect(jsonPath("$.refreshToken").value(refreshToken.toString()));

        verify(cookieService).addAccessTokenCookie(any(HttpServletResponse.class), eq(newAccessToken));
        verify(cookieService).addRefreshTokenCookie(any(HttpServletResponse.class), eq(refreshToken));
    }

    @Test
    @WithMockUser
    @DisplayName("Refresh token fails without cookie")
    void refreshToken_noCookie() throws Exception {
        when(cookieService.extractRefreshTokenFromCookies(any(HttpServletRequest.class))).thenReturn(null);

        mockMvc.perform(post("/api/auth/refresh-token").with(csrf()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(authenticationService);
    }

    @Test
    @WithMockUser
    @DisplayName("Logout revokes refresh token and deletes cookies")
    void logout_success() throws Exception {
        UUID refreshToken = UUID.randomUUID();
        when(cookieService.extractRefreshTokenFromCookies(any(HttpServletRequest.class))).thenReturn(refreshToken);

        mockMvc.perform(post("/api/auth/logout").with(csrf()))
                .andExpect(status().isNoContent());

        verify(authenticationService).revokeRefreshToken(refreshToken);
        verify(cookieService).deleteAccessTokenCookie(any(HttpServletResponse.class));
        verify(cookieService).deleteRefreshTokenCookie(any(HttpServletResponse.class));
    }

    @Test
    @WithMockUser
    @DisplayName("Logout works even without refresh token")
    void logout_noRefreshToken() throws Exception {
        when(cookieService.extractRefreshTokenFromCookies(any(HttpServletRequest.class))).thenReturn(null);

        mockMvc.perform(post("/api/auth/logout").with(csrf()))
                .andExpect(status().isNoContent());

        verify(authenticationService, never()).revokeRefreshToken(any());
        verify(cookieService).deleteAccessTokenCookie(any(HttpServletResponse.class));
        verify(cookieService).deleteRefreshTokenCookie(any(HttpServletResponse.class));
    }

}