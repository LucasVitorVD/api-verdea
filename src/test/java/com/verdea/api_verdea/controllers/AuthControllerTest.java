package com.verdea.api_verdea.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.verdea.api_verdea.dtos.userDto.LoginResponse;
import com.verdea.api_verdea.dtos.userDto.UserRequestDTO;
import com.verdea.api_verdea.dtos.userDto.UserResponseDTO;
import com.verdea.api_verdea.exceptions.InvalidRefreshTokenException;
import com.verdea.api_verdea.services.authentication.AuthenticationService;
import com.verdea.api_verdea.services.authentication.CookieService;
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

    @Test
    @WithMockUser
    @DisplayName("Should register user successfully")
    void registerUserCase1() throws Exception {
        UserRequestDTO request = new UserRequestDTO("email@gmail.com", "12345");
        UserResponseDTO response = new UserResponseDTO(1L, request.email(), null);
        String reqBody = new ObjectMapper().writeValueAsString(request);

        when(userService.registerUser(eq(request))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqBody))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(1L),
                        jsonPath("$.email").value(response.email())
                );

        verify(userService).registerUser(eq(request));
    }

    @Test
    @WithMockUser
    @DisplayName("Should authenticate and return success message")
    void authenticateCase1() throws Exception {
        String email = "user@gmail.com";
        String password = "123456";
        String accessToken = "access-token";
        UUID refreshToken = UUID.randomUUID();

        UserRequestDTO requestDTO = new UserRequestDTO(email, password);
        LoginResponse loginResponse = new LoginResponse(accessToken, refreshToken);

        when(authenticationService.authenticate(any(UserRequestDTO.class))).thenReturn(loginResponse);

        String reqBody = new ObjectMapper().writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody))
                .andExpectAll(
                        status().isOk(),
                        content().string("Usuário autenticado!")
                );

        verify(authenticationService).authenticate(any(UserRequestDTO.class));
        verify(cookieService).addAccessTokenCookie(any(HttpServletResponse.class), eq(accessToken));
        verify(cookieService).addRefreshTokenCookie(any(HttpServletResponse.class), eq(refreshToken));
    }

    @Test
    @WithMockUser
    @DisplayName("Should throw an error if csrf not exists")
    void authenticateCase2() throws Exception {
        String email = "user@gmail.com";
        String password = "123456";

        UserRequestDTO requestDTO = new UserRequestDTO(email, password);

        when(authenticationService.authenticate(any(UserRequestDTO.class))).thenReturn(null);

        String reqBody = new ObjectMapper().writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqBody))
                .andExpectAll(
                        status().isForbidden()
                );
    }

    @Test
    @WithMockUser
    @DisplayName("Should refresh token successfully")
    void refreshTokenCase1() throws Exception {
        UUID validRefreshToken = UUID.randomUUID();
        String newAccessToken = "my-access-token";

        LoginResponse loginResponse = new LoginResponse(newAccessToken, validRefreshToken);

        when(cookieService.extractRefreshTokenFromCookies(any(HttpServletRequest.class))).thenReturn(validRefreshToken);
        when(authenticationService.refreshToken(eq(validRefreshToken))).thenReturn(loginResponse);

        mockMvc.perform(post("/api/auth/refresh-token").with(csrf()))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.accessToken").value(newAccessToken),
                        jsonPath("$.refreshToken").value(validRefreshToken.toString())
                );

        verify(cookieService).addAccessTokenCookie(any(HttpServletResponse.class), eq(newAccessToken));
        verify(cookieService).addRefreshTokenCookie(any(HttpServletResponse.class), eq(validRefreshToken));
    }

    @Test
    @WithMockUser
    @DisplayName("Should return UNAUTHORIZED when no refresh token in cookie")
    void refreshTokenCase2() throws Exception {
        when(cookieService.extractRefreshTokenFromCookies(any(HttpServletRequest.class))).thenReturn(null);

        mockMvc.perform(post("/api/auth/refresh-token").with(csrf()))
                .andExpectAll(
                        status().isUnauthorized()
                );

        verifyNoInteractions(authenticationService);
    }

    @Test
    @WithMockUser
    @DisplayName("Should return UNAUTHORIZED when refresh fails")
    void refreshTokenCase3() throws Exception {
        UUID invalidRefreshToken = UUID.randomUUID();

        when(cookieService.extractRefreshTokenFromCookies(any(HttpServletRequest.class))).thenReturn(invalidRefreshToken);
        when(authenticationService.refreshToken(eq(invalidRefreshToken))).thenThrow(new InvalidRefreshTokenException("Token inválido ou expirado"));

        mockMvc.perform(post("/api/auth/refresh-token").with(csrf()))
                .andExpectAll(
                        status().isUnauthorized()
                );

        verify(cookieService).deleteAccessTokenCookie(any(HttpServletResponse.class));
        verify(cookieService).deleteRefreshTokenCookie(any(HttpServletResponse.class));
    }

    @Test
    @WithMockUser
    @DisplayName("Should logout and revoke token successfully")
    void logoutCase1() throws Exception {
        UUID refreshToken = UUID.randomUUID();

        when(cookieService.extractRefreshTokenFromCookies(any(HttpServletRequest.class))).thenReturn(refreshToken);

        mockMvc.perform(post("/api/auth/logout").with(csrf()))
                .andExpectAll(
                        status().isNoContent()
                );

        verify(authenticationService).revokeRefreshToken(eq(refreshToken));
        verify(cookieService).deleteRefreshTokenCookie(any(HttpServletResponse.class));
        verify(cookieService).deleteAccessTokenCookie(any(HttpServletResponse.class));
    }

    @Test
    @WithMockUser
    @DisplayName("Should logout even without refresh token")
    void logoutCase2() throws Exception {
        when(cookieService.extractRefreshTokenFromCookies(any(HttpServletRequest.class))).thenReturn(null);

        mockMvc.perform(post("/api/auth/logout").with(csrf()))
                .andExpectAll(
                        status().isNoContent()
                );

        verify(authenticationService, never()).revokeRefreshToken(any());
        verify(cookieService).deleteRefreshTokenCookie(any(HttpServletResponse.class));
        verify(cookieService).deleteAccessTokenCookie(any(HttpServletResponse.class));
    }
}