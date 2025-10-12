package com.verdea.api_verdea.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.verdea.api_verdea.dtos.userDto.UpdateUserRequestDTO;
import com.verdea.api_verdea.dtos.userDto.UserResponseDTO;
import com.verdea.api_verdea.enums.Role;
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

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = true)
@WebMvcTest(UserProfileController.class)
class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CookieService cookieService;

    @MockitoBean
    private AuthenticationService authenticationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser(username = "user@gmail.com")
    @DisplayName("GET /me should return user profile")
    void getUserProfile_success() throws Exception {
        UserResponseDTO user = new UserResponseDTO(1L, "user@gmail.com", Role.USER, LocalDateTime.now());
        when(userService.getUserByEmail("user@gmail.com")).thenReturn(user);

        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("user@gmail.com"));
    }

    @Test
    @WithMockUser(username = "user@gmail.com")
    @DisplayName("PATCH /update should update user profile")
    void updateUserProfile_success() throws Exception {
        UpdateUserRequestDTO dto = new UpdateUserRequestDTO("new@gmail.com", "newpass");
        UserResponseDTO updatedUser = new UserResponseDTO(1L, "new@gmail.com", Role.USER, LocalDateTime.now());

        when(userService.updateUserInfo("user@gmail.com", dto)).thenReturn(updatedUser);

        mockMvc.perform(patch("/api/user/update").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new@gmail.com"));
    }

    @Test
    @WithMockUser(username = "user@gmail.com", authorities = {"USER"})
    @DisplayName("DELETE /delete should delete user if USER")
    void deleteUser() throws Exception {
        UUID refreshToken = UUID.randomUUID();
        when(userService.getUserByEmail("user@gmail.com")).thenReturn(new UserResponseDTO(1L, "user@gmail.com", Role.USER, LocalDateTime.now()));
        when(cookieService.extractRefreshTokenFromCookies(any(HttpServletRequest.class))).thenReturn(refreshToken);

        mockMvc.perform(delete("/api/user/delete").with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
        verify(authenticationService).revokeRefreshToken(refreshToken);
        verify(cookieService).deleteAccessTokenCookie(any(HttpServletResponse.class));
        verify(cookieService).deleteRefreshTokenCookie(any(HttpServletResponse.class));
    }
}